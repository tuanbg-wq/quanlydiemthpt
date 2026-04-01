package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.DashboardSearch;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.admin.DashboardService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private static final String DASHBOARD_ERROR_MESSAGE = "Không thể tải dữ liệu trang chủ.";

    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"", "/"})
    public String root() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@ModelAttribute("search") DashboardSearch search,
                            Authentication authentication,
                            Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("displayName", resolveDisplayName(authentication));

        try {
            DashboardService.DashboardData data = dashboardService.getDashboardData(search);
            model.addAttribute("soHocSinh", data.getSoHocSinh());
            model.addAttribute("soGiaoVien", data.getSoGiaoVien());
            model.addAttribute("soMonHoc", data.getSoMonHoc());
            model.addAttribute("soLop", data.getSoLop());
            model.addAttribute("scoreStats", data.getScoreStats());
            model.addAttribute("conductStats", data.getConductStats());
            model.addAttribute("grades", data.getGrades());
            model.addAttribute("classOptions", data.getClassOptions());
            model.addAttribute("courseOptions", data.getCourseOptions());
            model.addAttribute("activityItems", data.getActivityItems());
            model.addAttribute("recentStudents", data.getRecentStudents());
        } catch (Exception ex) {
            LOGGER.error("Lỗi tải dữ liệu dashboard", ex);
            model.addAttribute("soHocSinh", 0);
            model.addAttribute("soGiaoVien", 0);
            model.addAttribute("soMonHoc", 0);
            model.addAttribute("soLop", 0);
            model.addAttribute("scoreStats", new ScoreManagementService.ScoreStats(0, 0, 0, 0, 0, 0, 0));
            model.addAttribute("conductStats", new ConductManagementService.ConductStats(0, 0, 0, 0, 0));
            model.addAttribute("grades", List.of());
            model.addAttribute("classOptions", List.of());
            model.addAttribute("courseOptions", List.of());
            model.addAttribute("activityItems", List.of());
            model.addAttribute("recentStudents", List.of());
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", DASHBOARD_ERROR_MESSAGE);
        }

        return "admin/dashboard";
    }

    @GetMapping("/students")
    public String students() {
        return "redirect:/admin/student";
    }

    @GetMapping("/subjects")
    public String subjects() {
        return "redirect:/admin/subject";
    }

    @GetMapping("/teachers")
    public String teachers() {
        return "redirect:/admin/teacher";
    }

    @GetMapping("/scores")
    public String scores() {
        return "redirect:/admin/score";
    }

    @GetMapping("/conducts")
    public String conducts() {
        return "redirect:/admin/conduct";
    }

    @GetMapping("/accounts")
    public String accounts() {
        return "redirect:/admin/account";
    }

    private String resolveDisplayName(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "Quản trị";
        }
        return authentication.getName().trim();
    }
}
