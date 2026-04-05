package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.ConductEventUpsertRequest;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateFilter;
import com.quanly.webdiem.model.service.admin.ConductRewardCreatePageData;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateRequest;
import com.quanly.webdiem.model.service.admin.ConductStudentCandidate;
import com.quanly.webdiem.model.service.teacher.TeacherConductCreateService;
import com.quanly.webdiem.model.service.teacher.TeacherConductEditService;
import com.quanly.webdiem.model.service.teacher.TeacherConductExportService;
import com.quanly.webdiem.model.service.teacher.TeacherConductService;
import com.quanly.webdiem.model.service.teacher.TeacherConductService.TeacherConductDashboardData;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/teacher/conduct")
@PreAuthorize("hasAnyAuthority('ROLE_Giao_vien','ROLE_GVCN','ROLE_Admin')")
public class TeacherConductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherConductController.class);
    private static final DateTimeFormatter EXPORT_FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ERROR_NO_HOMEROOM_CLASS = "Tài khoản chưa được phân công lớp chủ nhiệm.";
    private static final String PAGE_TITLE = "Khen thưởng / Kỷ luật lớp chủ nhiệm";
    private static final String PAGE_REWARD_CREATE_TITLE = "Thêm khen thưởng lớp chủ nhiệm";
    private static final String PAGE_DISCIPLINE_CREATE_TITLE = "Thêm kỷ luật lớp chủ nhiệm";
    private static final String PAGE_INFO_TITLE = "Thông tin quyết định";
    private static final String PAGE_EDIT_TITLE = "Sửa quyết định";

    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;
    private final TeacherConductService teacherConductService;
    private final TeacherConductCreateService teacherConductCreateService;
    private final TeacherConductEditService teacherConductEditService;
    private final TeacherConductExportService teacherConductExportService;
    private final ActivityLogService activityLogService;

    public TeacherConductController(TeacherStudentScopeService scopeService,
                                    TeacherPageModelHelper pageModelHelper,
                                    TeacherConductService teacherConductService,
                                    TeacherConductCreateService teacherConductCreateService,
                                    TeacherConductEditService teacherConductEditService,
                                    TeacherConductExportService teacherConductExportService,
                                    ActivityLogService activityLogService) {
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
        this.teacherConductService = teacherConductService;
        this.teacherConductCreateService = teacherConductCreateService;
        this.teacherConductEditService = teacherConductEditService;
        this.teacherConductExportService = teacherConductExportService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public String conductPage(@ModelAttribute("search") ConductSearch search,
                              Authentication authentication,
                              Model model) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        pageModelHelper.applyBasePage(model, "conduct", PAGE_TITLE, scope);

        try {
            TeacherConductDashboardData dashboardData = teacherConductService.loadDashboard(scope, search);
            model.addAttribute("search", dashboardData.getSearch());
            model.addAttribute("records", dashboardData.getPageData().getItems());
            model.addAttribute("pageData", dashboardData.getPageData());
            model.addAttribute("stats", dashboardData.getStats());
            model.addAttribute("grades", dashboardData.getGrades());
            model.addAttribute("classOptions", dashboardData.getClassOptions());
            model.addAttribute("courseOptions", dashboardData.getCourseOptions());
            model.addAttribute("activityLogs", dashboardData.getActivityLogs());
            if (!scopeService.hasHomeroomClass(scope)) {
                model.addAttribute("warningMessage", ERROR_NO_HOMEROOM_CLASS);
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải trang khen thưởng/kỷ luật cho giáo viên chủ nhiệm", ex);
            TeacherConductDashboardData emptyData = TeacherConductDashboardData.empty(search);
            model.addAttribute("search", emptyData.getSearch());
            model.addAttribute("records", emptyData.getPageData().getItems());
            model.addAttribute("pageData", emptyData.getPageData());
            model.addAttribute("stats", emptyData.getStats());
            model.addAttribute("grades", emptyData.getGrades());
            model.addAttribute("classOptions", emptyData.getClassOptions());
            model.addAttribute("courseOptions", emptyData.getCourseOptions());
            model.addAttribute("activityLogs", emptyData.getActivityLogs());
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Không thể tải danh sách khen thưởng/kỷ luật của lớp chủ nhiệm.");
        }
        return "teacher/conduct";
    }

    @GetMapping("/reward/create")
    public String rewardCreatePage(@ModelAttribute("filter") ConductRewardCreateFilter filter,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ERROR_NO_HOMEROOM_CLASS);
            return "redirect:/teacher/conduct";
        }

        pageModelHelper.applyBasePage(model, "conduct", PAGE_REWARD_CREATE_TITLE, scope);
        try {
            ConductRewardCreatePageData pageData = teacherConductCreateService.getCreatePageData(scope, filter);
            model.addAttribute("pageData", pageData);
            model.addAttribute("filter", pageData.getFilter());
            model.addAttribute("form", new ConductRewardCreateRequest());
            model.addAttribute("suggestedDecisionNumber", teacherConductCreateService.suggestRewardDecisionNumber());
        } catch (RuntimeException ex) {
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
            model.addAttribute("suggestedDecisionNumber", teacherConductCreateService.suggestRewardDecisionNumber());
        }
        return "teacher/conduct-create";
    }

    @PostMapping("/reward/create")
    public String rewardCreateSubmit(@ModelAttribute("form") ConductRewardCreateRequest form,
                                     Authentication authentication,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ERROR_NO_HOMEROOM_CLASS);
            return "redirect:/teacher/conduct";
        }

        try {
            teacherConductCreateService.applyDefaultRewardDecisionNumber(form);
            teacherConductCreateService.createReward(scope, form);
            ConductManagementService.ConductRow latest = teacherConductCreateService.getLatestReward(scope, form.getStudentId());
            activityLogService.logConductCreated(
                    ConductManagementService.LOAI_KHEN_THUONG,
                    latest == null ? null : latest.getEventId(),
                    latest == null ? form.getStudentId() : latest.getIdHocSinh(),
                    latest == null ? null : latest.getTenHocSinh(),
                    latest == null ? form.getSoQuyetDinh() : latest.getSoQuyetDinh(),
                    username,
                    pageModelHelper.resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Thêm khen thưởng thành công.");
            return "redirect:/teacher/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }

        applyRewardCreateRedirectAttributes(redirectAttributes, form);
        addIfPresent(redirectAttributes, "studentId", form.getStudentId());
        return "redirect:/teacher/conduct/reward/create";
    }

    @GetMapping("/discipline/create")
    public String disciplineCreatePage(@ModelAttribute("filter") ConductRewardCreateFilter filter,
                                       Authentication authentication,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ERROR_NO_HOMEROOM_CLASS);
            return "redirect:/teacher/conduct";
        }

        pageModelHelper.applyBasePage(model, "conduct", PAGE_DISCIPLINE_CREATE_TITLE, scope);
        try {
            ConductRewardCreatePageData pageData = teacherConductCreateService.getCreatePageData(scope, filter);
            model.addAttribute("pageData", pageData);
            model.addAttribute("filter", pageData.getFilter());
            model.addAttribute("form", new ConductRewardCreateRequest());
            model.addAttribute("suggestedDecisionNumber", teacherConductCreateService.suggestDisciplineDecisionNumber());
        } catch (RuntimeException ex) {
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
            model.addAttribute("suggestedDecisionNumber", teacherConductCreateService.suggestDisciplineDecisionNumber());
        }
        return "teacher/conduct-discipline-create";
    }

    @PostMapping("/discipline/create")
    public String disciplineCreateSubmit(@ModelAttribute("form") ConductRewardCreateRequest form,
                                         Authentication authentication,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ERROR_NO_HOMEROOM_CLASS);
            return "redirect:/teacher/conduct";
        }

        try {
            teacherConductCreateService.applyDefaultDisciplineDecisionNumber(form);
            teacherConductCreateService.createDiscipline(scope, form);
            ConductManagementService.ConductRow latest = teacherConductCreateService.getLatestDiscipline(scope, form.getStudentId());
            activityLogService.logConductCreated(
                    ConductManagementService.LOAI_KY_LUAT,
                    latest == null ? null : latest.getEventId(),
                    latest == null ? form.getStudentId() : latest.getIdHocSinh(),
                    latest == null ? null : latest.getTenHocSinh(),
                    latest == null ? form.getSoQuyetDinh() : latest.getSoQuyetDinh(),
                    username,
                    pageModelHelper.resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Thêm kỷ luật thành công.");
            return "redirect:/teacher/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }

        applyRewardCreateRedirectAttributes(redirectAttributes, form);
        addIfPresent(redirectAttributes, "studentId", form.getStudentId());
        return "redirect:/teacher/conduct/discipline/create";
    }

    @GetMapping("/reward/suggest-students")
    @ResponseBody
    public List<ConductStudentCandidate> suggestRewardStudents(@RequestParam(value = "q", required = false) String q,
                                                               Authentication authentication) {
        return suggestStudents(q, authentication);
    }

    @GetMapping("/suggest-students")
    @ResponseBody
    public List<ConductStudentCandidate> suggestStudents(@RequestParam(value = "q", required = false) String q,
                                                         Authentication authentication) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        if (!scopeService.hasHomeroomClass(scope)) {
            return List.of();
        }
        return teacherConductCreateService.suggestStudents(scope, q);
    }

    @GetMapping("/{eventId}/info")
    public String conductInfoPage(@PathVariable("eventId") Long eventId,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        pageModelHelper.applyBasePage(model, "conduct", PAGE_INFO_TITLE, scope);

        try {
            model.addAttribute("detail", teacherConductEditService.getEventDetailInScope(scope, eventId));
            return "teacher/conduct-info";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/teacher/conduct";
        }
    }

    @GetMapping("/{eventId}/edit")
    public String conductEditPage(@PathVariable("eventId") Long eventId,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        pageModelHelper.applyBasePage(model, "conduct", PAGE_EDIT_TITLE, scope);

        try {
            model.addAttribute("detail", teacherConductEditService.getEventDetailInScope(scope, eventId));
            model.addAttribute("form", teacherConductEditService.getEditDataInScope(scope, eventId));
            return "teacher/conduct-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/teacher/conduct";
        }
    }

    @PostMapping("/{eventId}/edit")
    public String conductEditSubmit(@PathVariable("eventId") Long eventId,
                                    @ModelAttribute("form") ConductEventUpsertRequest form,
                                    Authentication authentication,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        form.setEventId(eventId);

        try {
            ConductManagementService.ConductRow beforeUpdate = teacherConductEditService.getEventDetailInScope(scope, eventId);
            teacherConductEditService.updateEvent(scope, form);
            ConductManagementService.ConductRow afterUpdate = teacherConductEditService.getEventDetailInScope(scope, eventId);
            ConductManagementService.ConductRow logSource = afterUpdate != null ? afterUpdate : beforeUpdate;
            activityLogService.logConductUpdated(
                    logSource == null ? null : logSource.getLoai(),
                    logSource == null ? eventId : logSource.getEventId(),
                    logSource == null ? null : logSource.getIdHocSinh(),
                    logSource == null ? null : logSource.getTenHocSinh(),
                    logSource == null ? null : logSource.getSoQuyetDinh(),
                    username,
                    pageModelHelper.resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Sửa quyết định thành công.");
            return "redirect:/teacher/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/teacher/conduct/" + eventId + "/edit";
        }
    }

    @PostMapping("/{eventId}/delete")
    public String deleteConductEvent(@PathVariable("eventId") Long eventId,
                                     Authentication authentication,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);

        try {
            ConductManagementService.ConductRow existing = teacherConductEditService.getEventDetailInScope(scope, eventId);
            teacherConductEditService.deleteEvent(scope, eventId);
            activityLogService.logConductDeleted(
                    existing == null ? null : existing.getLoai(),
                    existing == null ? eventId : existing.getEventId(),
                    existing == null ? null : existing.getIdHocSinh(),
                    existing == null ? null : existing.getTenHocSinh(),
                    existing == null ? null : existing.getSoQuyetDinh(),
                    username,
                    pageModelHelper.resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Đã xóa quyết định.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/teacher/conduct";
    }

    @GetMapping("/export/excel")
    public Object exportListExcel(@ModelAttribute("search") ConductSearch search,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        return exportList(search, authentication, redirectAttributes, true);
    }

    @GetMapping("/export/pdf")
    public Object exportListPdf(@ModelAttribute("search") ConductSearch search,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        return exportList(search, authentication, redirectAttributes, false);
    }

    private Object exportList(ConductSearch search,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes,
                              boolean excel) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ERROR_NO_HOMEROOM_CLASS);
            return "redirect:/teacher/conduct";
        }

        try {
            List<ConductManagementService.ConductRow> rows = teacherConductService.getRowsForExport(scope, search);
            if (rows.isEmpty()) {
                throw new RuntimeException(excel
                        ? "Không có dữ liệu khen thưởng/kỷ luật phù hợp để xuất Excel."
                        : "Không có dữ liệu khen thưởng/kỷ luật phù hợp để xuất PDF.");
            }

            byte[] content = excel
                    ? teacherConductExportService.exportExcel(rows, search)
                    : teacherConductExportService.exportPdf(rows, search);
            MediaType mediaType = excel
                    ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    : MediaType.APPLICATION_PDF;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .headers(downloadHeaders(buildListExportFileName(excel ? "xlsx" : "pdf")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/teacher/conduct";
        }
    }

    private HttpHeaders downloadHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build());
        return headers;
    }

    private String buildListExportFileName(String extension) {
        return "bao-cao-khen-thuong-ky-luat-gvcn-" + EXPORT_FILE_DATE.format(LocalDate.now()) + "." + extension;
    }

    private void applySearchRedirectAttributes(RedirectAttributes redirectAttributes, ConductSearch search) {
        if (redirectAttributes == null || search == null) {
            return;
        }
        addIfPresent(redirectAttributes, "q", search.getQ());
        addIfPresent(redirectAttributes, "loai", search.getLoai());
        if (search.getPage() != null && search.getPage() > 0) {
            redirectAttributes.addAttribute("page", search.getPage());
        }
    }

    private void applyRewardCreateRedirectAttributes(RedirectAttributes redirectAttributes,
                                                     ConductRewardCreateRequest request) {
        if (redirectAttributes == null || request == null) {
            return;
        }
        addIfPresent(redirectAttributes, "q", request.getQ());
        addIfPresent(redirectAttributes, "khoi", request.getKhoi());
        addIfPresent(redirectAttributes, "khoa", request.getKhoa());
        addIfPresent(redirectAttributes, "lop", request.getLop());
    }

    private void addIfPresent(RedirectAttributes redirectAttributes, String key, Object value) {
        if (redirectAttributes == null || key == null || key.isBlank() || value == null) {
            return;
        }
        if (value instanceof String stringValue) {
            String trimmed = stringValue.trim();
            if (!trimmed.isEmpty()) {
                redirectAttributes.addAttribute(key, trimmed);
            }
            return;
        }
        redirectAttributes.addAttribute(key, value);
    }
}
