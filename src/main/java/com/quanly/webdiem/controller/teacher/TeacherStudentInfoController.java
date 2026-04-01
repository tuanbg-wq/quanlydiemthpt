package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.StudentClassHistoryService;
import com.quanly.webdiem.model.service.admin.StudentService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/student")
@PreAuthorize("hasAnyAuthority('ROLE_Giao_vien','ROLE_GVCN','ROLE_Admin')")
public class TeacherStudentInfoController {

    private final StudentClassHistoryService historyService;
    private final ActivityLogService activityLogService;
    private final StudentService studentService;
    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherStudentInfoController(StudentClassHistoryService historyService,
                                        ActivityLogService activityLogService,
                                        StudentService studentService,
                                        TeacherStudentScopeService scopeService,
                                        TeacherPageModelHelper pageModelHelper) {
        this.historyService = historyService;
        this.activityLogService = activityLogService;
        this.studentService = studentService;
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/{id}/info")
    public String showInfo(@PathVariable String id,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            return "redirect:/teacher/student";
        }

        Student student;
        try {
            student = scopeService.getStudentInScopeOrThrow(id, scope);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/teacher/student";
        }

        studentService.populateConductForStudent(student);
        pageModelHelper.applyStudentPage(model, "Thông tin học sinh", scope);
        model.addAttribute("student", student);
        model.addAttribute("classHistories", historyService.getHistoryByStudent(id));
        model.addAttribute("editLogs", activityLogService.getStudentEditLogs(id));
        return "teacher/student-info";
    }
}

