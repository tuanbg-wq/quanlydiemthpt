package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.ConductEventUpsertRequest;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateFilter;
import com.quanly.webdiem.model.service.admin.ConductRewardCreatePageData;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateRequest;
import com.quanly.webdiem.model.service.admin.ConductStudentCandidate;
import com.quanly.webdiem.model.service.admin.report.AdminReportExportHistoryService;
import com.quanly.webdiem.model.service.admin.report.AdminReportType;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Service
public class TeacherConductService {

    private static final int TEACHER_ACTIVITY_HISTORY_LIMIT = 40;
    private static final int TEACHER_EXPORT_HISTORY_LIMIT = 40;

    private final ConductManagementService conductManagementService;
    private final TeacherStudentScopeService scopeService;
    private final ActivityLogService activityLogService;
    private final AdminReportExportHistoryService reportExportHistoryService;

    public TeacherConductService(ConductManagementService conductManagementService,
                                 TeacherStudentScopeService scopeService,
                                 ActivityLogService activityLogService,
                                 AdminReportExportHistoryService reportExportHistoryService) {
        this.conductManagementService = conductManagementService;
        this.scopeService = scopeService;
        this.activityLogService = activityLogService;
        this.reportExportHistoryService = reportExportHistoryService;
    }

    @Transactional(readOnly = true)
    public TeacherConductDashboardData loadDashboard(String username, TeacherHomeroomScope scope, ConductSearch search) {
        ScopeMetadata scopeMetadata = buildScopeMetadata(scope);
        if (!scopeMetadata.hasHomeroomClass()) {
            return TeacherConductDashboardData.empty(search);
        }

        ConductSearch scopedSearch = applyScope(search, scopeMetadata);
        return new TeacherConductDashboardData(
                scopedSearch,
                conductManagementService.search(scopedSearch),
                conductManagementService.getStats(scopedSearch),
                scopeMetadata.grades(),
                scopeMetadata.classOptions(),
                scopeMetadata.courseOptions(),
                activityLogService.getRecentConductActivitiesByClassId(
                        scopeMetadata.classId(),
                        null,
                        null,
                        TEACHER_ACTIVITY_HISTORY_LIMIT
                ),
                getOwnExportHistory(username)
        );
    }

    @Transactional(readOnly = true)
    public ConductRewardCreatePageData getCreatePageData(TeacherHomeroomScope scope,
                                                         ConductRewardCreateFilter filter) {
        ScopeMetadata scopeMetadata = requireScopeMetadata(scope);
        ConductRewardCreateFilter scopedFilter = applyScope(filter, scopeMetadata);
        ConductRewardCreatePageData basePageData = runWithNormalizedErrors(
                () -> conductManagementService.getRewardCreatePageData(scopedFilter)
        );

        ConductStudentCandidate selectedStudent = basePageData.getSelectedStudent();
        if (!isCandidateInScope(selectedStudent, scopeMetadata.classId())) {
            selectedStudent = null;
            scopedFilter.setStudentId(null);
        }

        List<ConductStudentCandidate> studentCandidates = basePageData.getStudentCandidates().stream()
                .filter(candidate -> isCandidateInScope(candidate, scopeMetadata.classId()))
                .toList();

        return new ConductRewardCreatePageData(
                scopedFilter,
                scopeMetadata.grades(),
                scopeMetadata.classOptions(),
                scopeMetadata.courseOptions(),
                studentCandidates,
                selectedStudent
        );
    }

    @Transactional(readOnly = true)
    public List<ConductStudentCandidate> suggestStudents(TeacherHomeroomScope scope, String q) {
        ScopeMetadata scopeMetadata = requireScopeMetadata(scope);
        if (safeTrim(q) == null) {
            return List.of();
        }
        return runWithNormalizedErrors(() -> conductManagementService.suggestStudentsForReward(
                q,
                scopeMetadata.grade(),
                scopeMetadata.classId(),
                scopeMetadata.courseId()
        )).stream()
                .filter(candidate -> isCandidateInScope(candidate, scopeMetadata.classId()))
                .toList();
    }

