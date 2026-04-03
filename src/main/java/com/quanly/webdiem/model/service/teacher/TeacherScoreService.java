package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.dao.ScoreDAO;
import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.search.TeacherScoreSearch;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class TeacherScoreService {

    private static final int PAGE_SIZE = 10;
    private static final String CLASS_SCOPE_HOMEROOM = "HOMEROOM";
    private static final String CLASS_SCOPE_SUBJECT = "SUBJECT";

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
        String teacherId = resolveTeacherId(username);
        TeacherScoreSearch search = normalizeSearch(rawSearch);
        String homeroomClassId = safeTrim(scope == null ? null : scope.getClassId());
        String schoolYear = resolveSchoolYear(scope, teacherId);

        if (teacherId == null || schoolYear == null) {
            return ScoreDashboardData.empty(search, schoolYear);
        }

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
                        parseHocKy(search.getHocKy()),
                        normalizeClassScope(search.getClassScope())
                ).stream()
                .map(this::mapScoreRow)
                .filter(Objects::nonNull)
                .toList();

        ScoreStats stats = calculateStats(allRows);
        PageData pageData = paginate(allRows, normalizePage(search.getPage()));

        return new ScoreDashboardData(
                search,
                schoolYear,
                teacherId,
                visibleSubjectOptions,
                teachingSubjectOptions,
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

        String homeroomClassId = safeTrim(scope == null ? null : scope.getClassId());
        if (homeroomClassId != null && homeroomClassId.equalsIgnoreCase(classId)) {
            throw new RuntimeException("Bạn là GVCN lớp này nhưng không được phân công dạy môn đã chọn, nên không thể nhập/sửa điểm.");
        }
        throw new RuntimeException("Bạn chưa được phân công dạy môn này ở lớp của học sinh, nên không thể nhập/sửa điểm.");
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
            if (row.isCanEdit()) {
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
                asInteger(row, 9, null),
                asString(row, 10, "-"),
                classScopeType.toUpperCase(Locale.ROOT),
                asInteger(row, 12, 0) > 0
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

    private TeacherScoreSearch normalizeSearch(TeacherScoreSearch rawSearch) {
        TeacherScoreSearch normalized = new TeacherScoreSearch();
        if (rawSearch == null) {
            return normalized;
        }
        normalized.setQ(safeTrim(rawSearch.getQ()));
        normalized.setMon(safeTrim(rawSearch.getMon()));
        normalized.setHocKy(normalizeHocKy(rawSearch.getHocKy()));
        normalized.setClassScope(normalizeClassScope(rawSearch.getClassScope()));
        normalized.setPage(normalizePage(rawSearch.getPage()));
        return normalized;
    }

    private String normalizeHocKy(String hocKy) {
        String value = safeTrim(hocKy);
        if (value == null) {
            return null;
        }
        if ("1".equals(value) || "2".equals(value)) {
            return value;
        }
        return null;
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

        public Integer getHocKy() {
            return hocKy;
        }

        public String getHocKyDisplay() {
            if (hocKy == null) {
                return "-";
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
