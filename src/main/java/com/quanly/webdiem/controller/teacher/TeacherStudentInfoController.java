package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import com.quanly.webdiem.model.service.teacher.TeacherStudentService;
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

    private static final String ERROR_NO_HOMEROOM_CLASS = "Tài khoản chưa được phân công lớp chủ nhiệm.";

    private final TeacherStudentService teacherStudentService;
    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherStudentInfoController(TeacherStudentService teacherStudentService,
                                        TeacherStudentScopeService scopeService,
                                        TeacherPageModelHelper pageModelHelper) {
        this.teacherStudentService = teacherStudentService;
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
            redirectAttributes.addFlashAttribute("flashMessage", ERROR_NO_HOMEROOM_CLASS);
            return "redirect:/teacher/student";
        }

        try {
            Student student = teacherStudentService.getStudentForDisplay(id, scope);
            pageModelHelper.applyStudentPage(model, "Thông tin học sinh", scope);
            model.addAttribute("student", student);
            model.addAttribute("classHistories", teacherStudentService.getStudentClassHistories(id));
            model.addAttribute("editLogs", teacherStudentService.getStudentEditLogs(id));
            return "teacher/student-info";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/teacher/student";
        }
    }
}