    @Transactional
    public void createReward(TeacherHomeroomScope scope, ConductRewardCreateRequest request) {
        assertStudentInScope(scope, request == null ? null : request.getStudentId());
        runWithNormalizedErrors(() -> conductManagementService.createReward(request));
    }

    @Transactional
    public void createDiscipline(TeacherHomeroomScope scope, ConductRewardCreateRequest request) {
        assertStudentInScope(scope, request == null ? null : request.getStudentId());
        runWithNormalizedErrors(() -> conductManagementService.createDiscipline(request));
    }

    @Transactional(readOnly = true)
    public ConductManagementService.ConductRow getEventDetailInScope(TeacherHomeroomScope scope, Long eventId) {
        ConductManagementService.ConductRow detail = runWithNormalizedErrors(
                () -> conductManagementService.getEventDetail(eventId)
        );
        assertStudentInScope(scope, detail == null ? null : detail.getIdHocSinh());
        return detail;
    }

    @Transactional(readOnly = true)
    public ConductEventUpsertRequest getEditDataInScope(TeacherHomeroomScope scope, Long eventId) {
        ConductManagementService.ConductRow detail = getEventDetailInScope(scope, eventId);
        ConductEventUpsertRequest request = runWithNormalizedErrors(
                () -> conductManagementService.getEditData(detail.getEventId())
        );
        request.setStudentId(detail.getIdHocSinh());
        return request;
    }

    @Transactional
    public void updateEvent(TeacherHomeroomScope scope, ConductEventUpsertRequest request) {
        Long eventId = request == null ? null : request.getEventId();
        ConductManagementService.ConductRow detail = getEventDetailInScope(scope, eventId);
        assertStudentInScope(scope, detail == null ? null : detail.getIdHocSinh());
        runWithNormalizedErrors(() -> conductManagementService.updateEvent(request));
    }

    @Transactional
    public void deleteEvent(TeacherHomeroomScope scope, Long eventId) {
        ConductManagementService.ConductRow detail = getEventDetailInScope(scope, eventId);
        assertStudentInScope(scope, detail == null ? null : detail.getIdHocSinh());
        runWithNormalizedErrors(() -> conductManagementService.deleteEvent(eventId));
    }

    @Transactional(readOnly = true)
    public ConductManagementService.ConductRow getLatestEventByStudentAndTypeInScope(TeacherHomeroomScope scope,
                                                                                     String studentId,
                                                                                     String loai) {
        assertStudentInScope(scope, studentId);
        ConductManagementService.ConductRow detail = runWithNormalizedErrors(
                () -> conductManagementService.getLatestEventByStudentAndType(studentId, loai)
        );
        if (detail == null) {
            return null;
        }
        assertStudentInScope(scope, detail.getIdHocSinh());
        return detail;
    }

    @Transactional(readOnly = true)
    public List<ConductManagementService.ConductRow> getRowsForExport(TeacherHomeroomScope scope, ConductSearch search) {
        ScopeMetadata scopeMetadata = requireScopeMetadata(scope);
        return runWithNormalizedErrors(() -> conductManagementService.getRowsForExport(applyScope(search, scopeMetadata)));
    }

    @Transactional(readOnly = true)
    public ConductSearch getScopedSearchForExport(TeacherHomeroomScope scope, ConductSearch search) {
        ScopeMetadata scopeMetadata = requireScopeMetadata(scope);
        return applyScope(search, scopeMetadata);
    }

