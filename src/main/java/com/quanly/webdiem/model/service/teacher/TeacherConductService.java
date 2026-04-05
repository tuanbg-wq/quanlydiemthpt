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
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherConductService {

    private final ConductManagementService conductManagementService;
    private final TeacherStudentScopeService scopeService;
    private final ActivityLogService activityLogService;

    public TeacherConductService(ConductManagementService conductManagementService,
                                 TeacherStudentScopeService scopeService,
                                 ActivityLogService activityLogService) {
        this.conductManagementService = conductManagementService;
        this.scopeService = scopeService;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public TeacherConductDashboardData loadDashboard(TeacherHomeroomScope scope, ConductSearch search) {
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
                activityLogService.getRecentConductActivitiesByClassId(scopeMetadata.classId(), null, null, 20)
        );
    }

    @Transactional(readOnly = true)
    public ConductRewardCreatePageData getCreatePageData(TeacherHomeroomScope scope,
                                                         ConductRewardCreateFilter filter) {
        ScopeMetadata scopeMetadata = requireScopeMetadata(scope);
        ConductRewardCreateFilter scopedFilter = applyScope(filter, scopeMetadata);
        ConductRewardCreatePageData basePageData = conductManagementService.getRewardCreatePageData(scopedFilter);

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
        return conductManagementService.suggestStudentsForReward(
                q,
                scopeMetadata.grade(),
                scopeMetadata.classId(),
                scopeMetadata.courseId()
        ).stream()
                .filter(candidate -> isCandidateInScope(candidate, scopeMetadata.classId()))
                .toList();
    }

    @Transactional
    public void createReward(TeacherHomeroomScope scope, ConductRewardCreateRequest request) {
        assertStudentInScope(scope, request == null ? null : request.getStudentId());
        conductManagementService.createReward(request);
    }

    @Transactional
    public void createDiscipline(TeacherHomeroomScope scope, ConductRewardCreateRequest request) {
        assertStudentInScope(scope, request == null ? null : request.getStudentId());
        conductManagementService.createDiscipline(request);
    }

    @Transactional(readOnly = true)
    public ConductManagementService.ConductRow getEventDetailInScope(TeacherHomeroomScope scope, Long eventId) {
        ConductManagementService.ConductRow detail = conductManagementService.getEventDetail(eventId);
        assertStudentInScope(scope, detail == null ? null : detail.getIdHocSinh());
        return detail;
    }

    @Transactional(readOnly = true)
    public ConductEventUpsertRequest getEditDataInScope(TeacherHomeroomScope scope, Long eventId) {
        ConductManagementService.ConductRow detail = getEventDetailInScope(scope, eventId);
        ConductEventUpsertRequest request = conductManagementService.getEditData(detail.getEventId());
        request.setStudentId(detail.getIdHocSinh());
        return request;
    }

    @Transactional
    public void updateEvent(TeacherHomeroomScope scope, ConductEventUpsertRequest request) {
        Long eventId = request == null ? null : request.getEventId();
        ConductManagementService.ConductRow detail = getEventDetailInScope(scope, eventId);
        assertStudentInScope(scope, detail == null ? null : detail.getIdHocSinh());
        conductManagementService.updateEvent(request);
    }

    @Transactional
    public void deleteEvent(TeacherHomeroomScope scope, Long eventId) {
        ConductManagementService.ConductRow detail = getEventDetailInScope(scope, eventId);
        assertStudentInScope(scope, detail == null ? null : detail.getIdHocSinh());
        conductManagementService.deleteEvent(eventId);
    }

    @Transactional(readOnly = true)
    public ConductManagementService.ConductRow getLatestEventByStudentAndTypeInScope(TeacherHomeroomScope scope,
                                                                                     String studentId,
                                                                                     String loai) {
        assertStudentInScope(scope, studentId);
        ConductManagementService.ConductRow detail = conductManagementService.getLatestEventByStudentAndType(studentId, loai);
        if (detail == null) {
            return null;
        }
        assertStudentInScope(scope, detail.getIdHocSinh());
        return detail;
    }

    @Transactional(readOnly = true)
    public List<ConductManagementService.ConductRow> getRowsForExport(TeacherHomeroomScope scope, ConductSearch search) {
        ScopeMetadata scopeMetadata = requireScopeMetadata(scope);
        return conductManagementService.getRowsForExport(applyScope(search, scopeMetadata));
    }

    @Transactional(readOnly = true)
    public String suggestRewardDecisionNumber() {
        return conductManagementService.suggestDecisionNumber(ConductManagementService.LOAI_KHEN_THUONG, "GVCN-QĐ-KT");
    }

    @Transactional(readOnly = true)
    public String suggestDisciplineDecisionNumber() {
        return conductManagementService.suggestDecisionNumber(ConductManagementService.LOAI_KY_LUAT, "GVCN-QĐ-KL");
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

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

        public TeacherConductDashboardData(ConductSearch search,
                                           ConductManagementService.ConductPageResult pageData,
                                           ConductManagementService.ConductStats stats,
                                           List<String> grades,
                                           List<ConductManagementService.FilterOption> classOptions,
                                           List<ConductManagementService.FilterOption> courseOptions,
                                           List<ActivityLogService.ConductActivityItem> activityLogs) {
            this.search = search;
            this.pageData = pageData;
            this.stats = stats;
            this.grades = grades;
            this.classOptions = classOptions;
            this.courseOptions = courseOptions;
            this.activityLogs = activityLogs;
        }

        public static TeacherConductDashboardData empty(ConductSearch search) {
            return new TeacherConductDashboardData(
                    search == null ? new ConductSearch() : search,
                    new ConductManagementService.ConductPageResult(List.of(), 1, 1, 0, 0, 0),
                    new ConductManagementService.ConductStats(0, 0, 0, 0.0, 0.0),
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
    }
}
