package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.AdminReportSearch;
import com.quanly.webdiem.model.service.admin.report.AdminReportPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/report")
public class AdminReportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminReportController.class);

    private final AdminReportPageService reportPageService;

    public AdminReportController(AdminReportPageService reportPageService) {
        this.reportPageService = reportPageService;
    }

    @GetMapping
    public String reportPage(@ModelAttribute("search") AdminReportSearch search,
                             Model model) {
        AdminReportPageService.ReportPagePayload payload = reportPageService.buildPage(search);

        model.addAttribute("activePage", "report");
        model.addAttribute("pageTitle", "Báo cáo thống kê");
        model.addAttribute("typeCards", payload.getTypeCards());
        model.addAttribute("selectedType", payload.getSelectedType());
        model.addAttribute("filters", payload.getFilters());
        model.addAttribute("preview", payload.getPreview());
        model.addAttribute("exportHistory", payload.getExportHistory());

        return "admin/report";
    }

    @PostMapping("/export")
    public String createReport(@ModelAttribute("search") AdminReportSearch search,
                               @RequestParam(value = "format", required = false) String format,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String displayName = resolveDisplayName(authentication);
            AdminReportPageService.ExportResult result = reportPageService.createExport(search, format, displayName);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", result.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Lỗi tạo báo cáo thống kê", ex);
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Không thể tạo báo cáo. Vui lòng thử lại.");
        }

        applySearchRedirectAttributes(search, redirectAttributes);
        return "redirect:/admin/report";
    }

    private void applySearchRedirectAttributes(AdminReportSearch search,
                                               RedirectAttributes redirectAttributes) {
        if (search == null) {
            return;
        }

        addIfPresent(redirectAttributes, "type", search.getType());
        addIfPresent(redirectAttributes, "q", search.getQ());
        addIfPresent(redirectAttributes, "namHoc", search.getNamHoc());
        addIfPresent(redirectAttributes, "hocKy", search.getHocKy());
        addIfPresent(redirectAttributes, "khoi", search.getKhoi());
        addIfPresent(redirectAttributes, "lop", search.getLop());
        addIfPresent(redirectAttributes, "mon", search.getMon());
        addIfPresent(redirectAttributes, "khoa", search.getKhoa());
        addIfPresent(redirectAttributes, "loai", search.getLoai());
        addIfPresent(redirectAttributes, "boMon", search.getBoMon());
        addIfPresent(redirectAttributes, "trangThai", search.getTrangThai());
        addIfPresent(redirectAttributes, "vaiTro", search.getVaiTro());
    }

    private void addIfPresent(RedirectAttributes redirectAttributes, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        redirectAttributes.addAttribute(key, value);
    }

    private String resolveDisplayName(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "Quản trị";
        }
        return authentication.getName().trim();
    }
}
