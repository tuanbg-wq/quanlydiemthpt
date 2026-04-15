package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.dao.ScoreDAO;
import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.search.TeacherScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class TeacherScoreService {

    private static final int PAGE_SIZE = 6;
    private static final String CLASS_SCOPE_HOMEROOM = "HOMEROOM";
    private static final String CLASS_SCOPE_SUBJECT = "SUBJECT";
    private static final String ACADEMIC_EXCELLENT = "gioi";
    private static final String ACADEMIC_GOOD = "kha";
    private static final String ACADEMIC_AVERAGE = "trung_binh";
    private static final String ACADEMIC_WEAK = "yeu";
    private static final String ACADEMIC_POOR = "kem";
    private static final String META_TX_KEY = "so cot diem thuong xuyen";
    private static final int DEFAULT_FREQUENT_COLUMNS = 3;

    private static final LinkedHashMap<String, Integer> DEFAULT_FREQUENT_SCORE_RULES = buildFrequentScoreRules();

    private final ScoreDAO scoreDAO;
    private final StudentDAO studentDAO;

    public TeacherScoreService(ScoreDAO scoreDAO, StudentDAO studentDAO) {
        this.scoreDAO = scoreDAO;
        this.studentDAO = studentDAO;
    }

    @Transactional(readOnly = true)
    public ScoreDashboardData loadDashboard(String username,
                                            TeacherHomeroomScope scope,
                                            TeacherScoreSearch rawSearch) {
        return loadDashboardInternal(username, scope, rawSearch, true);
    }

    @Transactional(readOnly = true)
    public ScoreDashboardData loadDashboardForExport(String username,
                                                     TeacherHomeroomScope scope,
                                                     TeacherScoreSearch rawSearch) {
        return loadDashboardInternal(username, scope, rawSearch, false);
    }

    @Transactional(readOnly = true)
    public List<ScoreCreateService.FrequentRuleItem> loadFrequentRuleItems(String username,
                                                                           TeacherHomeroomScope scope,
                                                                           String classId) {
        String teacherId = resolveTeacherId(username);
        String schoolYear = resolveSchoolYear(scope, teacherId);
        String normalizedClassId = safeTrim(classId);

        if (teacherId == null || schoolYear == null) {
            return List.of();
        }

        LinkedHashMap<String, ScoreCreateService.FrequentRuleItem> items = new LinkedHashMap<>();
        for (Object[] row : scoreDAO.findTeachingSubjectRulesByTeacherAndYear(teacherId, schoolYear, normalizedClassId)) {
            String subjectId = asString(row, 0, null);
            if (subjectId == null) {
                continue;
            }
            String subjectName = asString(row, 1, subjectId);
            String description = asString(row, 2, null);
            int frequentColumns = resolveFrequentColumns(subjectId, subjectName, description);
            items.put(
                    subjectId.toLowerCase(Locale.ROOT),
                    new ScoreCreateService.FrequentRuleItem(subjectName + " (" + subjectId + ")", frequentColumns)
            );
        }
        return new ArrayList<>(items.values());
    }

    private ScoreDashboardData loadDashboardInternal(String username,
                                                     TeacherHomeroomScope scope,
                                                     TeacherScoreSearch rawSearch,
                                                     boolean paginate) {
        String teacherId = resolveTeacherId(username);
        String homeroomClassId = safeTrim(scope == null ? null : scope.getClassId());
        String homeroomClassName = safeTrim(scope == null ? null : scope.getClassName());
        String schoolYear = resolveSchoolYear(scope, teacherId);

        if (teacherId == null || schoolYear == null) {
            TeacherScoreSearch search = normalizeSearch(rawSearch, List.of());
            return ScoreDashboardData.empty(search, schoolYear);
        }
        List<ClassFilterOption> classOptions = buildClassOptions(
                teacherId,
                schoolYear,
                homeroomClassId,
                homeroomClassName
        );
        TeacherScoreSearch search = normalizeSearch(rawSearch, classOptions);

        List<FilterOption> visibleSubjectOptions = scoreDAO
                .findVisibleSubjectsForTeacherScore(teacherId, homeroomClassId, schoolYear)
                .stream()
                .map(this::mapFilterOption)
                .filter(Objects::nonNull)
                .toList();

        List<FilterOption> teachingSubjectOptions = scoreDAO
                .findTeachingSubjectsByTeacherAndYear(teacherId, schoolYear)
                .stream()
                .map(this::mapFilterOption)
                .filter(Objects::nonNull)
                .toList();

        List<ScoreRow> allRows = scoreDAO.findRowsForTeacherScore(
                        teacherId,
                        homeroomClassId,
                        schoolYear,
                        safeTrim(search.getQ()),
                        safeTrim(search.getMon()),
                        safeTrim(search.getClassId()),
                        safeTrim(search.getKhoa()),
                        parseHocKy(search.getHocKy()),
                        normalizeClassScope(search.getClassScope())
                ).stream()
                .map(this::mapScoreRow)
                .filter(Objects::nonNull)
                .toList();
        if (isAnnualSearch(search)) {
            allRows = mergeAnnualRows(allRows);
        }
        allRows = applyAcademicLevelFilter(allRows, search.getHocLuc());

        ScoreStats stats = calculateStats(allRows);
        PageData pageData = paginate
                ? paginate(allRows, normalizePage(search.getPage()))
                : new PageData(
                        allRows,
                        1,
                        1,
                        allRows.size(),
                        allRows.isEmpty() ? 0 : 1,
                        allRows.size()
                );

        return new ScoreDashboardData(
                search,
                schoolYear,
                teacherId,
                visibleSubjectOptions,
                teachingSubjectOptions,
                classOptions,
                pageData.items(),
                pageData.page(),
                pageData.totalPages(),
                pageData.totalItems(),
                pageData.fromRecord(),
                pageData.toRecord(),
                stats
        );
    }

    @Transactional(readOnly = true)
    public void assertCanEditScore(String username,
                                   TeacherHomeroomScope scope,
                                   String studentId,
                                   String subjectId,
                                   String namHoc,
                                   String hocKy) {
        String teacherId = resolveTeacherId(username);
        String normalizedStudentId = safeTrim(studentId);
        String normalizedSubjectId = safeTrim(subjectId);
        String normalizedYear = safeTrim(namHoc);
        Integer semester = parseHocKy(hocKy);

        if (teacherId == null) {
            throw new RuntimeException("Không xác định được giáo viên từ tài khoản đang đăng nhập.");
        }
        if (normalizedStudentId == null || normalizedSubjectId == null || normalizedYear == null || semester == null || semester <= 0) {
            throw new RuntimeException("Thiếu thông tin để mở/chỉnh sửa điểm.");
        }

        Student student = studentDAO.findById(normalizedStudentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh cần thao tác."));
        ClassEntity classEntity = student.getLop();
        if (classEntity == null || safeTrim(classEntity.getIdLop()) == null) {
            throw new RuntimeException("Không xác định được lớp hiện tại của học sinh.");
        }

        String classId = classEntity.getIdLop();
        String homeroomClassId = safeTrim(scope == null ? null : scope.getClassId());
        if (homeroomClassId != null && homeroomClassId.equalsIgnoreCase(classId)) {
            throw new RuntimeException("Lớp chủ nhiệm chỉ được xem chi tiết điểm, không được nhập/sửa/xóa.");
        }

        long assignmentCount = scoreDAO.countTeachingAssignmentForScore(
                teacherId,
                normalizedSubjectId,
                normalizedYear,
                semester,
                classId
        );
        if (assignmentCount > 0) {
            return;
        }

        throw new RuntimeException("Bạn chưa được phân công dạy môn này ở lớp của học sinh, nên không thể nhập/sửa điểm.");
    }

    @Transactional(readOnly = true)
    public void assertCanViewScore(String username,
                                   TeacherHomeroomScope scope,
                                   String studentId,
                                   String subjectId,
                                   String namHoc,
                                   String hocKy) {
        String teacherId = resolveTeacherId(username);
        String normalizedStudentId = safeTrim(studentId);
        String normalizedSubjectId = safeTrim(subjectId);
        String normalizedYear = safeTrim(namHoc);

        if (teacherId == null) {
            throw new RuntimeException("Không xác định được giáo viên từ tài khoản đăng nhập.");
        }
        if (normalizedStudentId == null || normalizedSubjectId == null || normalizedYear == null) {
            throw new RuntimeException("Thiếu thông tin để xem chi tiết điểm.");
        }

        Student student = studentDAO.findById(normalizedStudentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh cần thao tác."));
        ClassEntity classEntity = student.getLop();
        if (classEntity == null || safeTrim(classEntity.getIdLop()) == null) {
            throw new RuntimeException("Không xác định được lớp hiện tại của học sinh.");
        }
        String classId = classEntity.getIdLop();

        String homeroomClassId = safeTrim(scope == null ? null : scope.getClassId());
        if (homeroomClassId != null && homeroomClassId.equalsIgnoreCase(classId)) {
            return;
        }

        List<Integer> semesters = resolveSemesters(hocKy);
        for (Integer semester : semesters) {
            long assignmentCount = scoreDAO.countTeachingAssignmentForScore(
                    teacherId,
                    normalizedSubjectId,
                    normalizedYear,
                    semester,
                    classId
            );
            if (assignmentCount > 0) {
                return;
            }
        }
        throw new RuntimeException("Bạn không có quyền xem chi tiết điểm của học sinh này.");
    }

    @Transactional(readOnly = true)
    public void assertCanDeleteScoreGroup(String username,
                                          TeacherHomeroomScope scope,
                                          String studentId,
                                          String subjectId,
                                          String namHoc) {
        String teacherId = resolveTeacherId(username);
        String normalizedStudentId = safeTrim(studentId);
        String normalizedSubjectId = safeTrim(subjectId);
        String normalizedYear = safeTrim(namHoc);

        if (teacherId == null) {
            throw new RuntimeException("Không xác định được giáo viên từ tài khoản đăng nhập.");
        }
        if (normalizedStudentId == null || normalizedSubjectId == null || normalizedYear == null) {
            throw new RuntimeException("Thiếu thông tin để xóa nhóm điểm.");
        }

        Student student = studentDAO.findById(normalizedStudentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh cần thao tác."));
        ClassEntity classEntity = student.getLop();
        if (classEntity == null || safeTrim(classEntity.getIdLop()) == null) {
            throw new RuntimeException("Không xác định được lớp hiện tại của học sinh.");
        }
        String classId = classEntity.getIdLop();

        String homeroomClassId = safeTrim(scope == null ? null : scope.getClassId());
        if (homeroomClassId != null && homeroomClassId.equalsIgnoreCase(classId)) {
            throw new RuntimeException("Lớp chủ nhiệm chỉ được xem chi tiết điểm, không được nhập/sửa/xóa.");
        }

        long assignmentHk1 = scoreDAO.countTeachingAssignmentForScore(
                teacherId, normalizedSubjectId, normalizedYear, 1, classId
        );
        long assignmentHk2 = scoreDAO.countTeachingAssignmentForScore(
                teacherId, normalizedSubjectId, normalizedYear, 2, classId
        );
        if (assignmentHk1 > 0 || assignmentHk2 > 0) {
            return;
        }
        throw new RuntimeException("Bạn chưa được phân công dạy môn này ở lớp của học sinh, nên không thể xóa.");
    }

    @Transactional(readOnly = true)
    public CreateScopeData buildCreateScopeData(String username,
                                                TeacherHomeroomScope scope,
                                                String selectedClassId,
                                                String selectedSubjectId) {
        String teacherId = resolveTeacherId(username);
        String schoolYear = resolveSchoolYear(scope, teacherId);
        String homeroomClassId = safeTrim(scope == null ? null : scope.getClassId());

        if (teacherId == null || schoolYear == null) {
            return CreateScopeData.empty(schoolYear);
        }

        List<ClassFilterOption> classOptions = scoreDAO
                .findTeachingClassesByTeacherAndYear(teacherId, schoolYear)
                .stream()
                .map(this::mapClassOptionAsSubject)
                .filter(Objects::nonNull)
                .filter(item -> homeroomClassId == null || !homeroomClassId.equalsIgnoreCase(item.getId()))
                .toList();

        String normalizedClassId = safeTrim(selectedClassId);
        Set<String> classIds = classOptions.stream()
                .map(ClassFilterOption::getId)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedClassId == null || !classIds.contains(normalizedClassId.toLowerCase(Locale.ROOT))) {
            normalizedClassId = classOptions.isEmpty() ? null : classOptions.get(0).getId();
        }

        List<FilterOption> subjectOptions = normalizedClassId == null
                ? List.of()
                : scoreDAO.findTeachingSubjectsByTeacherClassAndYear(teacherId, normalizedClassId, schoolYear).stream()
                .map(this::mapFilterOption)
                .filter(Objects::nonNull)
                .toList();

        String normalizedSubjectId = safeTrim(selectedSubjectId);
        Set<String> subjectIds = subjectOptions.stream()
                .map(FilterOption::getId)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedSubjectId == null || !subjectIds.contains(normalizedSubjectId.toLowerCase(Locale.ROOT))) {
            normalizedSubjectId = subjectOptions.isEmpty() ? null : subjectOptions.get(0).getId();
        }

        return new CreateScopeData(
                teacherId,
                schoolYear,
                normalizedClassId,
                normalizedSubjectId,
                classOptions,
                subjectOptions
        );
    }

    @Transactional(readOnly = true)
    public boolean canUseSubjectClass(String username,
                                      TeacherHomeroomScope scope,
                                      String classId) {
        String normalized = safeTrim(classId);
        if (normalized == null) {
            return false;
        }
        CreateScopeData createScopeData = buildCreateScopeData(username, scope, normalized, null);
        return createScopeData.getClassOptions().stream()
                .anyMatch(item -> item.getId().equalsIgnoreCase(normalized));
    }

    @Transactional(readOnly = true)
    public String resolveTeacherId(String username) {
        String normalizedUsername = safeTrim(username);
        if (normalizedUsername == null) {
            return null;
        }
        String teacherId = safeTrim(scoreDAO.findTeacherIdByUsername(normalizedUsername));
        if (teacherId != null) {
            return teacherId.toUpperCase(Locale.ROOT);
        }
        if (normalizedUsername.matches("(?i)^gv[0-9]+$")) {
            return normalizedUsername.toUpperCase(Locale.ROOT);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public String resolveSchoolYearForTeacher(String username, TeacherHomeroomScope scope) {
        return resolveSchoolYear(scope, resolveTeacherId(username));
    }

    public String normalizeCreateSemester(String hocKy) {
        String value = safeTrim(hocKy);
        if ("0".equals(value) || "1".equals(value) || "2".equals(value)) {
            return value;
        }
        return "0";
    }

    private String resolveSchoolYear(TeacherHomeroomScope scope, String teacherId) {
        String homeroomYear = safeTrim(scope == null ? null : scope.getSchoolYear());
        if (homeroomYear != null) {
            return homeroomYear;
        }
        if (teacherId == null) {
            return null;
        }
        return safeTrim(scoreDAO.findLatestSchoolYearForTeacherScore(teacherId));
    }

    private List<ClassFilterOption> buildClassOptions(String teacherId,
                                                      String schoolYear,
                                                      String homeroomClassId,
                                                      String homeroomClassName) {
        LinkedHashMap<String, ClassFilterOption> options = new LinkedHashMap<>();

        if (homeroomClassId != null) {
            options.put(
                    homeroomClassId.toLowerCase(Locale.ROOT),
                    new ClassFilterOption(
                            homeroomClassId,
                            homeroomClassName == null ? homeroomClassId : homeroomClassName,
                            CLASS_SCOPE_HOMEROOM
                    )
            );
        }

        List<ClassFilterOption> subjectClasses = scoreDAO
                .findTeachingClassesByTeacherAndYear(teacherId, schoolYear)
                .stream()
                .map(this::mapClassOptionAsSubject)
                .filter(Objects::nonNull)
                .toList();

        for (ClassFilterOption item : subjectClasses) {
            String key = item.getId().toLowerCase(Locale.ROOT);
            if (!options.containsKey(key)) {
                options.put(key, item);
            }
        }
        return new ArrayList<>(options.values());
    }

    private ScoreStats calculateStats(List<ScoreRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ScoreStats(0, null, 0, 0, 0);
        }

        int homeroomCount = 0;
        int subjectCount = 0;
        int editableCount = 0;
        double homeroomSum = 0.0;
        int homeroomScoreCount = 0;

        for (ScoreRow row : rows) {
            if (row == null) {
                continue;
            }
            if (CLASS_SCOPE_HOMEROOM.equalsIgnoreCase(row.getClassScopeType())) {
                homeroomCount++;
                if (row.getTongKet() != null) {
                    homeroomSum += row.getTongKet();
                    homeroomScoreCount++;
                }
            } else {
                subjectCount++;
            }
            if (row.isCanManage()) {
                editableCount++;
            }
        }

        Double homeroomAverage = homeroomScoreCount == 0
                ? null
                : roundOneDecimal(homeroomSum / homeroomScoreCount);

        return new ScoreStats(rows.size(), homeroomAverage, homeroomCount, subjectCount, editableCount);
    }

    private PageData paginate(List<ScoreRow> rows, int requestedPage) {
        if (rows == null || rows.isEmpty()) {
            return new PageData(List.of(), 1, 1, 0, 0, 0);
        }
        int totalItems = rows.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int page = Math.min(Math.max(1, requestedPage), totalPages);
        int fromIndex = (page - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);

        List<ScoreRow> items = rows.subList(fromIndex, toIndex);
        return new PageData(
                items,
                page,
                totalPages,
                totalItems,
                fromIndex + 1,
                toIndex
        );
    }

    private ScoreRow mapScoreRow(Object[] row) {
        if (row == null || row.length < 13) {
            return null;
        }
        String classScopeType = safeTrim(asString(row, 11, CLASS_SCOPE_SUBJECT));
        if (classScopeType == null) {
            classScopeType = CLASS_SCOPE_SUBJECT;
        }
        return new ScoreRow(
                asString(row, 0, "-"),
                asString(row, 1, "-"),
                asString(row, 2, ""),
                asString(row, 3, "-"),
                asString(row, 4, "-"),
                asString(row, 5, "-"),
                asDouble(row, 6),
                asDouble(row, 7),
                asDouble(row, 8),
                null,
                null,
                null,
                asInteger(row, 9, null),
                asString(row, 10, "-"),
                classScopeType.toUpperCase(Locale.ROOT),
                asInteger(row, 12, 0) > 0
        );
    }

    private boolean isAnnualSearch(TeacherScoreSearch search) {
        return search != null && "0".equals(safeTrim(search.getHocKy()));
    }

    private List<ScoreRow> mergeAnnualRows(List<ScoreRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        LinkedHashMap<String, AnnualScoreAccumulator> grouped = new LinkedHashMap<>();
        for (ScoreRow row : rows) {
            if (row == null) {
                continue;
            }
            String key = buildAnnualKey(row);
            AnnualScoreAccumulator accumulator = grouped.computeIfAbsent(key, ignored -> new AnnualScoreAccumulator(row));
            accumulator.accept(row);
        }
        return grouped.values().stream()
                .map(this::toAnnualRow)
                .toList();
    }

    private String buildAnnualKey(ScoreRow row) {
        return safeKey(row.getStudentId())
                + "|"
                + safeKey(row.getSubjectId())
                + "|"
                + safeKey(row.getNamHoc());
    }

    private ScoreRow toAnnualRow(AnnualScoreAccumulator accumulator) {
        Double hocKy1 = accumulator.tongKetHocKy1;
        Double hocKy2 = accumulator.tongKetHocKy2;
        Double caNam = accumulator.tongKetCaNamFromData;
        if (caNam == null && hocKy1 != null && hocKy2 != null) {
            caNam = roundOneDecimal((hocKy1 + 2 * hocKy2) / 3.0);
        }

        return new ScoreRow(
                accumulator.studentId,
                accumulator.studentName,
                accumulator.classId,
                accumulator.className,
                accumulator.subjectId,
                accumulator.subjectName,
                null,
                null,
                caNam,
                hocKy1,
                hocKy2,
                caNam,
                0,
                accumulator.namHoc,
                accumulator.classScopeType,
                false
        );
    }

    private FilterOption mapFilterOption(Object[] row) {
        if (row == null || row.length < 2) {
            return null;
        }
        String id = safeTrim(row[0] == null ? null : row[0].toString());
        String name = safeTrim(row[1] == null ? null : row[1].toString());
        if (id == null) {
            return null;
        }
        return new FilterOption(id, name == null ? id : name);
    }

    private ClassFilterOption mapClassOptionAsSubject(Object[] row) {
        if (row == null || row.length < 2) {
            return null;
        }
        String id = safeTrim(row[0] == null ? null : row[0].toString());
        String name = safeTrim(row[1] == null ? null : row[1].toString());
        if (id == null) {
            return null;
        }
        return new ClassFilterOption(id, name == null ? id : name, CLASS_SCOPE_SUBJECT);
    }

    private TeacherScoreSearch normalizeSearch(TeacherScoreSearch rawSearch,
                                               List<ClassFilterOption> classOptions) {
        TeacherScoreSearch normalized = new TeacherScoreSearch();
        if (rawSearch == null) {
            return normalized;
        }
        normalized.setQ(safeTrim(rawSearch.getQ()));
        normalized.setKhoa(safeTrim(rawSearch.getKhoa()));
        normalized.setHocLuc(normalizeAcademicLevel(rawSearch.getHocLuc()));
        normalized.setMon(safeTrim(rawSearch.getMon()));
        normalized.setHocKy(normalizeHocKy(rawSearch.getHocKy()));
        normalized.setClassScope(normalizeClassScope(rawSearch.getClassScope()));
        normalized.setClassId(normalizeClassId(rawSearch.getClassId(), classOptions));
        normalized.setPage(normalizePage(rawSearch.getPage()));
        return normalized;
    }

    private String normalizeClassId(String classId, List<ClassFilterOption> classOptions) {
        String value = safeTrim(classId);
        if (value == null || classOptions == null || classOptions.isEmpty()) {
            return null;
        }
        for (ClassFilterOption option : classOptions) {
            if (option.getId().equalsIgnoreCase(value)) {
                return option.getId();
            }
        }
        return null;
    }

    private String normalizeHocKy(String hocKy) {
        String value = safeTrim(hocKy);
        if (value == null) {
            return null;
        }
        if ("0".equals(value) || "1".equals(value) || "2".equals(value)) {
            return value;
        }
        return null;
    }

    private String normalizeAcademicLevel(String hocLuc) {
        String value = safeTrim(hocLuc);
        if (value == null) {
            return null;
        }
        return switch (normalizeKey(value).replace('-', '_')) {
            case "gioi" -> ACADEMIC_EXCELLENT;
            case "kha" -> ACADEMIC_GOOD;
            case "trung binh", "trung_binh", "tb" -> ACADEMIC_AVERAGE;
            case "yeu" -> ACADEMIC_WEAK;
            case "kem" -> ACADEMIC_POOR;
            default -> null;
        };
    }

    private List<ScoreRow> applyAcademicLevelFilter(List<ScoreRow> rows, String hocLuc) {
        String normalizedLevel = normalizeAcademicLevel(hocLuc);
        if (normalizedLevel == null || rows == null || rows.isEmpty()) {
            return rows == null ? List.of() : rows;
        }
        return rows.stream()
                .filter(Objects::nonNull)
                .filter(row -> matchesAcademicLevel(row, normalizedLevel))
                .toList();
    }

    private boolean matchesAcademicLevel(ScoreRow row, String hocLuc) {
        String rowLevel = classifyAcademicLevel(resolveScoreForAcademicLevel(row));
        return rowLevel != null && rowLevel.equalsIgnoreCase(hocLuc);
    }

    private Double resolveScoreForAcademicLevel(ScoreRow row) {
        if (row == null) {
            return null;
        }
        if (row.getHocKy() != null && row.getHocKy() == 0) {
            if (row.getTongKetCaNam() != null) {
                return row.getTongKetCaNam();
            }
            return row.getTongKet();
        }
        return row.getTongKet();
    }

    private String classifyAcademicLevel(Double score) {
        if (score == null) {
            return null;
        }
        if (score >= 8.0d) {
            return ACADEMIC_EXCELLENT;
        }
        if (score >= 6.5d) {
            return ACADEMIC_GOOD;
        }
        if (score >= 5.0d) {
            return ACADEMIC_AVERAGE;
        }
        if (score >= 3.5d) {
            return ACADEMIC_WEAK;
        }
        return ACADEMIC_POOR;
    }

    public String resolveAcademicLevelLabel(String hocLuc) {
        String normalizedLevel = normalizeAcademicLevel(hocLuc);
        if (normalizedLevel == null) {
            return "Tất cả học lực";
        }
        return switch (normalizedLevel) {
            case ACADEMIC_EXCELLENT -> "Giỏi";
            case ACADEMIC_GOOD -> "Khá";
            case ACADEMIC_AVERAGE -> "Trung bình";
            case ACADEMIC_WEAK -> "Yếu";
            case ACADEMIC_POOR -> "Kém";
            default -> "Tất cả học lực";
        };
    }

    private String normalizeClassScope(String classScope) {
        String value = safeTrim(classScope);
        if (value == null) {
            return null;
        }
        if (CLASS_SCOPE_HOMEROOM.equalsIgnoreCase(value)) {
            return CLASS_SCOPE_HOMEROOM;
        }
        if (CLASS_SCOPE_SUBJECT.equalsIgnoreCase(value)) {
            return CLASS_SCOPE_SUBJECT;
        }
        return null;
    }

    private Integer parseHocKy(String hocKy) {
        String value = safeTrim(hocKy);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Integer> resolveSemesters(String hocKy) {
        Integer semester = parseHocKy(hocKy);
        if (semester == null || semester <= 0) {
            return List.of(1, 2);
        }
        if (semester == 1 || semester == 2) {
            return List.of(semester);
        }
        return List.of(1, 2);
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String asString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        String value = row[index].toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private Integer asInteger(Object[] row, int index, Integer fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        Object value = row[index];
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private Double asDouble(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }
        Object value = row[index];
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double roundOneDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String safeKey(String value) {
        String normalized = safeTrim(value);
        return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
    }

    private int resolveFrequentColumns(String subjectId, String subjectName, String description) {
        Integer configured = extractFrequentColumnsFromDescription(description);
        if (configured != null && configured >= 2 && configured <= 4) {
            return configured;
        }

        String normalizedSubjectId = safeTrim(subjectId);
        if (normalizedSubjectId != null) {
            Integer fallbackById = DEFAULT_FREQUENT_SCORE_RULES.get(normalizedSubjectId.toLowerCase(Locale.ROOT));
            if (fallbackById != null) {
                return fallbackById;
            }
        }
        return resolveDefaultFrequentColumnsByName(subjectName);
    }

    private Integer extractFrequentColumnsFromDescription(String description) {
        String normalizedDescription = safeTrim(description);
        if (normalizedDescription == null) {
            return null;
        }
        String[] lines = normalizedDescription.split("\\R");
        for (String line : lines) {
            if (line == null || !line.contains(":")) {
                continue;
            }
            String[] pair = line.split(":", 2);
            String key = normalizeKey(pair[0]);
            if (!META_TX_KEY.equals(key)) {
                continue;
            }
            try {
                int parsed = Integer.parseInt(pair.length > 1 ? pair[1].trim() : "");
                if (parsed >= 2 && parsed <= 4) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private int resolveDefaultFrequentColumnsByName(String subjectName) {
        String normalized = normalizeKey(subjectName);
        for (var entry : DEFAULT_FREQUENT_SCORE_RULES.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return DEFAULT_FREQUENT_COLUMNS;
    }

    private String normalizeKey(String value) {
        String normalized = safeTrim(value);
        if (normalized == null) {
            return "";
        }
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lowerCase.length());
        for (int index = 0; index < lowerCase.length(); index++) {
            char current = lowerCase.charAt(index);
            builder.append(switch (current) {
                case 'à', 'á', 'ạ', 'ả', 'ã', 'ă', 'ằ', 'ắ', 'ặ', 'ẳ', 'ẵ', 'â', 'ầ', 'ấ', 'ậ', 'ẩ', 'ẫ' -> 'a';
                case 'è', 'é', 'ẹ', 'ẻ', 'ẽ', 'ê', 'ề', 'ế', 'ệ', 'ể', 'ễ' -> 'e';
                case 'ì', 'í', 'ị', 'ỉ', 'ĩ' -> 'i';
                case 'ò', 'ó', 'ọ', 'ỏ', 'õ', 'ô', 'ồ', 'ố', 'ộ', 'ổ', 'ỗ', 'ơ', 'ờ', 'ớ', 'ợ', 'ở', 'ỡ' -> 'o';
                case 'ù', 'ú', 'ụ', 'ủ', 'ũ', 'ư', 'ừ', 'ứ', 'ự', 'ử', 'ữ' -> 'u';
                case 'ỳ', 'ý', 'ỵ', 'ỷ', 'ỹ' -> 'y';
                case 'đ' -> 'd';
                default -> current;
            });
        }
        return builder.toString().replaceAll("\\s+", " ").trim();
    }

    private static LinkedHashMap<String, Integer> buildFrequentScoreRules() {
        LinkedHashMap<String, Integer> rules = new LinkedHashMap<>();
        rules.put("ngu van", 4);
        rules.put("toan", 4);
        rules.put("ngoai ngu 1", 4);
        rules.put("lich su", 3);
        rules.put("giao duc quoc phong va an ninh", 2);
        rules.put("dia li", 3);
        rules.put("giao duc kinh te va phap luat", 3);
        rules.put("vat li", 3);
        rules.put("hoa hoc", 3);
        rules.put("sinh hoc", 3);
        rules.put("cong nghe", 3);
        rules.put("tin hoc", 3);
        rules.put("hoat dong trai nghiem huong nghiep", 2);
        rules.put("noi dung giao duc cua dia phuong", 2);
        return rules;
    }

    private record PageData(
            List<ScoreRow> items,
            int page,
            int totalPages,
            int totalItems,
            int fromRecord,
            int toRecord
    ) {
    }

    public static class FilterOption {
        private final String id;
        private final String name;

        public FilterOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class ClassFilterOption {
        private final String id;
        private final String name;
        private final String scopeType;

        public ClassFilterOption(String id, String name, String scopeType) {
            this.id = id;
            this.name = name;
            this.scopeType = scopeType;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getScopeType() {
            return scopeType;
        }

        public boolean isHomeroom() {
            return CLASS_SCOPE_HOMEROOM.equalsIgnoreCase(scopeType);
        }
    }

    public static class CreateScopeData {
        private final String teacherId;
        private final String schoolYear;
        private final String selectedClassId;
        private final String selectedSubjectId;
        private final List<ClassFilterOption> classOptions;
        private final List<FilterOption> subjectOptions;

        public CreateScopeData(String teacherId,
                               String schoolYear,
                               String selectedClassId,
                               String selectedSubjectId,
                               List<ClassFilterOption> classOptions,
                               List<FilterOption> subjectOptions) {
            this.teacherId = teacherId;
            this.schoolYear = schoolYear;
            this.selectedClassId = selectedClassId;
            this.selectedSubjectId = selectedSubjectId;
            this.classOptions = classOptions;
            this.subjectOptions = subjectOptions;
        }

        public static CreateScopeData empty(String schoolYear) {
            return new CreateScopeData(
                    null,
                    schoolYear,
                    null,
                    null,
                    List.of(),
                    List.of()
            );
        }

        public String getTeacherId() {
            return teacherId;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public String getSelectedClassId() {
            return selectedClassId;
        }

        public String getSelectedSubjectId() {
            return selectedSubjectId;
        }

        public String getSelectedSubjectName() {
            if (selectedSubjectId == null || subjectOptions == null || subjectOptions.isEmpty()) {
                return null;
            }
            for (FilterOption item : subjectOptions) {
                if (item != null
                        && item.getId() != null
                        && item.getId().equalsIgnoreCase(selectedSubjectId)) {
                    return item.getName();
                }
            }
            return selectedSubjectId;
        }

        public String getTeachingSubjectDisplay() {
            String selectedName = getSelectedSubjectName();
            if (selectedName != null) {
                return selectedName;
            }
            if (subjectOptions == null || subjectOptions.isEmpty()) {
                return null;
            }
            return subjectOptions.stream()
                    .filter(Objects::nonNull)
                    .map(FilterOption::getName)
                    .filter(Objects::nonNull)
                    .filter(name -> !name.isBlank())
                    .distinct()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse(null);
        }

        public List<ClassFilterOption> getClassOptions() {
            return classOptions;
        }

        public List<FilterOption> getSubjectOptions() {
            return subjectOptions;
        }
    }

    public static class ScoreRow {
        private final String studentId;
        private final String studentName;
        private final String classId;
        private final String className;
        private final String subjectId;
        private final String subjectName;
        private final Double diemGiuaKy;
        private final Double diemCuoiKy;
        private final Double tongKet;
        private final Double tongKetHocKy1;
        private final Double tongKetHocKy2;
        private final Double tongKetCaNam;
        private final Integer hocKy;
        private final String namHoc;
        private final String classScopeType;
        private final boolean canEdit;

        public ScoreRow(String studentId,
                        String studentName,
                        String classId,
                        String className,
                        String subjectId,
                        String subjectName,
                        Double diemGiuaKy,
                        Double diemCuoiKy,
                        Double tongKet,
                        Double tongKetHocKy1,
                        Double tongKetHocKy2,
                        Double tongKetCaNam,
                        Integer hocKy,
                        String namHoc,
                        String classScopeType,
                        boolean canEdit) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.classId = classId;
            this.className = className;
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.diemGiuaKy = diemGiuaKy;
            this.diemCuoiKy = diemCuoiKy;
            this.tongKet = tongKet;
            this.tongKetHocKy1 = tongKetHocKy1;
            this.tongKetHocKy2 = tongKetHocKy2;
            this.tongKetCaNam = tongKetCaNam;
            this.hocKy = hocKy;
            this.namHoc = namHoc;
            this.classScopeType = classScopeType;
            this.canEdit = canEdit;
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
            if (classId == null || classId.isBlank()) {
                return className;
            }
            if (className == null || className.isBlank() || className.equalsIgnoreCase(classId)) {
                return classId;
            }
            return classId + " - " + className;
        }

        public String getSubjectId() {
            return subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getDiemGiuaKyDisplay() {
            return formatScore(diemGiuaKy);
        }

        public String getDiemCuoiKyDisplay() {
            return formatScore(diemCuoiKy);
        }

        public Double getTongKet() {
            return tongKet;
        }

        public String getTongKetDisplay() {
            return formatScore(tongKet);
        }

        public Double getTongKetHocKy1() {
            return tongKetHocKy1;
        }

        public String getTongKetHocKy1Display() {
            return formatScore(tongKetHocKy1);
        }

        public Double getTongKetHocKy2() {
            return tongKetHocKy2;
        }

        public String getTongKetHocKy2Display() {
            return formatScore(tongKetHocKy2);
        }

        public Double getTongKetCaNam() {
            return tongKetCaNam;
        }

        public String getTongKetCaNamDisplay() {
            return formatScore(tongKetCaNam);
        }

        public Integer getHocKy() {
            return hocKy;
        }

        public String getHocKyDisplay() {
            if (hocKy == null) {
                return "-";
            }
            if (hocKy != null && hocKy == 0) {
                return "C\u1EA3 n\u0103m";
            }
            if (hocKy == 1) {
                return "Học kỳ I";
            }
            if (hocKy == 2) {
                return "Học kỳ II";
            }
            return "Học kỳ " + hocKy;
        }

        public String getNamHoc() {
            return namHoc;
        }

        public String getClassScopeDisplay() {
            if (CLASS_SCOPE_HOMEROOM.equalsIgnoreCase(classScopeType)) {
                return "Lớp chủ nhiệm";
            }
            return "Lớp bộ môn";
        }

        public String getClassScopeType() {
            return classScopeType;
        }

        public String getClassScopeBadge() {
            if (CLASS_SCOPE_HOMEROOM.equalsIgnoreCase(classScopeType)) {
                return "scope-homeroom";
            }
            return "scope-subject";
        }

        public boolean isCanEdit() {
            return canEdit;
        }

        public boolean isCanManage() {
            return canEdit && CLASS_SCOPE_SUBJECT.equalsIgnoreCase(classScopeType);
        }

        private String formatScore(Double value) {
            if (value == null) {
                return "-";
            }
            BigDecimal rounded = BigDecimal.valueOf(value)
                    .setScale(1, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            return rounded.toPlainString();
        }
    }

    private static final class AnnualScoreAccumulator {
        private final String studentId;
        private final String studentName;
        private final String classId;
        private final String className;
        private final String subjectId;
        private final String subjectName;
        private final String namHoc;
        private String classScopeType;
        private Double tongKetHocKy1;
        private Double tongKetHocKy2;
        private Double tongKetCaNamFromData;

        private AnnualScoreAccumulator(ScoreRow baseRow) {
            this.studentId = baseRow.getStudentId();
            this.studentName = baseRow.getStudentName();
            this.classId = baseRow.getClassId();
            this.className = baseRow.className;
            this.subjectId = baseRow.getSubjectId();
            this.subjectName = baseRow.getSubjectName();
            this.namHoc = baseRow.getNamHoc();
            this.classScopeType = baseRow.getClassScopeType();
        }

        private void accept(ScoreRow row) {
            Integer semester = row.getHocKy();
            if (semester == null) {
                return;
            }
            if (CLASS_SCOPE_SUBJECT.equalsIgnoreCase(row.getClassScopeType())) {
                classScopeType = CLASS_SCOPE_SUBJECT;
            }
            if (semester == 1) {
                if (tongKetHocKy1 == null) {
                    tongKetHocKy1 = row.getTongKet();
                }
                return;
            }
            if (semester == 2) {
                if (tongKetHocKy2 == null) {
                    tongKetHocKy2 = row.getTongKet();
                }
                return;
            }
            if (semester == 0 && tongKetCaNamFromData == null) {
                tongKetCaNamFromData = row.getTongKet();
            }
        }
    }

    public static class ScoreStats {
        private final int totalRows;
        private final Double homeroomAverage;
        private final int homeroomRows;
        private final int subjectRows;
        private final int editableRows;

        public ScoreStats(int totalRows,
                          Double homeroomAverage,
                          int homeroomRows,
                          int subjectRows,
                          int editableRows) {
            this.totalRows = totalRows;
            this.homeroomAverage = homeroomAverage;
            this.homeroomRows = homeroomRows;
            this.subjectRows = subjectRows;
            this.editableRows = editableRows;
        }

        public int getTotalRows() {
            return totalRows;
        }

        public Double getHomeroomAverage() {
            return homeroomAverage;
        }

        public int getHomeroomRows() {
            return homeroomRows;
        }

        public int getSubjectRows() {
            return subjectRows;
        }

        public int getEditableRows() {
            return editableRows;
        }

        public String getHomeroomAverageDisplay() {
            if (homeroomAverage == null) {
                return "--";
            }
            return BigDecimal.valueOf(homeroomAverage)
                    .setScale(1, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString();
        }
    }

    public static class ScoreDashboardData {
        private final TeacherScoreSearch search;
        private final String schoolYear;
        private final String teacherId;
        private final List<FilterOption> subjectOptions;
        private final List<FilterOption> teachingSubjects;
        private final List<ClassFilterOption> classOptions;
        private final List<ScoreRow> rows;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final int fromRecord;
        private final int toRecord;
        private final ScoreStats stats;

        public ScoreDashboardData(TeacherScoreSearch search,
                                  String schoolYear,
                                  String teacherId,
                                  List<FilterOption> subjectOptions,
                                  List<FilterOption> teachingSubjects,
                                  List<ClassFilterOption> classOptions,
                                  List<ScoreRow> rows,
                                  int page,
                                  int totalPages,
                                  int totalItems,
                                  int fromRecord,
                                  int toRecord,
                                  ScoreStats stats) {
            this.search = search;
            this.schoolYear = schoolYear;
            this.teacherId = teacherId;
            this.subjectOptions = subjectOptions;
            this.teachingSubjects = teachingSubjects;
            this.classOptions = classOptions;
            this.rows = rows;
            this.page = page;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.fromRecord = fromRecord;
            this.toRecord = toRecord;
            this.stats = stats;
        }

        public static ScoreDashboardData empty(TeacherScoreSearch search, String schoolYear) {
            return new ScoreDashboardData(
                    search == null ? new TeacherScoreSearch() : search,
                    schoolYear,
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    1,
                    1,
                    0,
                    0,
                    0,
                    new ScoreStats(0, null, 0, 0, 0)
            );
        }

        public TeacherScoreSearch getSearch() {
            return search;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public String getTeacherId() {
            return teacherId;
        }

        public List<FilterOption> getSubjectOptions() {
            return subjectOptions;
        }

        public List<FilterOption> getTeachingSubjects() {
            return teachingSubjects;
        }

        public List<ClassFilterOption> getClassOptions() {
            return classOptions;
        }

        public List<ScoreRow> getRows() {
            return rows;
        }

        public int getPage() {
            return page;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public int getFromRecord() {
            return fromRecord;
        }

        public int getToRecord() {
            return toRecord;
        }

        public ScoreStats getStats() {
            return stats;
        }

        public List<Integer> getPageNumbers() {
            if (totalPages <= 0) {
                return Collections.singletonList(1);
            }
            List<Integer> pages = new ArrayList<>(totalPages);
            for (int index = 1; index <= totalPages; index++) {
                pages.add(index);
            }
            return pages;
        }
    }
}
