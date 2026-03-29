package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.ConductEventUpsertRequest;
import com.quanly.webdiem.model.service.admin.ConductListExportService;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.admin.ConductManagementService.ConductPageResult;
import com.quanly.webdiem.model.service.admin.ConductManagementService.ConductRow;
import com.quanly.webdiem.model.service.admin.ConductManagementService.ConductStats;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateFilter;
import com.quanly.webdiem.model.service.admin.ConductRewardCreatePageData;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateRequest;
import com.quanly.webdiem.model.service.admin.ConductStudentCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/conduct")
public class ConductListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConductListController.class);
    private static final String PAGE_TITLE_CONDUCT_DISCIPLINE_CREATE = "Thêm kỷ luật";
    private static final String PAGE_TITLE_CONDUCT = "Khen thưởng / Kỷ luật";
    private static final String PAGE_TITLE_CONDUCT_CREATE = "Thêm khen thưởng";
    private static final String PAGE_TITLE_CONDUCT_INFO = "Thông tin quyết định";
    private static final String PAGE_TITLE_CONDUCT_EDIT = "Sửa quyết định";
    private static final String PAGE_ERROR_MESSAGE = "Không thể tải danh sách khen thưởng/kỷ luật.";
    private static final DateTimeFormatter EXPORT_FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ConductManagementService conductManagementService;
    private final ConductListExportService conductListExportService;
    private final ActivityLogService activityLogService;

    public ConductListController(ConductManagementService conductManagementService,
                                 ConductListExportService conductListExportService,
                                 ActivityLogService activityLogService) {
        this.conductManagementService = conductManagementService;
        this.conductListExportService = conductListExportService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public String conductPage(@ModelAttribute("search") ConductSearch search, Model model) {
        ConductPageResult pageResult;
        ConductStats stats;
        List<String> grades;
        List<ConductManagementService.FilterOption> classes;
        List<ConductManagementService.FilterOption> courses;
        List<ActivityLogService.ConductActivityItem> activityLogs;

        try {
            pageResult = conductManagementService.search(search);
            stats = conductManagementService.getStats(search);
            grades = conductManagementService.getGrades();
            classes = conductManagementService.getClasses();
            courses = conductManagementService.getCourses();
            activityLogs = activityLogService.getRecentConductActivities(null, 20);
        } catch (Exception ex) {
            LOGGER.error("Lỗi tải trang khen thưởng/kỷ luật", ex);
            pageResult = new ConductPageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new ConductStats(0, 0, 0, 0, 0);
            grades = List.of();
            classes = List.of();
            courses = List.of();
            activityLogs = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", PAGE_ERROR_MESSAGE);
        }

        model.addAttribute("activePage", "conduct");
        model.addAttribute("pageTitle", PAGE_TITLE_CONDUCT);
        model.addAttribute("records", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("stats", stats);
        model.addAttribute("grades", grades);
        model.addAttribute("classOptions", classes);
        model.addAttribute("courseOptions", courses);
        model.addAttribute("activityLogs", activityLogs);
        return "admin/conduct";
    }

    @GetMapping("/reward/create")
    public String rewardCreatePage(@ModelAttribute("filter") ConductRewardCreateFilter filter,
                                   Model model) {
        try {
            ConductRewardCreatePageData pageData = conductManagementService.getRewardCreatePageData(filter);
            model.addAttribute("pageData", pageData);
            model.addAttribute("filter", pageData.getFilter());
            model.addAttribute("form", new ConductRewardCreateRequest());
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải trang thêm khen thưởng", ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ex.getMessage());
            model.addAttribute("pageData", new ConductRewardCreatePageData(
                    new ConductRewardCreateFilter(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    null
            ));
            model.addAttribute("filter", new ConductRewardCreateFilter());
            model.addAttribute("form", new ConductRewardCreateRequest());
        }
        model.addAttribute("activePage", "conduct");
        model.addAttribute("pageTitle", PAGE_TITLE_CONDUCT_CREATE);
        return "admin/conduct-create";
    }

    @PostMapping("/reward/create")
    public String rewardCreateSubmit(@ModelAttribute("form") ConductRewardCreateRequest form,
                                     Authentication authentication,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        try {
            conductManagementService.createReward(form);
            ConductRow latest = conductManagementService.getLatestEventByStudentAndType(
                    form.getStudentId(),
                    ConductManagementService.LOAI_KHEN_THUONG
            );
            activityLogService.logConductCreated(
                    ConductManagementService.LOAI_KHEN_THUONG,
                    latest == null ? null : latest.getEventId(),
                    latest == null ? form.getStudentId() : latest.getIdHocSinh(),
                    latest == null ? null : latest.getTenHocSinh(),
                    latest == null ? form.getSoQuyetDinh() : latest.getSoQuyetDinh(),
                    resolveUsername(authentication),
                    resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Thêm khen thưởng thành công.");
            return "redirect:/admin/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        applyRewardCreateRedirectAttributes(redirectAttributes, form);
        redirectAttributes.addAttribute("studentId", form.getStudentId());
        return "redirect:/admin/conduct/reward/create";
    }

    @GetMapping("/discipline/create")
    public String disciplineCreatePage(@ModelAttribute("filter") ConductRewardCreateFilter filter,
                                       Model model) {
        try {
            ConductRewardCreatePageData pageData = conductManagementService.getRewardCreatePageData(filter);
            model.addAttribute("pageData", pageData);
            model.addAttribute("filter", pageData.getFilter());
            model.addAttribute("form", new ConductRewardCreateRequest());
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải trang thêm kỷ luật", ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ex.getMessage());
            model.addAttribute("pageData", new ConductRewardCreatePageData(
                    new ConductRewardCreateFilter(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    null
            ));
            model.addAttribute("filter", new ConductRewardCreateFilter());
            model.addAttribute("form", new ConductRewardCreateRequest());
        }
        model.addAttribute("activePage", "conduct");
        model.addAttribute("pageTitle", PAGE_TITLE_CONDUCT_DISCIPLINE_CREATE);
        return "admin/conduct-discipline-create";
    }

    @PostMapping("/discipline/create")
    public String disciplineCreateSubmit(@ModelAttribute("form") ConductRewardCreateRequest form,
                                         Authentication authentication,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        try {
            conductManagementService.createDiscipline(form);
            ConductRow latest = conductManagementService.getLatestEventByStudentAndType(
                    form.getStudentId(),
                    ConductManagementService.LOAI_KY_LUAT
            );
            activityLogService.logConductCreated(
                    ConductManagementService.LOAI_KY_LUAT,
                    latest == null ? null : latest.getEventId(),
                    latest == null ? form.getStudentId() : latest.getIdHocSinh(),
                    latest == null ? null : latest.getTenHocSinh(),
                    latest == null ? form.getSoQuyetDinh() : latest.getSoQuyetDinh(),
                    resolveUsername(authentication),
                    resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Thêm kỷ luật thành công.");
            return "redirect:/admin/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        applyRewardCreateRedirectAttributes(redirectAttributes, form);
        redirectAttributes.addAttribute("studentId", form.getStudentId());
        return "redirect:/admin/conduct/discipline/create";
    }

    @GetMapping("/reward/suggest-students")
    @ResponseBody
    public List<ConductStudentCandidate> suggestRewardStudents(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "khoi", required = false) String khoi,
            @RequestParam(value = "lop", required = false) String lop,
            @RequestParam(value = "khoa", required = false) String khoa) {
        return conductManagementService.suggestStudentsForReward(q, khoi, lop, khoa);
    }

    @GetMapping("/suggest-students")
    @ResponseBody
    public List<ConductStudentCandidate> suggestManagementStudents(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "khoi", required = false) String khoi,
            @RequestParam(value = "lop", required = false) String lop,
            @RequestParam(value = "khoa", required = false) String khoa) {
        return conductManagementService.suggestStudentsForReward(q, khoi, lop, khoa);
    }

    @GetMapping("/{eventId}/info")
    public String conductInfoPage(@PathVariable("eventId") Long eventId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            ConductRow detail = conductManagementService.getEventDetail(eventId);
            model.addAttribute("detail", detail);
            model.addAttribute("activePage", "conduct");
            model.addAttribute("pageTitle", PAGE_TITLE_CONDUCT_INFO);
            return "admin/conduct-info";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/conduct";
        }
    }

    @GetMapping("/{eventId}/edit")
    public String conductEditPage(@PathVariable("eventId") Long eventId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            ConductEventUpsertRequest form = conductManagementService.getEditData(eventId);
            ConductRow detail = conductManagementService.getEventDetail(eventId);
            model.addAttribute("form", form);
            model.addAttribute("detail", detail);
            model.addAttribute("activePage", "conduct");
            model.addAttribute("pageTitle", PAGE_TITLE_CONDUCT_EDIT);
            return "admin/conduct-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/conduct";
        }
    }

    @PostMapping("/{eventId}/edit")
    public String conductEditSubmit(@PathVariable("eventId") Long eventId,
                                    @ModelAttribute("form") ConductEventUpsertRequest form,
                                    Authentication authentication,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        form.setEventId(eventId);
        try {
            ConductRow beforeUpdate = conductManagementService.getEventDetail(eventId);
            conductManagementService.updateEvent(form);
            ConductRow afterUpdate = conductManagementService.getEventDetail(eventId);
            ConductRow logSource = afterUpdate != null ? afterUpdate : beforeUpdate;
            activityLogService.logConductUpdated(
                    logSource == null ? null : logSource.getLoai(),
                    logSource == null ? eventId : logSource.getEventId(),
                    logSource == null ? null : logSource.getIdHocSinh(),
                    logSource == null ? null : logSource.getTenHocSinh(),
                    logSource == null ? null : logSource.getSoQuyetDinh(),
                    resolveUsername(authentication),
                    resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Sửa quyết định thành công.");
            return "redirect:/admin/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/conduct/" + eventId + "/edit";
        }
    }

    @PostMapping("/{eventId}/delete")
    public String deleteConductEvent(@PathVariable("eventId") Long eventId,
                                     Authentication authentication,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        try {
            ConductRow existing = conductManagementService.getEventDetail(eventId);
            conductManagementService.deleteEvent(eventId);
            activityLogService.logConductDeleted(
                    existing == null ? null : existing.getLoai(),
                    existing == null ? eventId : existing.getEventId(),
                    existing == null ? null : existing.getIdHocSinh(),
                    existing == null ? null : existing.getTenHocSinh(),
                    existing == null ? null : existing.getSoQuyetDinh(),
                    resolveUsername(authentication),
                    resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Đã xóa quyết định.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/conduct";
    }

    @GetMapping("/export/excel")
    public Object exportListExcel(@ModelAttribute("search") ConductSearch search,
                                  RedirectAttributes redirectAttributes) {
        try {
            List<ConductRow> rows = conductManagementService.getRowsForExport(search);
            if (rows.isEmpty()) {
                throw new RuntimeException("Không có dữ liệu khen thưởng/kỷ luật phù hợp để xuất Excel.");
            }

            byte[] content = conductListExportService.exportExcel(rows, search);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .headers(downloadHeaders(buildListExportFileName("xlsx")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveExportErrorMessage(ex));
            applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/admin/conduct";
        }
    }

    @GetMapping("/export/pdf")
    public Object exportListPdf(@ModelAttribute("search") ConductSearch search,
                                RedirectAttributes redirectAttributes) {
        try {
            List<ConductRow> rows = conductManagementService.getRowsForExport(search);
            if (rows.isEmpty()) {
                throw new RuntimeException("Không có dữ liệu khen thưởng/kỷ luật phù hợp để xuất PDF.");
            }

            byte[] content = conductListExportService.exportPdf(rows, search);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .headers(downloadHeaders(buildListExportFileName("pdf")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveExportErrorMessage(ex));
            applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/admin/conduct";
        }
    }

    private HttpHeaders downloadHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build()
        );
        return headers;
    }

    private String buildListExportFileName(String extension) {
        return "bao-cao-khen-thuong-ky-luat-" + EXPORT_FILE_DATE.format(LocalDate.now()) + "." + extension;
    }

    private void applySearchRedirectAttributes(RedirectAttributes redirectAttributes, ConductSearch search) {
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

    private String resolveExportErrorMessage(RuntimeException ex) {
        String message = safeTrim(ex == null ? null : ex.getMessage());
        return message != null ? message : "Không thể xuất báo cáo. Vui lòng thử lại.";
    }

    private String resolveUsername(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        return safeTrim(username);
    }

    private String resolveIpAddress(HttpServletRequest request) {
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

    private void applyRewardCreateRedirectAttributes(RedirectAttributes redirectAttributes,
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

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