    public void appendExportHistory(TeacherHomeroomScope scope,
                                    String username,
                                    String format,
                                    long totalRows,
                                    ConductSearch scopedSearch) {
        String resolvedUsername = safeTrim(username);
        if (resolvedUsername == null) {
            return;
        }
        reportExportHistoryService.append(
                AdminReportType.REWARD_DISCIPLINE,
                format,
                resolvedUsername,
                "GVCN",
                totalRows,
                buildFilterSummary(scope, scopedSearch)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminReportExportHistoryService.HistoryItem> getOwnExportHistory(String username) {
        String resolvedUsername = safeTrim(username);
        if (resolvedUsername == null) {
            return List.of();
        }
        try {
            return reportExportHistoryService.getLatestByActorAndType(
                    resolvedUsername,
                    "GVCN",
                    AdminReportType.REWARD_DISCIPLINE.getCode(),
                    TEACHER_EXPORT_HISTORY_LIMIT
            );
        } catch (RuntimeException ex) {
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public String suggestRewardDecisionNumber() {
        return runWithNormalizedErrors(
                () -> conductManagementService.suggestDecisionNumber(ConductManagementService.LOAI_KHEN_THUONG, "GVCN-QĐ-KT")
        );
    }

    @Transactional(readOnly = true)
    public String suggestDisciplineDecisionNumber() {
        return runWithNormalizedErrors(
                () -> conductManagementService.suggestDecisionNumber(ConductManagementService.LOAI_KY_LUAT, "GVCN-QĐ-KL")
        );
    }

    public void applyDefaultRewardDecisionNumber(ConductRewardCreateRequest request) {
        if (request == null || safeTrim(request.getSoQuyetDinh()) != null) {
            return;
        }
        request.setSoQuyetDinh(suggestRewardDecisionNumber());
    }

    public void applyDefaultDisciplineDecisionNumber(ConductRewardCreateRequest request) {
        if (request == null || safeTrim(request.getSoQuyetDinh()) != null) {
            return;
        }
        request.setSoQuyetDinh(suggestDisciplineDecisionNumber());
    }

    private ScopeMetadata requireScopeMetadata(TeacherHomeroomScope scope) {
        ScopeMetadata metadata = buildScopeMetadata(scope);
        if (!metadata.hasHomeroomClass()) {
            throw new RuntimeException("Tài khoản chưa được phân công lớp chủ nhiệm.");
        }
        return metadata;
    }

    private ScopeMetadata buildScopeMetadata(TeacherHomeroomScope scope) {
        if (!scopeService.hasHomeroomClass(scope)) {
            return ScopeMetadata.empty();
        }

        String classId = safeTrim(scope.getClassId());
        String className = safeTrim(scope.getClassName());
        String schoolYear = safeTrim(scope.getSchoolYear());
        String grade = null;
        String courseId = null;
        String courseName = null;

        try {
            ClassEntity homeroomClass = scopeService.getHomeroomClassOrThrow(scope);
            if (homeroomClass != null) {
                className = firstNonBlank(className, safeTrim(homeroomClass.getMaVaTenLop()), safeTrim(homeroomClass.getTenLop()), classId);
                if (homeroomClass.getKhoi() != null) {
                    grade = String.valueOf(homeroomClass.getKhoi());
                }
                if (homeroomClass.getKhoaHoc() != null) {
                    courseId = safeTrim(homeroomClass.getKhoaHoc().getIdKhoa());
                    courseName = safeTrim(homeroomClass.getKhoaHoc().getTenKhoa());
                }
            }
        } catch (RuntimeException ignored) {
            className = firstNonBlank(className, classId);
        }

        List<String> grades = grade == null ? List.of() : List.of(grade);
        List<ConductManagementService.FilterOption> classOptions = classId == null
                ? List.of()
                : List.of(new ConductManagementService.FilterOption(classId, firstNonBlank(className, classId)));
        List<ConductManagementService.FilterOption> courseOptions = courseId == null
                ? List.of()
                : List.of(new ConductManagementService.FilterOption(
                courseId,
                courseName == null ? courseId : courseId + " (" + courseName + ")"
        ));

        return new ScopeMetadata(classId, className, schoolYear, grade, courseId, grades, classOptions, courseOptions);
    }

    private ConductSearch applyScope(ConductSearch search, ScopeMetadata scopeMetadata) {
        ConductSearch scopedSearch = new ConductSearch();
        if (search != null) {
            scopedSearch.setQ(search.getQ());
            scopedSearch.setLoai(search.getLoai());
            scopedSearch.setPage(search.getPage());
        }
        scopedSearch.setKhoi(scopeMetadata.grade());
        scopedSearch.setLop(scopeMetadata.classId());
        scopedSearch.setKhoa(scopeMetadata.courseId());
        return scopedSearch;
    }

    private ConductRewardCreateFilter applyScope(ConductRewardCreateFilter filter, ScopeMetadata scopeMetadata) {
        ConductRewardCreateFilter scopedFilter = new ConductRewardCreateFilter();
        if (filter != null) {
            scopedFilter.setQ(filter.getQ());
            scopedFilter.setStudentId(filter.getStudentId());
        }
        scopedFilter.setKhoi(scopeMetadata.grade());
        scopedFilter.setLop(scopeMetadata.classId());
        scopedFilter.setKhoa(scopeMetadata.courseId());
        return scopedFilter;
    }

    private void assertStudentInScope(TeacherHomeroomScope scope, String studentId) {
        if (!scopeService.hasHomeroomClass(scope)) {
            throw new RuntimeException("Tài khoản chưa được phân công lớp chủ nhiệm.");
        }
        scopeService.getStudentInScopeOrThrow(studentId, scope);
    }

    private boolean isCandidateInScope(ConductStudentCandidate candidate, String classId) {
        if (candidate == null) {
            return false;
        }
        String candidateClassId = safeTrim(candidate.getClassId());
        String scopedClassId = safeTrim(classId);
        return scopedClassId != null && scopedClassId.equalsIgnoreCase(candidateClassId);
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

    private String buildFilterSummary(TeacherHomeroomScope scope, ConductSearch search) {
        List<String> parts = new ArrayList<>();

        String schoolYear = safeTrim(scope == null ? null : scope.getSchoolYear());
        if (schoolYear != null) {
            parts.add("Năm học: " + schoolYear);
        }

        String className = safeTrim(scope == null ? null : scope.getClassName());
        if (className != null) {
            parts.add("Lớp chủ nhiệm: " + className);
        }

        String keyword = safeTrim(search == null ? null : search.getQ());
        if (keyword != null) {
            parts.add("Từ khóa: " + keyword);
        }

        String grade = safeTrim(search == null ? null : search.getKhoi());
        if (grade != null) {
            parts.add("Khối: " + grade);
        }

        String classId = safeTrim(search == null ? null : search.getLop());
        if (classId != null) {
            parts.add("Lớp lọc: " + classId);
        }

        String courseId = safeTrim(search == null ? null : search.getKhoa());
        if (courseId != null) {
            parts.add("Khóa: " + courseId);
        }

        String type = safeTrim(search == null ? null : search.getLoai());
        if (type != null) {
            parts.add("Loại: " + resolveConductTypeLabel(type));
        }

        if (parts.isEmpty()) {
            return "Không dùng bộ lọc";
        }
        return String.join(" | ", parts);
    }

    private String resolveConductTypeLabel(String type) {
        if (type == null) {
            return "";
        }
        if (ConductManagementService.LOAI_KHEN_THUONG.equalsIgnoreCase(type)) {
            return "Khen thưởng";
        }
        if (ConductManagementService.LOAI_KY_LUAT.equalsIgnoreCase(type)) {
            return "Kỷ luật";
        }
        return type;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private <T> T runWithNormalizedErrors(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException ex) {
            throw normalizeRuntimeException(ex);
        }
    }

    private void runWithNormalizedErrors(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            throw normalizeRuntimeException(ex);
        }
    }

    private RuntimeException normalizeRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex;
        }
        String normalized = message
                .replace("KhĂ´ng", "Không")
                .replace("Vui lĂ²ng", "Vui lòng")
                .replace("TĂ i khoáº£n", "Tài khoản")
                .replace("chá»n", "chọn")
                .replace("há»c sinh", "học sinh")
                .replace("trÆ°á»›c", "trước")
                .replace("lÆ°u", "lưu")
                .replace("Ná»™i dung", "Nội dung")
                .replace("ká»· luáº­t", "kỷ luật")
                .replace("thÆ°á»Ÿng", "thưởng")
                .replace("Ä‘Æ°á»£c", "được")
                .replace("Ä‘á»ƒ", "để")
                .replace("trá»‘ng", "trống")
                .replace("ngĂ y", "ngày")
                .replace("vi pháº¡m", "vi phạm")
                .replace("NgĂ y", "Ngày")
                .replace("thá»ƒ", "thể")
                .replace("dá»¯ liá»‡u", "dữ liệu")
                .replace("tĂ¬m tháº¥y", "tìm thấy")
                .replace("báº£n ghi", "bản ghi")
                .replace("khen thÆ°á»Ÿng", "khen thưởng")
                .replace("giáº£m", "giảm")
                .replace("chá»§ nhiá»‡m", "chủ nhiệm")
                .replace("hoáº·c", "hoặc")
                .replace("Ä‘Æ°á»£c", "được")
                .replace("Ä‘á»‹nh", "định")
                .replace("Ä", "Đ");
        return normalized.equals(message) ? ex : new RuntimeException(normalized, ex);
    }

    private record ScopeMetadata(String classId,
                                 String className,
                                 String schoolYear,
                                 String grade,
                                 String courseId,
                                 List<String> grades,
                                 List<ConductManagementService.FilterOption> classOptions,
                                 List<ConductManagementService.FilterOption> courseOptions) {

        private static ScopeMetadata empty() {
            return new ScopeMetadata(null, null, null, null, null, List.of(), List.of(), List.of());
        }

        private boolean hasHomeroomClass() {
            return classId != null && !classId.isBlank();
        }
    }

    public static class TeacherConductDashboardData {
        private final ConductSearch search;
        private final ConductManagementService.ConductPageResult pageData;
        private final ConductManagementService.ConductStats stats;
        private final List<String> grades;
        private final List<ConductManagementService.FilterOption> classOptions;
        private final List<ConductManagementService.FilterOption> courseOptions;
        private final List<ActivityLogService.ConductActivityItem> activityLogs;
        private final List<AdminReportExportHistoryService.HistoryItem> exportHistory;

        public TeacherConductDashboardData(ConductSearch search,
                                           ConductManagementService.ConductPageResult pageData,
                                           ConductManagementService.ConductStats stats,
                                           List<String> grades,
                                           List<ConductManagementService.FilterOption> classOptions,
                                           List<ConductManagementService.FilterOption> courseOptions,
                                           List<ActivityLogService.ConductActivityItem> activityLogs,
                                           List<AdminReportExportHistoryService.HistoryItem> exportHistory) {
            this.search = search;
            this.pageData = pageData;
            this.stats = stats;
            this.grades = grades;
            this.classOptions = classOptions;
            this.courseOptions = courseOptions;
            this.activityLogs = activityLogs;
            this.exportHistory = exportHistory;
        }

        public static TeacherConductDashboardData empty(ConductSearch search) {
            return new TeacherConductDashboardData(
                    search == null ? new ConductSearch() : search,
                    new ConductManagementService.ConductPageResult(List.of(), 1, 1, 0, 0, 0),
                    new ConductManagementService.ConductStats(0, 0, 0, 0.0, 0.0),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        public ConductSearch getSearch() {
            return search;
        }

        public ConductManagementService.ConductPageResult getPageData() {
            return pageData;
        }

        public ConductManagementService.ConductStats getStats() {
            return stats;
        }

        public List<String> getGrades() {
            return grades;
        }

        public List<ConductManagementService.FilterOption> getClassOptions() {
            return classOptions;
        }

        public List<ConductManagementService.FilterOption> getCourseOptions() {
            return courseOptions;
        }

        public List<ActivityLogService.ConductActivityItem> getActivityLogs() {
            return activityLogs;
        }

        public List<AdminReportExportHistoryService.HistoryItem> getExportHistory() {
            return exportHistory;
        }
    }
}
