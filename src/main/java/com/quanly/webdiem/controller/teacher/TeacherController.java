package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherController(TeacherStudentScopeService scopeService,
                             TeacherPageModelHelper pageModelHelper) {
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping({"", "/"})
    public String root() {
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        pageModelHelper.applyBasePage(model, "dashboard", "Trang chủ giáo viên chủ nhiệm", scope);
        return "teacher/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        pageModelHelper.applyBasePage(model, "profile", "Thông tin cá nhân", scope);
        return "teacher/profile";
    }
}