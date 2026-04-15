package com.quanly.webdiem.model.service.teacher_subject;

import com.quanly.webdiem.model.dao.ScoreDAO;
import com.quanly.webdiem.model.search.TeacherScoreSearch;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ClassFilterOption;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.CreateScopeData;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.FilterOption;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ScoreDashboardData;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class TeacherSubjectScoreService {

    private static final String CLASS_SCOPE_SUBJECT = "SUBJECT";

    private final TeacherStudentScopeService scopeService;
    private final TeacherScoreService teacherScoreService;
    private final ScoreDAO scoreDAO;
    private final ActivityLogService activityLogService;

    public TeacherSubjectScoreService(TeacherStudentScopeService scopeService,
                                      TeacherScoreService teacherScoreService,
                                      ScoreDAO scoreDAO,
                                      ActivityLogService activityLogService) {
        this.scopeService = scopeService;
        this.teacherScoreService = teacherScoreService;
        this.scoreDAO = scoreDAO;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public TeacherSubjectScorePageData loadPageData(String username, TeacherScoreSearch rawSearch) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        TeacherScoreSearch search = sanitizeSearch(username, scope, rawSearch);
        ScoreDashboardData dashboardData = teacherScoreService.loadDashboard(username, scope, search);
        List<ClassFilterOption> subjectClassOptions = filterSubjectClasses(dashboardData.getClassOptions());
        List<FilterOption> courseOptions = loadCourseOptions(username, dashboardData.getSchoolYear());
        String selectedClassDisplay = resolveSelectedClassDisplay(dashboardData.getSearch(), subjectClassOptions);
        return new TeacherSubjectScorePageData(
                scope,
                dashboardData,
                subjectClassOptions,
                courseOptions,
                selectedClassDisplay,
                activityLogService.getRecentScoreActivitiesByUsername(username, 24)
        );
    }

    @Transactional(readOnly = true)
    public ScoreDashboardData loadDashboardForExport(String username, TeacherScoreSearch rawSearch) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        TeacherScoreSearch search = sanitizeSearch(username, scope, rawSearch);
        return teacherScoreService.loadDashboardForExport(username, scope, search);
    }

    @Transactional(readOnly = true)
    public TeacherHomeroomScope resolveScope(String username) {
        return scopeService.resolveScopeByUsername(username);
    }

    @Transactional(readOnly = true)
    public String resolveSchoolYear(String username) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        return teacherScoreService.resolveSchoolYearForTeacher(username, scope);
    }

    public TeacherSubjectScorePageData emptyPageData(TeacherScoreSearch search) {
        return new TeacherSubjectScorePageData(
                null,
                ScoreDashboardData.empty(search, null),
                List.of(),
                List.of(),
                null,
                List.of()
        );
    }

    private TeacherScoreSearch sanitizeSearch(String username,
                                              TeacherHomeroomScope scope,
                                              TeacherScoreSearch rawSearch) {
        TeacherScoreSearch sanitized = new TeacherScoreSearch();
        if (rawSearch != null) {
            sanitized.setQ(rawSearch.getQ());
            sanitized.setKhoa(rawSearch.getKhoa());
            sanitized.setHocLuc(rawSearch.getHocLuc());
            sanitized.setMon(rawSearch.getMon());
            sanitized.setHocKy(rawSearch.getHocKy());
            sanitized.setPage(rawSearch.getPage());
        }
        sanitized.setClassScope(CLASS_SCOPE_SUBJECT);

        CreateScopeData createScopeData = teacherScoreService.buildCreateScopeData(
                username,
                scope,
                rawSearch == null ? null : rawSearch.getClassId(),
                rawSearch == null ? null : rawSearch.getMon()
        );
        List<FilterOption> courseOptions = loadCourseOptions(username, createScopeData.getSchoolYear());
        String requestedCourseId = rawSearch == null ? null : rawSearch.getKhoa();
        if (requestedCourseId != null && courseOptions.stream()
                .filter(Objects::nonNull)
                .anyMatch(item -> item.getId() != null && item.getId().equalsIgnoreCase(requestedCourseId))) {
            sanitized.setKhoa(requestedCourseId);
        }
        String requestedClassId = rawSearch == null ? null : rawSearch.getClassId();
        if (requestedClassId != null && createScopeData.getClassOptions().stream()
                .filter(Objects::nonNull)
                .anyMatch(item -> item.getId() != null && item.getId().equalsIgnoreCase(requestedClassId))) {
            sanitized.setClassId(requestedClassId);
        }
        return sanitized;
    }

    private List<FilterOption> loadCourseOptions(String username, String schoolYear) {
        String teacherId = teacherScoreService.resolveTeacherId(username);
        if (teacherId == null || schoolYear == null || schoolYear.isBlank()) {
            return List.of();
        }

        LinkedHashMap<String, FilterOption> options = new LinkedHashMap<>();
        for (Object[] row : scoreDAO.findTeachingCoursesByTeacherAndYear(teacherId, schoolYear)) {
            FilterOption item = mapFilterOption(row);
            if (item == null || item.getId() == null) {
                continue;
            }
            options.put(item.getId().toLowerCase(Locale.ROOT), item);
        }
        return new ArrayList<>(options.values());
    }

    private List<ClassFilterOption> filterSubjectClasses(List<ClassFilterOption> classOptions) {
        if (classOptions == null || classOptions.isEmpty()) {
            return List.of();
        }
        return classOptions.stream()
                .filter(Objects::nonNull)
                .filter(item -> !item.isHomeroom())
                .toList();
    }

    private String resolveSelectedClassDisplay(TeacherScoreSearch search,
                                               List<ClassFilterOption> classOptions) {
        if (search == null || classOptions == null || classOptions.isEmpty()) {
            return null;
        }
        String selectedClassId = search.getClassId();
        if (selectedClassId == null || selectedClassId.isBlank()) {
            return null;
        }
        for (ClassFilterOption option : classOptions) {
            if (option == null || option.getId() == null || !option.getId().equalsIgnoreCase(selectedClassId)) {
                continue;
            }
            String name = option.getName();
            if (name == null || name.isBlank() || name.equalsIgnoreCase(option.getId())) {
                return option.getId();
            }
            return option.getId() + " - " + name;
        }
        return null;
    }

    private FilterOption mapFilterOption(Object[] row) {
        if (row == null || row.length == 0 || row[0] == null) {
            return null;
        }
        String id = row[0].toString().trim();
        if (id.isBlank()) {
            return null;
        }
        String name = row.length > 1 && row[1] != null ? row[1].toString().trim() : id;
        return new FilterOption(id, name.isBlank() ? id : name);
    }

    public static class TeacherSubjectScorePageData {
        private final TeacherHomeroomScope scope;
        private final ScoreDashboardData dashboardData;
        private final List<ClassFilterOption> subjectClassOptions;
        private final List<FilterOption> courseOptions;
        private final String selectedClassDisplay;
        private final List<ActivityLogService.ScoreActivityItem> activityLogs;

        public TeacherSubjectScorePageData(TeacherHomeroomScope scope,
                                           ScoreDashboardData dashboardData,
                                           List<ClassFilterOption> subjectClassOptions,
                                           List<FilterOption> courseOptions,
                                           String selectedClassDisplay,
                                           List<ActivityLogService.ScoreActivityItem> activityLogs) {
            this.scope = scope;
            this.dashboardData = dashboardData;
            this.subjectClassOptions = subjectClassOptions;
            this.courseOptions = courseOptions;
            this.selectedClassDisplay = selectedClassDisplay;
            this.activityLogs = activityLogs;
        }

        public TeacherHomeroomScope getScope() {
            return scope;
        }

        public ScoreDashboardData getDashboardData() {
            return dashboardData;
        }

        public List<ClassFilterOption> getSubjectClassOptions() {
            return subjectClassOptions;
        }

        public List<FilterOption> getCourseOptions() {
            return courseOptions;
        }

        public String getSelectedClassDisplay() {
            return selectedClassDisplay;
        }

        public List<ActivityLogService.ScoreActivityItem> getActivityLogs() {
            return activityLogs;
        }
    }
}
