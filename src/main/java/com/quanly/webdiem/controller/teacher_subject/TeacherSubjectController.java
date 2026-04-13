package com.quanly.webdiem.controller.teacher_subject;

import com.quanly.webdiem.model.service.teacher_subject.TeacherSubjectDashboardService;
import com.quanly.webdiem.model.service.teacher_subject.TeacherSubjectDashboardService.TeacherSubjectDashboardData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teacher-subject")
@PreAuthorize("hasAnyAuthority('ROLE_GVBM','ROLE_Giao_vien','ROLE_Admin')")
public class TeacherSubjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherSubjectController.class);

    private final TeacherSubjectPageModelHelper pageModelHelper;
    private final TeacherSubjectDashboardService teacherSubjectDashboardService;

    public TeacherSubjectController(TeacherSubjectPageModelHelper pageModelHelper,
                                    TeacherSubjectDashboardService teacherSubjectDashboardService) {
        this.pageModelHelper = pageModelHelper;
        this.teacherSubjectDashboardService = teacherSubjectDashboardService;
    }

    @GetMapping({"", "/"})
    public String root() {
        return "redirect:/teacher-subject/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        pageModelHelper.applyBasePage(model, "dashboard", "Trang chủ giáo viên bộ môn");

        TeacherSubjectDashboardData dashboardData;
        try {
            String username = pageModelHelper.resolveUsername(authentication);
            dashboardData = teacherSubjectDashboardService.loadDashboard(username);
        } catch (RuntimeException ex) {
            LOGGER.error("Loi tai dashboard giao vien bo mon", ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Không thể tải dữ liệu trang chủ giáo viên bộ môn.");
            dashboardData = new TeacherSubjectDashboardData(null, null, null, null, null, null, null);
        }

        model.addAttribute("dashboardData", dashboardData);
        if (!dashboardData.isHasAssignments()) {
            model.addAttribute("warningMessage", "Tài khoản hiện chưa được phân công lớp bộ môn trong năm học hiện tại.");
        } else if (!dashboardData.isHasScoreData()) {
            model.addAttribute("warningMessage", "Đã có phân công lớp nhưng chưa có dữ liệu điểm để hiển thị tổng quan.");
        }
        return "teacher-subject/dashboard";
    }
}
