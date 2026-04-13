package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.search.TeacherScoreSearch;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.teacher.TeacherConductService.TeacherConductDashboardData;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ScoreDashboardData;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ScoreRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TeacherDashboardService {

    private final StudentDAO studentDAO;
    private final TeacherScoreService teacherScoreService;
    private final TeacherConductService teacherConductService;

    public TeacherDashboardService(StudentDAO studentDAO,
                                   TeacherScoreService teacherScoreService,
                                   TeacherConductService teacherConductService) {
        this.studentDAO = studentDAO;
        this.teacherScoreService = teacherScoreService;
        this.teacherConductService = teacherConductService;
    }

    @Transactional(readOnly = true)
    public TeacherDashboardData loadDashboard(String username, TeacherHomeroomScope scope) {
        if (scope == null || !scope.hasHomeroomClass()) {
            return TeacherDashboardData.empty();
        }

        ClassPopulation classPopulation = buildClassPopulation(scope.getClassId());
        ScoreOverview scoreOverview = buildScoreOverview(username, scope, classPopulation.getTotalStudents());
        ConductOverview conductOverview = buildConductOverview(scope);
        return new TeacherDashboardData(classPopulation, scoreOverview, conductOverview);
    }

    private ClassPopulation buildClassPopulation(String classId) {
        List<Object[]> rows = studentDAO.findStudentsByClassId(classId);
        int male = 0;
        int female = 0;

        for (Object[] row : rows) {
            String gender = asString(row, 2);
            String normalized = normalizeAsciiLower(gender);
            if ("nam".equals(normalized)) {
                male++;
            } else {
                female++;
            }
        }

        return new ClassPopulation(rows.size(), male, female);
    }

    private ScoreOverview buildScoreOverview(String username, TeacherHomeroomScope scope, int totalStudents) {
        TeacherScoreSearch scoreSearch = new TeacherScoreSearch();
        scoreSearch.setClassScope("HOMEROOM");
        scoreSearch.setClassId(scope.getClassId());
        scoreSearch.setHocKy("0");

        ScoreDashboardData scoreDashboardData = teacherScoreService.loadDashboardForExport(username, scope, scoreSearch);
        List<ScoreRow> rows = scoreDashboardData == null ? List.of() : scoreDashboardData.getRows();

        Map<String, StudentScoreAggregate> studentScores = new LinkedHashMap<>();
        for (ScoreRow row : rows) {
            if (row == null || row.getTongKet() == null) {
                continue;
            }
            String studentId = safeTrim(row.getStudentId());
            if (studentId == null) {
                continue;
            }
            studentScores.computeIfAbsent(studentId, key -> new StudentScoreAggregate())
                    .add(row.getTongKet());
        }

        int excellent = 0;
        int good = 0;
        int average = 0;
        int weak = 0;
        double totalAverage = 0.0;

        for (StudentScoreAggregate aggregate : studentScores.values()) {
            double studentAverage = aggregate.average();
            totalAverage += studentAverage;
            if (studentAverage >= 8.0) {
                excellent++;
            } else if (studentAverage >= 6.5) {
                good++;
            } else if (studentAverage >= 5.0) {
                average++;
            } else {
                weak++;
            }
        }

        int studentsWithScores = studentScores.size();
        int missingScores = Math.max(0, totalStudents - studentsWithScores);
        double classAverage = studentsWithScores == 0 ? 0.0 : totalAverage / studentsWithScores;

        return new ScoreOverview(
                studentsWithScores,
                missingScores,
                classAverage,
                excellent,
                good,
                average,
                weak
        );
    }

    private ConductOverview buildConductOverview(TeacherHomeroomScope scope) {
        ConductSearch conductSearch = new ConductSearch();
        TeacherConductDashboardData conductDashboardData = teacherConductService.loadDashboard(null, scope, conductSearch);
        ConductManagementService.ConductStats stats = conductDashboardData == null ? null : conductDashboardData.getStats();
        if (stats == null) {
            return new ConductOverview(0, 0, 0);
        }
        return new ConductOverview(
                stats.getTotalReward(),
                stats.getTotalDiscipline(),
                stats.getTotalRecords()
        );
    }

    private String asString(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }
        String value = row[index].toString().trim();
        return value.isEmpty() ? null : value;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeAsciiLower(String value) {
        String trimmed = safeTrim(value);
        if (trimmed == null) {
            return "";
        }
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    private static double toRate(long value, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return (value * 100.0) / total;
    }

    private static String formatRate(double value) {
        return String.format(Locale.US, "%.1f%%", value);
    }

    private static String formatRateValue(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static String formatOneDecimal(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static final class StudentScoreAggregate {
        private double total;
        private int count;

        private void add(Double value) {
            if (value == null) {
                return;
            }
            total += value;
            count++;
        }

        private double average() {
            if (count <= 0) {
                return 0.0;
            }
            return total / count;
        }
    }

    public static final class TeacherDashboardData {
        private final ClassPopulation classPopulation;
        private final ScoreOverview scoreOverview;
        private final ConductOverview conductOverview;

        public TeacherDashboardData(ClassPopulation classPopulation,
                                    ScoreOverview scoreOverview,
                                    ConductOverview conductOverview) {
            this.classPopulation = classPopulation;
            this.scoreOverview = scoreOverview;
            this.conductOverview = conductOverview;
        }

        public static TeacherDashboardData empty() {
            return new TeacherDashboardData(
                    new ClassPopulation(0, 0, 0),
                    new ScoreOverview(0, 0, 0.0, 0, 0, 0, 0),
                    new ConductOverview(0, 0, 0)
            );
        }

        public ClassPopulation getClassPopulation() {
            return classPopulation;
        }

        public ScoreOverview getScoreOverview() {
            return scoreOverview;
        }

        public ConductOverview getConductOverview() {
            return conductOverview;
        }
    }

    public static final class ClassPopulation {
        private final int totalStudents;
        private final int maleStudents;
        private final int femaleStudents;

        public ClassPopulation(int totalStudents,
                               int maleStudents,
                               int femaleStudents) {
            this.totalStudents = Math.max(0, totalStudents);
            this.maleStudents = Math.max(0, maleStudents);
            this.femaleStudents = Math.max(0, femaleStudents);
        }

        public int getTotalStudents() {
            return totalStudents;
        }

        public int getMaleStudents() {
            return maleStudents;
        }

        public int getFemaleStudents() {
            return femaleStudents;
        }

        public String getMaleRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(maleStudents, totalStudents));
        }

        public String getFemaleRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(femaleStudents, totalStudents));
        }

        public String getMaleRateValue() {
            return TeacherDashboardService.formatRateValue(TeacherDashboardService.toRate(maleStudents, totalStudents));
        }

        public String getFemaleRateValue() {
            return TeacherDashboardService.formatRateValue(TeacherDashboardService.toRate(femaleStudents, totalStudents));
        }
    }

    public static final class ScoreOverview {
        private final int studentsWithScores;
        private final int studentsMissingScores;
        private final double classAverage;
        private final int excellentCount;
        private final int goodCount;
        private final int averageCount;
        private final int weakCount;

        public ScoreOverview(int studentsWithScores,
                             int studentsMissingScores,
                             double classAverage,
                             int excellentCount,
                             int goodCount,
                             int averageCount,
                             int weakCount) {
            this.studentsWithScores = Math.max(0, studentsWithScores);
            this.studentsMissingScores = Math.max(0, studentsMissingScores);
            this.classAverage = Math.max(0.0, classAverage);
            this.excellentCount = Math.max(0, excellentCount);
            this.goodCount = Math.max(0, goodCount);
            this.averageCount = Math.max(0, averageCount);
            this.weakCount = Math.max(0, weakCount);
        }

        public int getStudentsWithScores() {
            return studentsWithScores;
        }

        public int getStudentsMissingScores() {
            return studentsMissingScores;
        }

        public String getClassAverageDisplay() {
            return TeacherDashboardService.formatOneDecimal(classAverage);
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

        public int getRatedStudents() {
            return excellentCount + goodCount + averageCount + weakCount;
        }

        public int getGoodPlusCount() {
            return excellentCount + goodCount;
        }

        public String getGoodPlusRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(getGoodPlusCount(), getRatedStudents()));
        }

        public String getExcellentRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(excellentCount, getRatedStudents()));
        }

        public String getGoodRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(goodCount, getRatedStudents()));
        }

        public String getAverageRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(averageCount, getRatedStudents()));
        }

        public String getWeakRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(weakCount, getRatedStudents()));
        }

        public String getExcellentRateValue() {
            return TeacherDashboardService.formatRateValue(TeacherDashboardService.toRate(excellentCount, getRatedStudents()));
        }

        public String getGoodRateValue() {
            return TeacherDashboardService.formatRateValue(TeacherDashboardService.toRate(goodCount, getRatedStudents()));
        }

        public String getAverageRateValue() {
            return TeacherDashboardService.formatRateValue(TeacherDashboardService.toRate(averageCount, getRatedStudents()));
        }

        public String getWeakRateValue() {
            return TeacherDashboardService.formatRateValue(TeacherDashboardService.toRate(weakCount, getRatedStudents()));
        }
    }

    public static final class ConductOverview {
        private final long rewardCount;
        private final long disciplineCount;
        private final long totalCount;

        public ConductOverview(long rewardCount, long disciplineCount, long totalCount) {
            this.rewardCount = Math.max(0, rewardCount);
            this.disciplineCount = Math.max(0, disciplineCount);
            this.totalCount = Math.max(0, totalCount);
        }

        public long getRewardCount() {
            return rewardCount;
        }

        public long getDisciplineCount() {
            return disciplineCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public String getRewardRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(rewardCount, totalCount));
        }

        public String getDisciplineRateDisplay() {
            return TeacherDashboardService.formatRate(TeacherDashboardService.toRate(disciplineCount, totalCount));
        }

        public String getRewardRateValue() {
            return TeacherDashboardService.formatRateValue(TeacherDashboardService.toRate(rewardCount, totalCount));
        }

        public String getDisciplineRateValue() {
            return TeacherDashboardService.formatRateValue(TeacherDashboardService.toRate(disciplineCount, totalCount));
        }
    }
}
