package com.quanly.webdiem.controller.teacher_subject;

import com.quanly.webdiem.model.search.TeacherScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherScoreCreateService;
import com.quanly.webdiem.model.service.teacher.TeacherScoreEditService;
import com.quanly.webdiem.model.service.teacher.TeacherScoreListExportService;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.CreateScopeData;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ScoreDashboardData;
import com.quanly.webdiem.model.service.teacher_subject.TeacherSubjectScoreService;
import com.quanly.webdiem.model.service.teacher_subject.TeacherSubjectScoreService.TeacherSubjectScorePageData;
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
@RequestMapping("/teacher-subject/score")
@PreAuthorize("hasAnyAuthority('ROLE_GVBM','ROLE_Giao_vien','ROLE_Admin')")
public class TeacherSubjectScoreController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherSubjectScoreController.class);
    private static final DateTimeFormatter EXPORT_FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String PAGE_TITLE = "Quản lý điểm số giáo viên bộ môn";
    private static final String PAGE_EDIT_TITLE = "Nhập/Sửa điểm môn học";
    private static final String PAGE_CREATE_TITLE = "Thêm điểm số lớp bộ môn";
    private static final String PAGE_DETAIL_TITLE = "Chi tiết điểm số";

    private final TeacherSubjectPageModelHelper pageModelHelper;
    private final TeacherSubjectScoreService teacherSubjectScoreService;
    private final TeacherScoreListExportService teacherScoreListExportService;
    private final TeacherScoreService teacherScoreService;
    private final TeacherScoreCreateService teacherScoreCreateService;
    private final TeacherScoreEditService teacherScoreEditService;

    public TeacherSubjectScoreController(TeacherSubjectPageModelHelper pageModelHelper,
                                         TeacherSubjectScoreService teacherSubjectScoreService,
                                         TeacherScoreListExportService teacherScoreListExportService,
                                         TeacherScoreService teacherScoreService,
                                         TeacherScoreCreateService teacherScoreCreateService,
                                         TeacherScoreEditService teacherScoreEditService) {
        this.pageModelHelper = pageModelHelper;
        this.teacherSubjectScoreService = teacherSubjectScoreService;
        this.teacherScoreListExportService = teacherScoreListExportService;
        this.teacherScoreService = teacherScoreService;
        this.teacherScoreCreateService = teacherScoreCreateService;
        this.teacherScoreEditService = teacherScoreEditService;
    }

    @GetMapping
    public String scorePage(@ModelAttribute("search") TeacherScoreSearch search,
                            Authentication authentication,
                            Model model) {
        String username = pageModelHelper.resolveUsername(authentication);
        pageModelHelper.applyBasePage(model, "score", PAGE_TITLE);

        try {
            TeacherSubjectScorePageData pageData = teacherSubjectScoreService.loadPageData(username, search);
            ScoreDashboardData dashboardData = pageData.getDashboardData();

            model.addAttribute("scorePageData", pageData);
            model.addAttribute("scoreData", dashboardData);
            model.addAttribute("search", dashboardData.getSearch());
            model.addAttribute("scope", pageData.getScope());

            if (dashboardData.getTeacherId() == null) {
                model.addAttribute("warningMessage", "Không xác định được giáo viên từ tài khoản đăng nhập.");
            } else if (dashboardData.getSchoolYear() == null || dashboardData.getSchoolYear().isBlank()) {
                model.addAttribute("warningMessage", "Không xác định được năm học hiện tại để hiển thị điểm.");
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải trang quản lý điểm cho GVBM", ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Không thể tải dữ liệu điểm số của giáo viên bộ môn.");
            model.addAttribute("scorePageData", teacherSubjectScoreService.emptyPageData(search));
            model.addAttribute("scoreData", ScoreDashboardData.empty(search, null));
        }
        return "teacher-subject/score";
    }

    @GetMapping("/export/excel")
    public Object exportExcel(@ModelAttribute("search") TeacherScoreSearch search,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        return exportScoreFile(search, authentication, redirectAttributes, true);
    }

    @GetMapping("/export/pdf")
    public Object exportPdf(@ModelAttribute("search") TeacherScoreSearch search,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        return exportScoreFile(search, authentication, redirectAttributes, false);
    }

    @GetMapping("/create")
    public String createPage(@ModelAttribute("filter") ScoreCreateService.ScoreCreateFilter filter,
                             Authentication authentication,
                             Model model) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = teacherSubjectScoreService.resolveScope(username);
        pageModelHelper.applyBasePage(model, "score", PAGE_CREATE_TITLE);

        try {
            CreateScopeData createScope = teacherScoreService.buildCreateScopeData(username, scope, filter.getLop(), filter.getMon());
            if (createScope.getClassOptions().isEmpty()) {
                model.addAttribute("warningMessage", "Bạn chưa được phân công lớp bộ môn trong năm học hiện tại.");
            } else if (createScope.getSelectedClassId() != null
                    && (createScope.getSelectedSubjectId() == null || createScope.getSelectedSubjectId().isBlank())) {
                model.addAttribute("warningMessage", "Lớp đã chọn chưa có môn được phân công cho tài khoản này.");
            }

            filter.setNamHoc(createScope.getSchoolYear());
            filter.setHocKy(teacherScoreService.normalizeCreateSemester(filter.getHocKy()));
            filter.setLop(createScope.getSelectedClassId());
            filter.setMon(createScope.getSelectedSubjectId());
            String currentTeacherDisplay = buildTeacherDisplay(createScope.getTeacherId(), scope == null ? null : scope.getTeacherName());
            if (currentTeacherDisplay != null) {
                filter.setTeacherHk1(currentTeacherDisplay);
                filter.setTeacherHk2(currentTeacherDisplay);
            }
            if (filter.getApplyFilter() == null || filter.getApplyFilter().isBlank()) {
                filter.setApplyFilter("0");
            }

            ScoreCreateService.ScoreCreatePageData createData = teacherScoreCreateService.getCreatePageData(filter);
            model.addAttribute("createScope", createScope);
            model.addAttribute("createData", createData);
            model.addAttribute("filter", createData.getFilter());
            model.addAttribute("ruleItems",
                    teacherScoreService.loadFrequentRuleItems(username, scope, createScope.getSelectedClassId()));
        } catch (RuntimeException ex) {
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ex.getMessage());
            model.addAttribute("createScope", CreateScopeData.empty(filter.getNamHoc()));
            model.addAttribute("createData", null);
            model.addAttribute("filter", filter);
            model.addAttribute("ruleItems", List.of());
        }
        return "teacher-subject/score-create";
    }

    @PostMapping("/create")
    public String createSubmit(@ModelAttribute ScoreCreateService.ScoreSaveRequest request,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = teacherSubjectScoreService.resolveScope(username);

        try {
            CreateScopeData createScope = teacherScoreService.buildCreateScopeData(username, scope, request.getLop(), request.getMon());
            request.setNamHoc(createScope.getSchoolYear());
            request.setHocKy(teacherScoreService.normalizeCreateSemester(request.getHocKy()));
            request.setLop(createScope.getSelectedClassId());
            request.setMon(createScope.getSelectedSubjectId());

            if (request.getLop() == null || request.getMon() == null) {
                throw new RuntimeException("Vui lòng chọn lớp bộ môn và môn học hợp lệ.");
            }
            if (request.getStudentId() == null || request.getStudentId().isBlank()) {
                throw new RuntimeException("Vui lòng chọn học sinh hợp lệ theo lớp đã chọn.");
            }

            if ("0".equals(request.getHocKy())) {
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), "1");
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), "2");
            } else {
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), request.getHocKy());
            }

            teacherScoreCreateService.save(request);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Đã lưu điểm thành công.");
            return "redirect:/teacher-subject/score";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            addIfPresent(redirectAttributes, "namHoc", request.getNamHoc());
            addIfPresent(redirectAttributes, "hocKy", request.getHocKy());
            addIfPresent(redirectAttributes, "lop", request.getLop());
            addIfPresent(redirectAttributes, "mon", request.getMon());
            addIfPresent(redirectAttributes, "q", request.getQ());
            addIfPresent(redirectAttributes, "studentId", request.getStudentId());
            return "redirect:/teacher-subject/score/create";
        }
    }

    @GetMapping("/suggest/students")
    @ResponseBody
    public List<ScoreCreateService.StudentItem> suggestStudents(@RequestParam(value = "classId", required = false) String classId,
                                                                @RequestParam(value = "q", required = false) String q,
                                                                Authentication authentication) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = teacherSubjectScoreService.resolveScope(username);
        if (!teacherScoreService.canUseSubjectClass(username, scope, classId)) {
            return List.of();
        }
        return teacherScoreCreateService.suggestStudents(classId, q);
    }

    @GetMapping("/detail")
    public String detailPage(@RequestParam("studentId") String studentId,
                             @RequestParam("subjectId") String subjectId,
                             @RequestParam("namHoc") String namHoc,
                             @RequestParam(value = "hocKy", required = false) String hocKy,
                             @RequestParam(value = "returnQ", required = false) String returnQ,
                             @RequestParam(value = "returnKhoa", required = false) String returnKhoa,
                             @RequestParam(value = "returnHocLuc", required = false) String returnHocLuc,
                             @RequestParam(value = "returnMon", required = false) String returnMon,
                             @RequestParam(value = "returnHocKy", required = false) String returnHocKy,
                             @RequestParam(value = "returnClassId", required = false) String returnClassId,
                             @RequestParam(value = "returnPage", required = false) String returnPage,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = teacherSubjectScoreService.resolveScope(username);
        pageModelHelper.applyBasePage(model, "score", PAGE_DETAIL_TITLE);

        String selectedSemester = teacherScoreService.normalizeCreateSemester(hocKy);

        try {
            teacherScoreService.assertCanViewScore(username, scope, studentId, subjectId, namHoc, selectedSemester);

            ScoreCreateService.ScoreCreateFilter filter = new ScoreCreateService.ScoreCreateFilter();
            filter.setStudentId(studentId);
            filter.setMon(subjectId);
            filter.setNamHoc(namHoc);
            filter.setHocKy(selectedSemester);
            filter.setApplyFilter("1");

            ScoreCreateService.ScoreCreatePageData detailData = teacherScoreEditService.getPageData(filter);
            if (detailData == null || !detailData.isReadyForInput()) {
                throw new RuntimeException("Không đủ dữ liệu để hiển thị chi tiết điểm.");
            }

            model.addAttribute("detailData", detailData);
            model.addAttribute("selectedHocKy", selectedSemester);
            model.addAttribute("returnQ", returnQ);
            model.addAttribute("returnKhoa", returnKhoa);
            model.addAttribute("returnHocLuc", returnHocLuc);
            model.addAttribute("returnMon", returnMon);
            model.addAttribute("returnHocKy", returnHocKy);
            model.addAttribute("returnClassId", returnClassId);
            model.addAttribute("returnPage", returnPage);
            return "teacher-subject/score-detail";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            appendReturnSearch(redirectAttributes, returnQ, returnKhoa, returnHocLuc, returnMon, returnHocKy, returnClassId, returnPage);
            return "redirect:/teacher-subject/score";
        }
    }

    @GetMapping("/edit")
    public String editPage(@RequestParam("studentId") String studentId,
                           @RequestParam("subjectId") String subjectId,
                           @RequestParam("namHoc") String namHoc,
                           @RequestParam("hocKy") String hocKy,
                           @RequestParam(value = "returnQ", required = false) String returnQ,
                           @RequestParam(value = "returnKhoa", required = false) String returnKhoa,
                           @RequestParam(value = "returnHocLuc", required = false) String returnHocLuc,
                           @RequestParam(value = "returnMon", required = false) String returnMon,
                           @RequestParam(value = "returnHocKy", required = false) String returnHocKy,
                           @RequestParam(value = "returnClassId", required = false) String returnClassId,
                           @RequestParam(value = "returnPage", required = false) String returnPage,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = teacherSubjectScoreService.resolveScope(username);
        pageModelHelper.applyBasePage(model, "score", PAGE_EDIT_TITLE);

        try {
            teacherScoreService.assertCanEditScore(username, scope, studentId, subjectId, namHoc, hocKy);

            ScoreCreateService.ScoreCreateFilter filter = new ScoreCreateService.ScoreCreateFilter();
            filter.setStudentId(studentId);
            filter.setMon(subjectId);
            filter.setNamHoc(namHoc);
            filter.setHocKy(normalizeEditSemester(hocKy));
            filter.setApplyFilter("1");

            ScoreCreateService.ScoreCreatePageData editData = teacherScoreEditService.getPageData(filter);
            if (editData == null || !editData.isReadyForInput()) {
                throw new RuntimeException("Không đủ dữ liệu để mở màn hình nhập/sửa điểm.");
            }

            model.addAttribute("editData", editData);
            model.addAttribute("returnQ", returnQ);
            model.addAttribute("returnKhoa", returnKhoa);
            model.addAttribute("returnHocLuc", returnHocLuc);
            model.addAttribute("returnMon", returnMon);
            model.addAttribute("returnHocKy", returnHocKy);
            model.addAttribute("returnClassId", returnClassId);
            model.addAttribute("returnPage", returnPage);
            return "teacher-subject/score-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            appendReturnSearch(redirectAttributes, returnQ, returnKhoa, returnHocLuc, returnMon, returnHocKy, returnClassId, returnPage);
            return "redirect:/teacher-subject/score";
        }
    }

    @PostMapping("/edit")
    public String editSubmit(@ModelAttribute ScoreCreateService.ScoreSaveRequest request,
                             @RequestParam(value = "returnQ", required = false) String returnQ,
                             @RequestParam(value = "returnKhoa", required = false) String returnKhoa,
                             @RequestParam(value = "returnHocLuc", required = false) String returnHocLuc,
                             @RequestParam(value = "returnMon", required = false) String returnMon,
                             @RequestParam(value = "returnHocKy", required = false) String returnHocKy,
                             @RequestParam(value = "returnClassId", required = false) String returnClassId,
                             @RequestParam(value = "returnPage", required = false) String returnPage,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = teacherSubjectScoreService.resolveScope(username);

        String semester = normalizeEditSemester(request.getHocKy());
        request.setHocKy(semester);

        try {
            if ("0".equals(semester)) {
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), "1");
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), "2");
            } else {
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), semester);
            }
            teacherScoreEditService.save(request);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Đã lưu điểm thành công.");
            appendReturnSearch(redirectAttributes, returnQ, returnKhoa, returnHocLuc, returnMon, returnHocKy, returnClassId, returnPage);
            return "redirect:/teacher-subject/score";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            redirectAttributes.addAttribute("studentId", request.getStudentId());
            redirectAttributes.addAttribute("subjectId", request.getMon());
            redirectAttributes.addAttribute("namHoc", request.getNamHoc());
            redirectAttributes.addAttribute("hocKy", semester);
            addIfPresent(redirectAttributes, "returnQ", returnQ);
            addIfPresent(redirectAttributes, "returnKhoa", returnKhoa);
            addIfPresent(redirectAttributes, "returnHocLuc", returnHocLuc);
            addIfPresent(redirectAttributes, "returnMon", returnMon);
            addIfPresent(redirectAttributes, "returnHocKy", returnHocKy);
            addIfPresent(redirectAttributes, "returnClassId", returnClassId);
            addIfPresent(redirectAttributes, "returnPage", returnPage);
            return "redirect:/teacher-subject/score/edit";
        }
    }

    @PostMapping("/delete")
    public String deleteScoreGroup(@RequestParam("studentId") String studentId,
                                   @RequestParam("subjectId") String subjectId,
                                   @RequestParam("namHoc") String namHoc,
                                   @RequestParam(value = "returnQ", required = false) String returnQ,
                                   @RequestParam(value = "returnKhoa", required = false) String returnKhoa,
                                   @RequestParam(value = "returnHocLuc", required = false) String returnHocLuc,
                                   @RequestParam(value = "returnMon", required = false) String returnMon,
                                   @RequestParam(value = "returnHocKy", required = false) String returnHocKy,
                                   @RequestParam(value = "returnClassId", required = false) String returnClassId,
                                   @RequestParam(value = "returnPage", required = false) String returnPage,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = teacherSubjectScoreService.resolveScope(username);

        try {
            teacherScoreService.assertCanDeleteScoreGroup(username, scope, studentId, subjectId, namHoc);
            teacherScoreEditService.deleteScoreGroup(studentId, subjectId, namHoc);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Đã xóa nhóm điểm thành công.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }

        appendReturnSearch(redirectAttributes, returnQ, returnKhoa, returnHocLuc, returnMon, returnHocKy, returnClassId, returnPage);
        return "redirect:/teacher-subject/score";
    }

    private Object exportScoreFile(TeacherScoreSearch search,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes,
                                   boolean excel) {
        String username = pageModelHelper.resolveUsername(authentication);

        try {
            ScoreDashboardData dashboardData = teacherSubjectScoreService.loadDashboardForExport(username, search);
            List<TeacherScoreService.ScoreRow> rows = dashboardData.getRows();
            if (rows == null || rows.isEmpty()) {
                throw new RuntimeException(excel
                        ? "Không có dữ liệu điểm phù hợp bộ lọc để xuất Excel."
                        : "Không có dữ liệu điểm phù hợp bộ lọc để xuất PDF.");
            }

            byte[] content = excel
                    ? teacherScoreListExportService.exportExcel(rows, dashboardData)
                    : teacherScoreListExportService.exportPdf(rows, dashboardData);

            return ResponseEntity.ok()
                    .contentType(excel
                            ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            : MediaType.APPLICATION_PDF)
                    .headers(downloadHeaders(buildListExportFileName(excel ? "xlsx" : "pdf")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveExportErrorMessage(ex));
            applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/teacher-subject/score";
        }
    }

    private void applySearchRedirectAttributes(RedirectAttributes redirectAttributes, TeacherScoreSearch search) {
        if (search == null) {
            return;
        }
        addIfPresent(redirectAttributes, "q", search.getQ());
        addIfPresent(redirectAttributes, "khoa", search.getKhoa());
        addIfPresent(redirectAttributes, "hocLuc", search.getHocLuc());
        addIfPresent(redirectAttributes, "classId", search.getClassId());
        addIfPresent(redirectAttributes, "mon", search.getMon());
        addIfPresent(redirectAttributes, "hocKy", search.getHocKy());
        if (search.getPage() != null && search.getPage() > 1) {
            redirectAttributes.addAttribute("page", search.getPage());
        }
    }

    private HttpHeaders downloadHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        return headers;
    }

    private String buildListExportFileName(String extension) {
        return "teacher-subject-score-" + EXPORT_FILE_DATE.format(LocalDate.now()) + "." + extension;
    }

    private String resolveExportErrorMessage(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "Không thể xuất danh sách điểm của giáo viên bộ môn.";
        }
        return message;
    }

    private void appendReturnSearch(RedirectAttributes redirectAttributes,
                                    String returnQ,
                                    String returnKhoa,
                                    String returnHocLuc,
                                    String returnMon,
                                    String returnHocKy,
                                    String returnClassId,
                                    String returnPage) {
        addIfPresent(redirectAttributes, "q", returnQ);
        addIfPresent(redirectAttributes, "khoa", returnKhoa);
        addIfPresent(redirectAttributes, "hocLuc", returnHocLuc);
        addIfPresent(redirectAttributes, "mon", returnMon);
        addIfPresent(redirectAttributes, "hocKy", returnHocKy);
        addIfPresent(redirectAttributes, "classId", returnClassId);
        addIfPresent(redirectAttributes, "page", returnPage);
    }

    private void addIfPresent(RedirectAttributes redirectAttributes, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        redirectAttributes.addAttribute(key, value.trim());
    }

    private String normalizeEditSemester(String hocKy) {
        if ("2".equals(hocKy) || "0".equals(hocKy)) {
            return hocKy;
        }
        return "1";
    }

    private String buildTeacherDisplay(String teacherId, String teacherName) {
        if (teacherId == null || teacherId.isBlank()) {
            return null;
        }
        String normalizedTeacherId = teacherId.trim().toUpperCase();
        if (teacherName == null || teacherName.isBlank()) {
            return normalizedTeacherId;
        }
        return teacherName.trim() + " (" + normalizedTeacherId + ")";
    }
}
