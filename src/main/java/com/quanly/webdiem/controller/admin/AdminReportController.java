package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.AdminReportSearch;
import com.quanly.webdiem.model.service.admin.report.AdminReportPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

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
        sanitizeSearch(search);
        AdminReportPageService.ReportPagePayload payload = reportPageService.buildPage(search);

        model.addAttribute("activePage", "report");
        model.addAttribute("pageTitle", "Báo cáo thống kê");
        model.addAttribute("typeCards", payload.getTypeCards());
        model.addAttribute("selectedType", payload.getSelectedType());
        model.addAttribute("filters", payload.getFilters());
        model.addAttribute("preview", payload.getPreview());
        model.addAttribute("previewVisible", payload.isPreviewVisible());
        model.addAttribute("previewPage", payload.getPreviewPage());
        model.addAttribute("totalPreviewPages", payload.getTotalPreviewPages());
        model.addAttribute("exportHistory", payload.getExportHistory());
        model.addAttribute("historyTypeOptions", payload.getHistoryTypeOptions());
        model.addAttribute("historyFormatOptions", payload.getHistoryFormatOptions());
        model.addAttribute("historyTimeOptions", payload.getHistoryTimeOptions());
        model.addAttribute("historyRoleOptions", payload.getHistoryRoleOptions());
        sanitizeFlashMessage(model);

        return "admin/report";
    }

    @PostMapping("/export")
    public Object createReport(@ModelAttribute("search") AdminReportSearch search,
                               @RequestParam(value = "format", required = false) String format,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        sanitizeSearch(search);
        try {
            String displayName = resolveDisplayName(authentication);
            String roleCode = resolveRoleCode(authentication);
            AdminReportPageService.ExportResult result = reportPageService.createExport(search, format, displayName, roleCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(result.getFileName(), StandardCharsets.UTF_8)
                            .build()
            );
            return ResponseEntity.ok()
                    .contentType(result.getMediaType())
                    .headers(headers)
                    .body(result.getContent());
        } catch (Exception ex) {
            LOGGER.error("Lỗi tạo báo cáo thống kê", ex);
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage() == null
                    ? "Không thể tạo báo cáo. Vui lòng thử lại."
                    : ex.getMessage());
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
        addIfPresent(redirectAttributes, "hanhKiem", search.getHanhKiem());
        addIfPresent(redirectAttributes, "lichSuChuyen", search.getLichSuChuyen());
        addIfPresent(redirectAttributes, "applyPreview", search.getApplyPreview());
        addIfPresent(redirectAttributes, "previewPage", search.getPreviewPage());
        addIfPresent(redirectAttributes, "historyType", search.getHistoryType());
        addIfPresent(redirectAttributes, "historyFormat", search.getHistoryFormat());
        addIfPresent(redirectAttributes, "historyTime", search.getHistoryTime());
        addIfPresent(redirectAttributes, "historyRole", search.getHistoryRole());
        addIfPresent(redirectAttributes, "historyDate", search.getHistoryDate());
        addIfPresent(redirectAttributes, "historyMonth", search.getHistoryMonth());
        addIfPresent(redirectAttributes, "historyYear", search.getHistoryYear());
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

    private String resolveRoleCode(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "ADMIN";
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) {
                continue;
            }
            String code = authority.getAuthority().trim().toUpperCase(Locale.ROOT);
            if ("ROLE_GVCN".equals(code)) {
                return "GVCN";
            }
            if ("ROLE_GVBM".equals(code) || "ROLE_GIAO_VIEN".equals(code)) {
                return "GVBM";
            }
        }
        return "ADMIN";
    }

    private void sanitizeSearch(AdminReportSearch search) {
        if (search == null) {
            return;
        }
        search.setType(sanitizeText(search.getType(), 40));
        search.setQ(sanitizeText(search.getQ(), 120));
        search.setNamHoc(sanitizeText(search.getNamHoc(), 40));
        search.setHocKy(sanitizeText(search.getHocKy(), 10));
        search.setKhoi(sanitizeText(search.getKhoi(), 20));
        search.setLop(sanitizeText(search.getLop(), 40));
        search.setMon(sanitizeText(search.getMon(), 40));
        search.setKhoa(sanitizeText(search.getKhoa(), 40));
        search.setLoai(sanitizeText(search.getLoai(), 40));
        search.setBoMon(sanitizeText(search.getBoMon(), 60));
        search.setTrangThai(sanitizeText(search.getTrangThai(), 40));
        search.setVaiTro(sanitizeText(search.getVaiTro(), 40));
        search.setHanhKiem(sanitizeText(search.getHanhKiem(), 40));
        search.setLichSuChuyen(sanitizeText(search.getLichSuChuyen(), 40));
        search.setHistoryType(sanitizeText(search.getHistoryType(), 40));
        search.setHistoryFormat(sanitizeText(search.getHistoryFormat(), 10));
        search.setHistoryTime(sanitizeText(search.getHistoryTime(), 10));
        search.setHistoryRole(sanitizeHistoryRole(search.getHistoryRole()));
        search.setHistoryDate(sanitizeText(search.getHistoryDate(), 10));
        search.setHistoryMonth(sanitizeText(search.getHistoryMonth(), 7));
        search.setHistoryYear(sanitizeYear(search.getHistoryYear()));
        search.setApplyPreview("1".equals(search.getApplyPreview()) ? "1" : null);
        search.setPreviewPage(sanitizePreviewPage(search.getPreviewPage()));
    }

    private String sanitizePreviewPage(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                return null;
            }
            return String.valueOf(parsed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String sanitizeYear(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (!trimmed.matches("\\d{4}")) {
            return null;
        }
        return trimmed;
    }

    private String sanitizeHistoryRole(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(normalized) || "GVCN".equals(normalized) || "GVBM".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private void sanitizeFlashMessage(Model model) {
        if (model == null) {
            return;
        }
        Map<String, Object> attributes = model.asMap();
        Object rawFlashMessage = attributes.get("flashMessage");
        if (rawFlashMessage == null) {
            model.addAttribute("sanitizedFlashMessage", null);
            return;
        }
        if (!(rawFlashMessage instanceof String flashMessage)) {
            attributes.remove("flashMessage");
            attributes.remove("flashType");
            model.addAttribute("sanitizedFlashMessage", null);
            return;
        }
        String sanitizedFlashMessage = sanitizeText(flashMessage, 220);
        if (sanitizedFlashMessage == null) {
            attributes.remove("flashMessage");
            attributes.remove("flashType");
            model.addAttribute("sanitizedFlashMessage", null);
            return;
        }
        model.addAttribute("flashMessage", sanitizedFlashMessage);
        model.addAttribute("sanitizedFlashMessage", sanitizedFlashMessage);
    }

    private String sanitizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (looksLikeHeaderDump(trimmed)) {
            return null;
        }

        if (maxLength > 0 && trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }

    private boolean looksLikeHeaderDump(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return false;
        }

        if ((normalized.startsWith("{") && normalized.contains("=") && normalized.contains(","))
                || (normalized.length() > 80 && normalized.contains("=") && normalized.contains(","))) {
            int headerSignals = 0;
            if (normalized.contains("sec-fetch")) headerSignals++;
            if (normalized.contains("user-agent")) headerSignals++;
            if (normalized.contains("accept-language")) headerSignals++;
            if (normalized.contains("accept-encoding")) headerSignals++;
            if (normalized.contains("cookie=")) headerSignals++;
            if (normalized.contains("referer=http")) headerSignals++;
            if (normalized.contains("host=localhost")) headerSignals++;
            if (normalized.contains("connection=keep-alive")) headerSignals++;
            if (headerSignals >= 2) {
                return true;
            }
        }

        return normalized.startsWith("{sec-fetch-")
                || normalized.startsWith("sec-fetch-")
                || normalized.contains("sec-fetch-mode=")
                || normalized.contains("sec-fetch-site=")
                || normalized.contains("sec-fetch-dest=")
                || normalized.contains("accept-language=")
                || normalized.contains("accept-encoding=")
                || normalized.contains("user-agent=")
                || normalized.contains("cookie=jsessionid")
                || normalized.contains("sec-ch-ua")
                || normalized.contains("upgrade-insecure-requests=")
                || normalized.contains("connection=keep-alive")
                || normalized.contains("host=localhost")
                || normalized.contains("referer=http");
    }
}
