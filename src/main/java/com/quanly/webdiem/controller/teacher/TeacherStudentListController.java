package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.entity.ActivityLog;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.admin.StudentService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/teacher/student")
@PreAuthorize("hasAnyAuthority('ROLE_Giao_vien','ROLE_GVCN','ROLE_Admin')")
public class TeacherStudentListController {

    private final StudentService studentService;
    private final ActivityLogService activityLogService;
    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherStudentListController(StudentService studentService,
                                        ActivityLogService activityLogService,
                                        TeacherStudentScopeService scopeService,
                                        TeacherPageModelHelper pageModelHelper) {
        this.studentService = studentService;
        this.activityLogService = activityLogService;
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping
    public String studentPage(@ModelAttribute("search") StudentSearch search,
                              @RequestParam(value = "historyMode", required = false) String historyMode,
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

        boolean showAllHistory = "all".equalsIgnoreCase(historyMode);
        model.addAttribute("showAllHistory", showAllHistory);

        if (!scopeService.hasHomeroomClass(scope)) {
            model.addAttribute("students", List.of());
            model.addAttribute("studentHistoryLogs", List.of());
            model.addAttribute("studentDisplayById", Map.of());
            model.addAttribute("hasMoreHistory", false);
            model.addAttribute("showHistoryColumn", false);
            if (selectedSchoolYear != null) {
                model.addAttribute("warningMessage", "Không tìm thấy lớp chủ nhiệm của bạn ở năm học " + selectedSchoolYear + ".");
            } else {
                model.addAttribute("warningMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            }
            return "teacher/student";
        }

        search.setCourseId(null);
        search.setKhoi(null);
        search.setClassId(scope.getClassId());

        List<Student> students = sortStudentsByName(studentService.search(search));
        model.addAttribute("students", students);
        model.addAttribute("showHistoryColumn", search.getHistoryType() != null && !search.getHistoryType().isBlank());

        List<String> studentIds = students.stream()
                .map(Student::getIdHocSinh)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();

        int historyLimit = showAllHistory ? 120 : 6;
        List<ActivityLog> logs = activityLogService.getStudentLogsByStudentIdsAndUsername(
                studentIds,
                username,
                historyLimit
        );
        logs = filterLogsByCurrentStudents(logs, students);
        boolean hasMoreHistory = !showAllHistory && logs.size() > 5;
        if (hasMoreHistory) {
            logs = logs.subList(0, 5);
        }
        model.addAttribute("studentHistoryLogs", logs);
        model.addAttribute("hasMoreHistory", hasMoreHistory);

        Map<String, String> studentDisplayById = new LinkedHashMap<>();
        for (Student student : students) {
            if (student.getIdHocSinh() == null || student.getIdHocSinh().isBlank()) {
                continue;
            }
            String name = student.getHoTen() == null || student.getHoTen().isBlank()
                    ? "(không rõ)"
                    : student.getHoTen().trim();
            studentDisplayById.put(student.getIdHocSinh(), name + " (" + student.getIdHocSinh() + ")");
        }
        model.addAttribute("studentDisplayById", studentDisplayById);

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

    private List<ActivityLog> filterLogsByCurrentStudents(List<ActivityLog> logs, List<Student> students) {
        if (logs == null || logs.isEmpty() || students == null || students.isEmpty()) {
            return List.of();
        }

        Map<String, LocalDateTime> createdAtByStudentId = new HashMap<>();
        for (Student student : students) {
            if (student == null || student.getIdHocSinh() == null || student.getIdHocSinh().isBlank()) {
                continue;
            }
            createdAtByStudentId.put(student.getIdHocSinh(), student.getNgayTao());
        }

        List<ActivityLog> filtered = new ArrayList<>();
        for (ActivityLog log : logs) {
            if (log == null || log.getIdBanGhi() == null || log.getIdBanGhi().isBlank()) {
                continue;
            }

            LocalDateTime createdAt = createdAtByStudentId.get(log.getIdBanGhi());
            if (createdAt != null && log.getThoiGian() != null && log.getThoiGian().isBefore(createdAt)) {
                continue;
            }

            filtered.add(log);
        }

        return filtered;
    }

}
