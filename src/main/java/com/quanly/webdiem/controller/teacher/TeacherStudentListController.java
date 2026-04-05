package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentService;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/teacher/student")
@PreAuthorize("hasAnyAuthority('ROLE_Giao_vien','ROLE_GVCN','ROLE_Admin')")
public class TeacherStudentListController {

    private final TeacherStudentService teacherStudentService;
    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherStudentListController(TeacherStudentService teacherStudentService,
                                        TeacherStudentScopeService scopeService,
                                        TeacherPageModelHelper pageModelHelper) {
        this.teacherStudentService = teacherStudentService;
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping
    public String studentPage(@ModelAttribute("search") StudentSearch search,
                              @RequestParam(value = "schoolYear", required = false) String schoolYear,
                              Authentication authentication,
                              Model model) {
        String username = pageModelHelper.resolveUsername(authentication);
        List<String> homeroomSchoolYears = scopeService.getHomeroomSchoolYearsByUsername(username);
        String selectedSchoolYear = normalizeSchoolYear(schoolYear, homeroomSchoolYears);

        TeacherHomeroomScope scope = scopeService.resolveScopeByUsernameAndSchoolYear(username, selectedSchoolYear);
        pageModelHelper.applyStudentPage(model, "Quản lý học sinh chủ nhiệm", scope);
        model.addAttribute("homeroomSchoolYears", homeroomSchoolYears);
        model.addAttribute("selectedSchoolYear", selectedSchoolYear == null ? scope.getSchoolYear() : selectedSchoolYear);

        if (!scopeService.hasHomeroomClass(scope)) {
            model.addAttribute("students", List.of());
            model.addAttribute("activityLogs", List.of());
            model.addAttribute("showHistoryColumn", false);
            if (selectedSchoolYear != null) {
                model.addAttribute("warningMessage", "Không tìm thấy lớp chủ nhiệm của bạn ở năm học " + selectedSchoolYear + ".");
            } else {
                model.addAttribute("warningMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            }
            return "teacher/student";
        }

        List<Student> students = sortStudentsByName(teacherStudentService.searchInScope(scope, search));
        model.addAttribute("students", students);
        model.addAttribute("showHistoryColumn", search.getHistoryType() != null && !search.getHistoryType().isBlank());
        model.addAttribute("activityLogs", teacherStudentService.getRecentActivitiesByScope(scope, 30));

        return "teacher/student";
    }

    private String normalizeSchoolYear(String schoolYear, List<String> availableYears) {
        if (schoolYear == null || schoolYear.isBlank()) {
            return null;
        }
        if (availableYears == null || availableYears.isEmpty()) {
            return schoolYear.trim();
        }
        for (String availableYear : availableYears) {
            if (availableYear != null && availableYear.equalsIgnoreCase(schoolYear.trim())) {
                return availableYear;
            }
        }
        return null;
    }

    private List<Student> sortStudentsByName(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }
        List<Student> sortedStudents = new ArrayList<>(students);

        Collator viCollator = Collator.getInstance(new Locale("vi", "VN"));
        viCollator.setStrength(Collator.PRIMARY);

        sortedStudents.sort(
                Comparator.comparing(
                                (Student student) -> extractGivenName(student == null ? null : student.getHoTen()),
                                viCollator
                        )
                        .thenComparing(
                                (Student student) -> extractNamePrefix(student == null ? null : student.getHoTen()),
                                viCollator
                        )
                        .thenComparing(
                                (Student student) -> normalizeForSort(student == null ? null : student.getHoTen()),
                                viCollator
                        )
                        .thenComparing((Student student) -> normalizeForSort(student == null ? null : student.getIdHocSinh()))
        );
        return sortedStudents;
    }

    private String normalizeForSort(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private String extractGivenName(String fullName) {
        String normalized = normalizeForSort(fullName).replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }
        int lastSpace = normalized.lastIndexOf(' ');
        return lastSpace < 0 ? normalized : normalized.substring(lastSpace + 1);
    }

    private String extractNamePrefix(String fullName) {
        String normalized = normalizeForSort(fullName).replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }
        int lastSpace = normalized.lastIndexOf(' ');
        return lastSpace < 0 ? "" : normalized.substring(0, lastSpace);
    }
}
