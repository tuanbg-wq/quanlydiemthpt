package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.service.teacher.TeacherDashboardService;
import com.quanly.webdiem.model.service.teacher.TeacherDashboardService.TeacherDashboardData;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherController.class);

    private final TeacherStudentScopeService scopeService;
    private final TeacherDashboardService teacherDashboardService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherController(TeacherStudentScopeService scopeService,
                             TeacherDashboardService teacherDashboardService,
                             TeacherPageModelHelper pageModelHelper) {
        this.scopeService = scopeService;
        this.teacherDashboardService = teacherDashboardService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping({"", "/"})
    public String root() {
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        pageModelHelper.applyBasePage(model, "dashboard", "Trang chủ giáo viên chủ nhiệm", scope);

        TeacherDashboardData dashboardData = TeacherDashboardData.empty();
        try {
            dashboardData = teacherDashboardService.loadDashboard(username, scope);
        } catch (RuntimeException ex) {
            LOGGER.error("Loi tai dashboard giao vien chu nhiem", ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Không thể tải dữ liệu dashboard lớp chủ nhiệm.");
        }

        model.addAttribute("dashboardData", dashboardData);
        if (!scopeService.hasHomeroomClass(scope)) {
            model.addAttribute("warningMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
        }
        return "teacher/dashboard";
    }

}
