package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.admin.ConductRewardCreatePageData;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ConductPageSupport {

    public static final String PAGE_TITLE_CONDUCT = "Khen thưởng / Kỷ luật";
    public static final String PAGE_TITLE_CONDUCT_CREATE = "Thêm khen thưởng";
    public static final String PAGE_TITLE_CONDUCT_DISCIPLINE_CREATE = "Thêm kỷ luật";
    public static final String PAGE_TITLE_CONDUCT_INFO = "Thông tin quyết định";
    public static final String PAGE_TITLE_CONDUCT_EDIT = "Sửa quyết định";
    public static final String PAGE_ERROR_MESSAGE = "Không thể tải danh sách khen thưởng/kỷ luật.";

    private static final DateTimeFormatter EXPORT_FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void applyListPage(Model model,
                              ConductManagementService.ConductPageResult pageResult,
                              ConductManagementService.ConductStats stats,
                              List<String> grades,
                              List<ConductManagementService.FilterOption> classes,
                              List<ConductManagementService.FilterOption> courses,
                              List<ActivityLogService.ConductActivityItem> activityLogs) {
        model.addAttribute("activePage", "conduct");
        model.addAttribute("pageTitle", PAGE_TITLE_CONDUCT);
        model.addAttribute("records", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("stats", stats);
        model.addAttribute("grades", grades);
        model.addAttribute("classOptions", classes);
        model.addAttribute("courseOptions", courses);
        model.addAttribute("activityLogs", activityLogs);
    }

    public void applyBasePage(Model model, String pageTitle) {
        model.addAttribute("activePage", "conduct");
        model.addAttribute("pageTitle", pageTitle);
    }

    public ConductRewardCreatePageData emptyRewardCreatePageData() {
        return new ConductRewardCreatePageData(
                new com.quanly.webdiem.model.service.admin.ConductRewardCreateFilter(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
        );
    }

    public HttpHeaders downloadHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build()
        );
        return headers;
    }

    public String buildListExportFileName(String extension) {
        return "bao-cao-khen-thuong-ky-luat-" + EXPORT_FILE_DATE.format(LocalDate.now()) + "." + extension;
    }

    public void applySearchRedirectAttributes(RedirectAttributes redirectAttributes, ConductSearch search) {
        if (redirectAttributes == null || search == null) {
            return;
        }
        if (safeTrim(search.getQ()) != null) {
            redirectAttributes.addAttribute("q", search.getQ());
        }
        if (safeTrim(search.getKhoi()) != null) {
            redirectAttributes.addAttribute("khoi", search.getKhoi());
        }
        if (safeTrim(search.getLop()) != null) {
            redirectAttributes.addAttribute("lop", search.getLop());
        }
        if (safeTrim(search.getKhoa()) != null) {
            redirectAttributes.addAttribute("khoa", search.getKhoa());
        }
        if (safeTrim(search.getLoai()) != null) {
            redirectAttributes.addAttribute("loai", search.getLoai());
        }
    }

    public String resolveExportErrorMessage(RuntimeException ex) {
        String message = safeTrim(ex == null ? null : ex.getMessage());
        return message != null ? message : "Không thể xuất báo cáo. Vui lòng thử lại.";
    }

    public String resolveUsername(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        return safeTrim(username);
    }

    public String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = safeTrim(request.getHeader("X-Forwarded-For"));
        if (forwarded != null) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex >= 0 ? safeTrim(forwarded.substring(0, commaIndex)) : forwarded;
        }
        return safeTrim(request.getRemoteAddr());
    }

    public void applyRewardCreateRedirectAttributes(RedirectAttributes redirectAttributes,
                                                    ConductRewardCreateRequest request) {
        if (redirectAttributes == null || request == null) {
            return;
        }
        if (safeTrim(request.getQ()) != null) {
            redirectAttributes.addAttribute("q", request.getQ());
        }
        if (safeTrim(request.getKhoi()) != null) {
            redirectAttributes.addAttribute("khoi", request.getKhoi());
        }
        if (safeTrim(request.getKhoa()) != null) {
            redirectAttributes.addAttribute("khoa", request.getKhoa());
        }
        if (safeTrim(request.getLop()) != null) {
            redirectAttributes.addAttribute("lop", request.getLop());
        }
    }

    public String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
