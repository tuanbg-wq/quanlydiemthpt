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

import java.util.LinkedHashMap;
import java.util.List;
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
                              Authentication authentication,
                              Model model) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        pageModelHelper.applyStudentPage(model, "Quản lý học sinh chủ nhiệm", scope);

        if (!scopeService.hasHomeroomClass(scope)) {
            model.addAttribute("students", List.of());
            model.addAttribute("studentHistoryLogs", List.of());
            model.addAttribute("studentDisplayById", Map.of());
            model.addAttribute("showHistoryColumn", false);
            model.addAttribute("warningMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            return "teacher/student";
        }

        search.setCourseId(null);
        search.setKhoi(null);
        search.setClassId(scope.getClassId());

        List<Student> students = studentService.search(search);
        model.addAttribute("students", students);
        model.addAttribute("showHistoryColumn", search.getHistoryType() != null && !search.getHistoryType().isBlank());

        List<String> studentIds = students.stream()
                .map(Student::getIdHocSinh)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        List<ActivityLog> studentHistoryLogs = activityLogService.getStudentLogsByStudentIds(studentIds, 5);
        model.addAttribute("studentHistoryLogs", studentHistoryLogs);

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
}

