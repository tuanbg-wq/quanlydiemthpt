package com.quanly.webdiem.model.service.teacher_subject;

import com.quanly.webdiem.model.search.TeacherScoreSearch;
import com.quanly.webdiem.model.service.admin.TeacherInfoService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherProfileService;
import com.quanly.webdiem.model.service.teacher.TeacherProfileService.TeacherProfilePageData;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ClassFilterOption;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.CreateScopeData;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.FilterOption;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ScoreDashboardData;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ScoreRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class TeacherSubjectDashboardService {

    private static final String CLASS_SCOPE_SUBJECT = "SUBJECT";
    private static final int SPOTLIGHT_LIMIT = 5;

    private final TeacherHomeroomScopeService scopeService;
    private final TeacherScoreService teacherScoreService;
    private final TeacherProfileService teacherProfileService;

    public TeacherSubjectDashboardService(TeacherHomeroomScopeService scopeService,
                                          TeacherScoreService teacherScoreService,
                                          TeacherProfileService teacherProfileService) {
        this.scopeService = scopeService;
        this.teacherScoreService = teacherScoreService;
        this.teacherProfileService = teacherProfileService;
    }

    @Transactional(readOnly = true)
    public TeacherSubjectDashboardData loadDashboard(String username) {
        TeacherHomeroomScope scope = scopeService.resolveByUsername(username);
        TeacherProfilePageData profilePageData = teacherProfileService.getProfilePageData(username);
        String teacherId = teacherScoreService.resolveTeacherId(username);
        String schoolYear = teacherScoreService.resolveSchoolYearForTeacher(username, scope);

        CreateScopeData createScopeData = teacherScoreService.buildCreateScopeData(username, scope, null, null);
        List<ClassFilterOption> classOptions = createScopeData == null || createScopeData.getClassOptions() == null
                ? List.of()
                : createScopeData.getClassOptions();

        TeacherScoreSearch search = new TeacherScoreSearch();
        search.setClassScope(CLASS_SCOPE_SUBJECT);
        search.setHocKy("0");

        ScoreDashboardData scoreDashboardData = teacherScoreService.loadDashboardForExport(username, scope, search);
        List<FilterOption> teachingSubjects = scoreDashboardData == null || scoreDashboardData.getTeachingSubjects() == null
                ? List.of()
                : scoreDashboardData.getTeachingSubjects();
        List<ScoreRow> rows = scoreDashboardData == null || scoreDashboardData.getRows() == null
                ? List.of()
                : scoreDashboardData.getRows();

        AggregationResult aggregation = aggregate(rows, classOptions);

        return new TeacherSubjectDashboardData(
                scope,
                buildTeacherSummary(username, teacherId, schoolYear, scope, profilePageData, classOptions, teachingSubjects),
                buildSummaryMetrics(aggregation.overallStudents, classOptions.size(), teachingSubjects.size()),
                buildScoreDistribution(aggregation.overallStudents),
                aggregation.classSummaries,
                aggregation.topStudents,
                aggregation.supportStudents
        );
    }

    private TeacherSummary buildTeacherSummary(String username,
                                               String teacherId,
                                               String schoolYear,
                                               TeacherHomeroomScope scope,
                                               TeacherProfilePageData profilePageData,
                                               List<ClassFilterOption> classOptions,
                                               List<FilterOption> teachingSubjects) {
        TeacherInfoService.TeacherInfoView teacherInfo = profilePageData == null ? null : profilePageData.getTeacherInfo();
        String teacherName = firstNonBlank(
                teacherInfo == null ? null : teacherInfo.getHoTen(),
                scope == null ? null : scope.getTeacherName(),
                username,
                "Giáo viên bộ môn"
        );
        String avatar = teacherInfo == null ? null : teacherInfo.getAvatar();
        String subjectDisplay = joinDistinctNames(teachingSubjects, 4, "môn học");
        String classDisplay = joinDistinctClasses(classOptions, 5);
        return new TeacherSummary(
                teacherName,
                teacherId,
                firstNonBlank(schoolYear, scope == null ? null : scope.getSchoolYear()),
                avatar,
                subjectDisplay,
                classDisplay,
                classOptions.size(),
                teachingSubjects.size(),
                username
        );
    }

    private SummaryMetrics buildSummaryMetrics(List<StudentAggregate> students,
                                               int classCount,
                                               int subjectCount) {
        if (students == null || students.isEmpty()) {
            return new SummaryMetrics(0.0, 0, 0, 0, 0, 0, classCount, subjectCount);
        }

        int excellent = 0;
        int good = 0;
        int average = 0;
        int weak = 0;
        double totalAverage = 0.0;

        for (StudentAggregate student : students) {
            if (student == null) {
                continue;
            }
            double score = student.average();
            totalAverage += score;
            PerformanceLevel level = PerformanceLevel.fromScore(score);
            if (level == PerformanceLevel.EXCELLENT) {
                excellent++;
            } else if (level == PerformanceLevel.GOOD) {
                good++;
            } else if (level == PerformanceLevel.AVERAGE) {
                average++;
            } else {
                weak++;
            }
        }

        double overallAverage = totalAverage / Math.max(1, students.size());
        return new SummaryMetrics(overallAverage, students.size(), excellent, good, average, weak, classCount, subjectCount);
    }

    private ScoreDistribution buildScoreDistribution(List<StudentAggregate> students) {
        if (students == null || students.isEmpty()) {
            return new ScoreDistribution(0, 0, 0, 0);
        }

        int excellent = 0;
        int good = 0;
        int average = 0;
        int weak = 0;
        for (StudentAggregate student : students) {
            if (student == null) {
                continue;
            }
            PerformanceLevel level = PerformanceLevel.fromScore(student.average());
            if (level == PerformanceLevel.EXCELLENT) {
                excellent++;
            } else if (level == PerformanceLevel.GOOD) {
                good++;
            } else if (level == PerformanceLevel.AVERAGE) {
                average++;
            } else {
                weak++;
            }
        }
        return new ScoreDistribution(excellent, good, average, weak);
    }

    private AggregationResult aggregate(List<ScoreRow> rows, List<ClassFilterOption> classOptions) {
        LinkedHashMap<String, StudentAggregate> overallByStudent = new LinkedHashMap<>();
        LinkedHashMap<String, ClassAccumulator> classAccumulators = new LinkedHashMap<>();

        if (classOptions != null) {
            for (ClassFilterOption option : classOptions) {
                if (option == null || safeTrim(option.getId()) == null) {
                    continue;
                }
                classAccumulators.put(
                        option.getId().toLowerCase(Locale.ROOT),
                        new ClassAccumulator(option.getId(), option.getName())
                );
            }
        }

        if (rows != null) {
            for (ScoreRow row : rows) {
                if (row == null) {
                    continue;
                }
                Double scoreValue = resolveAnnualScore(row);
                String studentId = safeTrim(row.getStudentId());
                String classId = safeTrim(row.getClassId());
                if (scoreValue == null || studentId == null || classId == null) {
                    continue;
                }

                String studentKey = studentId.toLowerCase(Locale.ROOT);
                overallByStudent.computeIfAbsent(
                        studentKey,
                        ignored -> new StudentAggregate(studentId, row.getStudentName(), classId, resolveClassName(row))
                ).add(scoreValue);

                ClassAccumulator classAccumulator = classAccumulators.computeIfAbsent(
                        classId.toLowerCase(Locale.ROOT),
                        ignored -> new ClassAccumulator(classId, resolveClassName(row))
                );
                classAccumulator.addStudentScore(studentId, row.getStudentName(), scoreValue);
            }
        }

        List<StudentAggregate> overallStudents = new ArrayList<>(overallByStudent.values());
        List<StudentSpotlight> topStudents = overallStudents.stream()
                .sorted(STUDENT_DESC_COMPARATOR)
                .limit(SPOTLIGHT_LIMIT)
                .map(this::mapSpotlight)
                .toList();
        List<StudentSpotlight> supportStudents = overallStudents.stream()
                .sorted(STUDENT_ASC_COMPARATOR)
                .limit(SPOTLIGHT_LIMIT)
                .map(this::mapSpotlight)
                .toList();

        List<ClassSummary> classSummaries = classAccumulators.values().stream()
                .map(ClassAccumulator::toSummary)
                .sorted(Comparator.comparing(ClassSummary::getClassSortKey))
                .toList();

        return new AggregationResult(overallStudents, classSummaries, topStudents, supportStudents);
    }

    private StudentSpotlight mapSpotlight(StudentAggregate aggregate) {
        double averageScore = aggregate.average();
        PerformanceLevel level = PerformanceLevel.fromScore(averageScore);
        return new StudentSpotlight(
                aggregate.studentId,
                firstNonBlank(aggregate.studentName, aggregate.studentId, "Học sinh"),
                aggregate.classId,
                aggregate.classDisplay(),
                averageScore,
                level
        );
    }

    private Double resolveAnnualScore(ScoreRow row) {
        if (row == null) {
            return null;
        }
        if (row.getTongKetCaNam() != null) {
            return row.getTongKetCaNam();
        }
        return row.getTongKet();
    }

    private String resolveClassName(ScoreRow row) {
        if (row == null) {
            return null;
        }
        String classDisplay = safeTrim(row.getClassDisplay());
        if (classDisplay != null) {
            return classDisplay;
        }
        return safeTrim(row.getClassId());
    }

    private String joinDistinctClasses(List<ClassFilterOption> classOptions, int limit) {
        if (classOptions == null || classOptions.isEmpty()) {
            return "Chưa có lớp được phân công";
        }
        List<String> items = classOptions.stream()
                .filter(Objects::nonNull)
                .map(item -> firstNonBlank(item.getName(), item.getId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return joinLimited(items, limit, "lớp");
    }

    private String joinDistinctNames(List<FilterOption> options, int limit, String emptyFallback) {
        if (options == null || options.isEmpty()) {
            return "Chưa có " + emptyFallback;
        }
        List<String> items = options.stream()
                .filter(Objects::nonNull)
                .map(item -> firstNonBlank(item.getName(), item.getId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return joinLimited(items, limit, emptyFallback);
    }

    private String joinLimited(List<String> items, int limit, String emptyFallback) {
        if (items == null || items.isEmpty()) {
            return "Chưa có " + emptyFallback;
        }
        int safeLimit = Math.max(1, limit);
        int size = items.size();
        if (size <= safeLimit) {
            return String.join(", ", items);
        }
        List<String> limitedItems = items.subList(0, safeLimit);
        return String.join(", ", limitedItems) + " +" + (size - safeLimit);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String trimmed = safeTrim(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String formatOneDecimal(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static String formatRate(double value) {
        return formatOneDecimal(value) + "%";
    }

    private static String formatRateValue(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static double toRate(long value, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return (value * 100.0) / total;
    }

    private static final Comparator<StudentAggregate> STUDENT_DESC_COMPARATOR =
            Comparator.comparingDouble(StudentAggregate::average).reversed()
                    .thenComparing(StudentAggregate::normalizedStudentName)
                    .thenComparing(StudentAggregate::normalizedStudentId);

    private static final Comparator<StudentAggregate> STUDENT_ASC_COMPARATOR =
            Comparator.comparingDouble(StudentAggregate::average)
                    .thenComparing(StudentAggregate::normalizedStudentName)
                    .thenComparing(StudentAggregate::normalizedStudentId);

    private record AggregationResult(
            List<StudentAggregate> overallStudents,
            List<ClassSummary> classSummaries,
            List<StudentSpotlight> topStudents,
            List<StudentSpotlight> supportStudents
    ) {
    }

    private static final class StudentAggregate {
        private final String studentId;
        private final String studentName;
        private final String classId;
        private final String className;
        private double totalScore;
        private int scoreCount;

        private StudentAggregate(String studentId,
                                 String studentName,
                                 String classId,
                                 String className) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.classId = classId;
            this.className = className;
        }

        private void add(double score) {
            totalScore += score;
            scoreCount++;
        }

        private double average() {
            if (scoreCount <= 0) {
                return 0.0;
            }
            return totalScore / scoreCount;
        }

        private String classDisplay() {
            if (classId == null || classId.isBlank()) {
                return className == null ? "-" : className;
            }
            if (className == null || className.isBlank() || className.equalsIgnoreCase(classId)) {
                return classId;
            }
            return classId + " - " + className;
        }

        private String normalizedStudentName() {
            return studentName == null ? "" : studentName.trim().toLowerCase(Locale.ROOT);
        }

        private String normalizedStudentId() {
            return studentId == null ? "" : studentId.trim().toLowerCase(Locale.ROOT);
        }
    }

    private static final class ClassAccumulator {
        private final String classId;
        private final String className;
        private final LinkedHashMap<String, StudentAggregate> students = new LinkedHashMap<>();

        private ClassAccumulator(String classId, String className) {
            this.classId = classId;
            this.className = className;
        }

        private void addStudentScore(String studentId, String studentName, double score) {
            String key = studentId.toLowerCase(Locale.ROOT);
            students.computeIfAbsent(
                    key,
                    ignored -> new StudentAggregate(studentId, studentName, classId, className)
            ).add(score);
        }

        private ClassSummary toSummary() {
            int excellent = 0;
            int good = 0;
            int average = 0;
            int weak = 0;
            double totalAverage = 0.0;

            for (StudentAggregate student : students.values()) {
                double score = student.average();
                totalAverage += score;
                PerformanceLevel level = PerformanceLevel.fromScore(score);
                if (level == PerformanceLevel.EXCELLENT) {
                    excellent++;
                } else if (level == PerformanceLevel.GOOD) {
                    good++;
                } else if (level == PerformanceLevel.AVERAGE) {
                    average++;
                } else {
                    weak++;
                }
            }

            int studentCount = students.size();
            double averageScore = studentCount == 0 ? 0.0 : totalAverage / studentCount;
            return new ClassSummary(classId, className, studentCount, averageScore, excellent, good, average, weak);
        }
    }

    private enum PerformanceLevel {
        EXCELLENT("Giỏi", "excellent"),
        GOOD("Khá", "good"),
        AVERAGE("Trung bình", "average"),
        WEAK("Yếu", "weak");

        private final String label;
        private final String cssClass;

        PerformanceLevel(String label, String cssClass) {
            this.label = label;
            this.cssClass = cssClass;
        }

        private static PerformanceLevel fromScore(double score) {
            if (score >= 8.0) {
                return EXCELLENT;
            }
            if (score >= 6.5) {
                return GOOD;
            }
            if (score >= 5.0) {
                return AVERAGE;
            }
            return WEAK;
        }
    }

    public static class TeacherSubjectDashboardData {
        private final TeacherHomeroomScope scope;
        private final TeacherSummary teacherSummary;
        private final SummaryMetrics summaryMetrics;
        private final ScoreDistribution scoreDistribution;
        private final List<ClassSummary> classSummaries;
        private final List<StudentSpotlight> topStudents;
        private final List<StudentSpotlight> supportStudents;

        public TeacherSubjectDashboardData(TeacherHomeroomScope scope,
                                           TeacherSummary teacherSummary,
                                           SummaryMetrics summaryMetrics,
                                           ScoreDistribution scoreDistribution,
                                           List<ClassSummary> classSummaries,
                                           List<StudentSpotlight> topStudents,
                                           List<StudentSpotlight> supportStudents) {
            this.scope = scope;
            this.teacherSummary = teacherSummary;
            this.summaryMetrics = summaryMetrics;
            this.scoreDistribution = scoreDistribution;
            this.classSummaries = classSummaries == null ? List.of() : classSummaries;
            this.topStudents = topStudents == null ? List.of() : topStudents;
            this.supportStudents = supportStudents == null ? List.of() : supportStudents;
        }

        public TeacherHomeroomScope getScope() {
            return scope;
        }

        public TeacherSummary getTeacherSummary() {
            return teacherSummary;
        }

        public SummaryMetrics getSummaryMetrics() {
            return summaryMetrics;
        }

        public ScoreDistribution getScoreDistribution() {
            return scoreDistribution;
        }

        public List<ClassSummary> getClassSummaries() {
            return classSummaries;
        }

        public List<StudentSpotlight> getTopStudents() {
            return topStudents;
        }

        public List<StudentSpotlight> getSupportStudents() {
            return supportStudents;
        }

        public boolean isHasAssignments() {
            return teacherSummary != null && teacherSummary.getClassCount() > 0;
        }

        public boolean isHasScoreData() {
            return summaryMetrics != null && summaryMetrics.getStudentCount() > 0;
        }
    }

    public static class TeacherSummary {
        private final String teacherName;
        private final String teacherId;
        private final String schoolYear;
        private final String avatar;
        private final String subjectDisplay;
        private final String classDisplay;
        private final int classCount;
        private final int subjectCount;
        private final String username;

        public TeacherSummary(String teacherName,
                              String teacherId,
                              String schoolYear,
                              String avatar,
                              String subjectDisplay,
                              String classDisplay,
                              int classCount,
                              int subjectCount,
                              String username) {
            this.teacherName = teacherName;
            this.teacherId = teacherId;
            this.schoolYear = schoolYear;
            this.avatar = avatar;
            this.subjectDisplay = subjectDisplay;
            this.classDisplay = classDisplay;
            this.classCount = Math.max(0, classCount);
            this.subjectCount = Math.max(0, subjectCount);
            this.username = username;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public String getTeacherId() {
            return teacherId;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getSubjectDisplay() {
            return subjectDisplay;
        }

        public String getClassDisplay() {
            return classDisplay;
        }

        public int getClassCount() {
            return classCount;
        }

        public int getSubjectCount() {
            return subjectCount;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class SummaryMetrics {
        private final double overallAverage;
        private final int studentCount;
        private final int excellentCount;
        private final int goodCount;
        private final int averageCount;
        private final int weakCount;
        private final int classCount;
        private final int subjectCount;

        public SummaryMetrics(double overallAverage,
                              int studentCount,
                              int excellentCount,
                              int goodCount,
                              int averageCount,
                              int weakCount,
                              int classCount,
                              int subjectCount) {
            this.overallAverage = Math.max(0.0, overallAverage);
            this.studentCount = Math.max(0, studentCount);
            this.excellentCount = Math.max(0, excellentCount);
            this.goodCount = Math.max(0, goodCount);
            this.averageCount = Math.max(0, averageCount);
            this.weakCount = Math.max(0, weakCount);
            this.classCount = Math.max(0, classCount);
            this.subjectCount = Math.max(0, subjectCount);
        }

        public double getOverallAverage() {
            return overallAverage;
        }

        public String getOverallAverageDisplay() {
            return formatOneDecimal(overallAverage);
        }

        public int getStudentCount() {
            return studentCount;
        }

        public int getExcellentCount() {
            return excellentCount;
        }

        public int getGoodCount() {
            return goodCount;
        }

        public int getAverageCount() {
            return averageCount;
        }

        public int getWeakCount() {
            return weakCount;
        }

        public int getClassCount() {
            return classCount;
        }

        public int getSubjectCount() {
            return subjectCount;
        }

        public int getGoodPlusCount() {
            return excellentCount + goodCount;
        }

        public String getGoodPlusRateDisplay() {
            return formatRate(toRate(getGoodPlusCount(), studentCount));
        }

        public String getGoodPlusRateValue() {
            return formatRateValue(toRate(getGoodPlusCount(), studentCount));
        }
    }

    public static class ScoreDistribution {
        private final int excellentCount;
        private final int goodCount;
        private final int averageCount;
        private final int weakCount;

        public ScoreDistribution(int excellentCount,
                                 int goodCount,
                                 int averageCount,
                                 int weakCount) {
            this.excellentCount = Math.max(0, excellentCount);
            this.goodCount = Math.max(0, goodCount);
            this.averageCount = Math.max(0, averageCount);
            this.weakCount = Math.max(0, weakCount);
        }

        public int getExcellentCount() {
            return excellentCount;
        }

        public int getGoodCount() {
            return goodCount;
        }

        public int getAverageCount() {
            return averageCount;
        }

        public int getWeakCount() {
            return weakCount;
        }

        public int getTotalStudents() {
            return excellentCount + goodCount + averageCount + weakCount;
        }

        public String getExcellentRateDisplay() {
            return formatRate(toRate(excellentCount, getTotalStudents()));
        }

        public String getGoodRateDisplay() {
            return formatRate(toRate(goodCount, getTotalStudents()));
        }

        public String getAverageRateDisplay() {
            return formatRate(toRate(averageCount, getTotalStudents()));
        }

        public String getWeakRateDisplay() {
            return formatRate(toRate(weakCount, getTotalStudents()));
        }

        public String getExcellentRateValue() {
            return formatRateValue(toRate(excellentCount, getTotalStudents()));
        }

        public String getGoodRateValue() {
            return formatRateValue(toRate(goodCount, getTotalStudents()));
        }

        public String getAverageRateValue() {
            return formatRateValue(toRate(averageCount, getTotalStudents()));
        }

        public String getWeakRateValue() {
            return formatRateValue(toRate(weakCount, getTotalStudents()));
        }
    }

    public static class ClassSummary {
        private final String classId;
        private final String className;
        private final int studentCount;
        private final double averageScore;
        private final int excellentCount;
        private final int goodCount;
        private final int averageCount;
        private final int weakCount;

        public ClassSummary(String classId,
                            String className,
                            int studentCount,
                            double averageScore,
                            int excellentCount,
                            int goodCount,
                            int averageCount,
                            int weakCount) {
            this.classId = classId;
            this.className = className;
            this.studentCount = Math.max(0, studentCount);
            this.averageScore = Math.max(0.0, averageScore);
            this.excellentCount = Math.max(0, excellentCount);
            this.goodCount = Math.max(0, goodCount);
            this.averageCount = Math.max(0, averageCount);
            this.weakCount = Math.max(0, weakCount);
        }

        public String getClassId() {
            return classId;
        }

        public String getClassName() {
            return className;
        }

        public String getClassLabel() {
            if (classId == null || classId.isBlank()) {
                return className == null ? "-" : className;
            }
            if (className == null || className.isBlank() || className.equalsIgnoreCase(classId)) {
                return classId;
            }
            return classId + " - " + className;
        }

        public String getClassSortKey() {
            return (classId == null ? "" : classId).toLowerCase(Locale.ROOT);
        }

        public int getStudentCount() {
            return studentCount;
        }

        public String getAverageScoreDisplay() {
            return formatOneDecimal(averageScore);
        }

        public int getExcellentCount() {
            return excellentCount;
        }

        public int getGoodCount() {
            return goodCount;
        }

        public int getAverageCount() {
            return averageCount;
        }

        public int getWeakCount() {
            return weakCount;
        }

        public int getGoodPlusCount() {
            return excellentCount + goodCount;
        }

        public String getGoodPlusRateDisplay() {
            return formatRate(toRate(getGoodPlusCount(), studentCount));
        }

        public String getGoodPlusRateValue() {
            return formatRateValue(toRate(getGoodPlusCount(), studentCount));
        }
    }

    public static class StudentSpotlight {
        private final String studentId;
        private final String studentName;
        private final String classId;
        private final String classDisplay;
        private final double averageScore;
        private final PerformanceLevel performanceLevel;

        public StudentSpotlight(String studentId,
                                String studentName,
                                String classId,
                                String classDisplay,
                                double averageScore,
                                PerformanceLevel performanceLevel) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.classId = classId;
            this.classDisplay = classDisplay;
            this.averageScore = Math.max(0.0, averageScore);
            this.performanceLevel = performanceLevel == null ? PerformanceLevel.AVERAGE : performanceLevel;
        }

        public String getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getClassId() {
            return classId;
        }

        public String getClassDisplay() {
            return classDisplay;
        }

        public String getAverageScoreDisplay() {
            return formatOneDecimal(averageScore);
        }

        public String getPerformanceLabel() {
            return performanceLevel.label;
        }

        public String getPerformanceCssClass() {
            return performanceLevel.cssClass;
        }
    }
}
