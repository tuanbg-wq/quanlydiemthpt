package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.ScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.admin.ScoreDetailExportService;
import com.quanly.webdiem.model.service.admin.ScoreListExportService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScoreGroupSummary;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScorePageResult;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScoreRow;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScoreStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/admin/score")
public class ScoreListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreListController.class);
    private static final String PAGE_TITLE_SCORE = "Quản lý điểm số";
    private static final String PAGE_TITLE_SCORE_CREATE = "Thêm điểm số";
    private static final String PAGE_TITLE_SCORE_DETAIL = "Chi tiết điểm";
    private static final String PAGE_TITLE_SCORE_EDIT = "Chỉnh sửa điểm";
    private static final String PAGE_ERROR_MESSAGE = "Không thể tải danh sách điểm số.";
    private static final String FLASH_UPDATE_SUCCESS = "Cập nhật điểm thành công.";
    private static final String FLASH_DELETE_SUCCESS = "Xóa nhóm điểm thành công.";
    private static final String FLASH_CREATE_SUCCESS = "Đã lưu điểm thành công.";
    private static final String PAGE_CREATE_ERROR_MESSAGE = "Không thể tải dữ liệu trang thêm điểm.";
    private static final String SEMESTER_ALL = "0";
    private static final String SEMESTER_1 = "1";
    private static final String SEMESTER_2 = "2";
    private static final DateTimeFormatter EXPORT_FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ScoreManagementService scoreManagementService;
    private final ScoreCreateService scoreCreateService;
    private final ScoreDetailExportService scoreDetailExportService;
    private final ScoreListExportService scoreListExportService;

    public ScoreListController(ScoreManagementService scoreManagementService,
                               ScoreCreateService scoreCreateService,
                               ScoreDetailExportService scoreDetailExportService,
                               ScoreListExportService scoreListExportService) {
        this.scoreManagementService = scoreManagementService;
        this.scoreCreateService = scoreCreateService;
        this.scoreDetailExportService = scoreDetailExportService;
        this.scoreListExportService = scoreListExportService;
    }

    @GetMapping
    public String scorePage(@ModelAttribute("search") ScoreSearch search,
                            Model model) {
        ScorePageResult pageResult;
        ScoreStats stats;
        List<String> grades;
        List<ScoreManagementService.FilterOption> classes;
        List<ScoreManagementService.FilterOption> subjects;
        List<ScoreManagementService.FilterOption> courses;

        try {
            pageResult = scoreManagementService.search(search);
            stats = scoreManagementService.getStats(search);
            grades = scoreManagementService.getGrades();
            classes = scoreManagementService.getClasses();
            subjects = scoreManagementService.getSubjects();
            courses = scoreManagementService.getCourses();
        } catch (Exception ex) {
            LOGGER.error("Lỗi tải trang danh sách điểm số", ex);
            pageResult = new ScorePageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new ScoreStats(0, 0, 0, 0, 0, 0, 0);
            grades = List.of();
            classes = List.of();
            subjects = List.of();
            courses = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", PAGE_ERROR_MESSAGE);
        }

        model.addAttribute("activePage", "score");
        model.addAttribute("pageTitle", PAGE_TITLE_SCORE);
        model.addAttribute("scores", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("stats", stats);
        model.addAttribute("grades", grades);
        model.addAttribute("classOptions", classes);
        model.addAttribute("subjectOptions", subjects);
        model.addAttribute("courseOptions", courses);
        return "admin/score";
    }

    @GetMapping("/export/excel")
    public Object exportListExcel(@ModelAttribute("search") ScoreSearch search,
                                  RedirectAttributes redirectAttributes) {
        try {
            List<ScoreRow> rows = scoreManagementService.getRowsForExport(search);
            if (rows.isEmpty()) {
                throw new RuntimeException("Khong co du lieu diem phu hop bo loc de xuat Excel.");
            }

            byte[] content = scoreListExportService.exportExcel(rows, search);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .headers(downloadHeaders(buildListExportFileName("xlsx")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveExportErrorMessage(ex));
            applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/admin/score";
        }
    }

    @GetMapping("/export/pdf")
    public Object exportListPdf(@ModelAttribute("search") ScoreSearch search,
                                RedirectAttributes redirectAttributes) {
        try {
            List<ScoreRow> rows = scoreManagementService.getRowsForExport(search);
            if (rows.isEmpty()) {
                throw new RuntimeException("Khong co du lieu diem phu hop bo loc de xuat PDF.");
            }

            byte[] content = scoreListExportService.exportPdf(rows, search);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .headers(downloadHeaders(buildListExportFileName("pdf")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveExportErrorMessage(ex));
            applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/admin/score";
        }
    }

    @GetMapping("/create")
    public String createScorePage(@ModelAttribute("filter") ScoreCreateService.ScoreCreateFilter filter,
                                  Model model) {
        try {
            ScoreCreateService.ScoreCreatePageData pageData = scoreCreateService.getCreatePageData(filter);
            model.addAttribute("createData", pageData);
            model.addAttribute("filter", pageData.getFilter());
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải trang thêm điểm số", ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", PAGE_CREATE_ERROR_MESSAGE);
            try {
                ScoreCreateService.ScoreCreatePageData fallbackData =
                        scoreCreateService.getCreatePageData(new ScoreCreateService.ScoreCreateFilter());
                model.addAttribute("createData", fallbackData);
                model.addAttribute("filter", fallbackData.getFilter());
            } catch (RuntimeException ignoreEx) {
                model.addAttribute("createData", null);
                model.addAttribute("filter", new ScoreCreateService.ScoreCreateFilter());
            }
        }

        model.addAttribute("activePage", "score");
        model.addAttribute("pageTitle", PAGE_TITLE_SCORE_CREATE);
        return "admin/score-create";
    }

    @PostMapping("/create")
    public String createScoreSubmit(@ModelAttribute ScoreCreateService.ScoreSaveRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            scoreCreateService.save(request);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_CREATE_SUCCESS);
            return "redirect:/admin/score";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }

        redirectAttributes.addAttribute("namHoc", request.getNamHoc());
        redirectAttributes.addAttribute("hocKy", request.getHocKy());
        redirectAttributes.addAttribute("khoi", request.getKhoi());
        redirectAttributes.addAttribute("khoa", request.getKhoa());
        redirectAttributes.addAttribute("lop", request.getLop());
        redirectAttributes.addAttribute("mon", request.getMon());
        redirectAttributes.addAttribute("q", request.getQ());
        redirectAttributes.addAttribute("studentId", request.getStudentId());
        redirectAttributes.addAttribute("teacherHk1", request.getHk1Teacher());
        redirectAttributes.addAttribute("teacherHk2", request.getHk2Teacher());
        return "redirect:/admin/score/create";
    }

    @GetMapping("/suggest/students")
    @ResponseBody
    public List<ScoreCreateService.StudentItem> suggestStudents(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "classId", required = false) String classId) {
        return scoreCreateService.suggestStudents(classId, q);
    }

    @GetMapping("/suggest/courses")
    @ResponseBody
    public List<ScoreCreateService.OptionItem> suggestCourses(
            @RequestParam(value = "q", required = false) String q) {
        return scoreCreateService.suggestCourses(q);
    }

    @GetMapping("/suggest/teachers")
    @ResponseBody
    public List<ScoreCreateService.TeacherItem> suggestTeachers(
            @RequestParam(value = "subjectId", required = false) String subjectId,
            @RequestParam(value = "classId", required = false) String classId,
            @RequestParam(value = "namHoc", required = false) String namHoc,
            @RequestParam(value = "hocKy", required = false) String hocKy,
            @RequestParam(value = "q", required = false) String q) {
        return scoreCreateService.suggestTeachers(subjectId, classId, namHoc, hocKy, q);
    }

    @GetMapping("/detail")
    public String scoreDetailPage(@RequestParam("studentId") String studentId,
                                  @RequestParam("subjectId") String subjectId,
                                  @RequestParam("namHoc") String namHoc,
                                  @RequestParam(value = "hocKy", required = false) String hocKy,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        String selectedSemester = resolveSemester(hocKy);
        try {
            ScoreGroupSummary summary = scoreManagementService.getScoreGroupSummary(studentId, subjectId, namHoc);
            ScoreCreateService.ScoreCreatePageData detailData =
                    loadDetailData(studentId, subjectId, namHoc, selectedSemester);
            model.addAttribute("activePage", "score");
            model.addAttribute("pageTitle", PAGE_TITLE_SCORE_DETAIL);
            model.addAttribute("summary", summary);
            model.addAttribute("detailData", detailData);
            model.addAttribute("selectedHocKy", selectedSemester);
            return "admin/score-detail";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveDetailErrorMessage(ex));
            return "redirect:/admin/score";
        }
    }

    @GetMapping("/detail/export/excel")
    public Object exportDetailExcel(@RequestParam("studentId") String studentId,
                                    @RequestParam("subjectId") String subjectId,
                                    @RequestParam("namHoc") String namHoc,
                                    @RequestParam(value = "hocKy", required = false) String hocKy,
                                    RedirectAttributes redirectAttributes) {
        String selectedSemester = resolveSemester(hocKy);
        try {
            ScoreGroupSummary summary = scoreManagementService.getScoreGroupSummary(studentId, subjectId, namHoc);
            ScoreCreateService.ScoreCreatePageData detailData =
                    loadDetailData(studentId, subjectId, namHoc, selectedSemester);
            validateExportEligibility(detailData, selectedSemester);
            byte[] content = scoreDetailExportService.exportExcel(summary, detailData, selectedSemester);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .headers(downloadHeaders(buildExportFileName(summary, selectedSemester, "xlsx")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveExportErrorMessage(ex));
            redirectAttributes.addAttribute("studentId", studentId);
            redirectAttributes.addAttribute("subjectId", subjectId);
            redirectAttributes.addAttribute("namHoc", namHoc);
            redirectAttributes.addAttribute("hocKy", selectedSemester);
            return "redirect:/admin/score/detail";
        }
    }

    @GetMapping("/detail/export/pdf")
    public Object exportDetailPdf(@RequestParam("studentId") String studentId,
                                  @RequestParam("subjectId") String subjectId,
                                  @RequestParam("namHoc") String namHoc,
                                  @RequestParam(value = "hocKy", required = false) String hocKy,
                                  RedirectAttributes redirectAttributes) {
        String selectedSemester = resolveSemester(hocKy);
        try {
            ScoreGroupSummary summary = scoreManagementService.getScoreGroupSummary(studentId, subjectId, namHoc);
            ScoreCreateService.ScoreCreatePageData detailData =
                    loadDetailData(studentId, subjectId, namHoc, selectedSemester);
            validateExportEligibility(detailData, selectedSemester);
            byte[] content = scoreDetailExportService.exportPdf(summary, detailData, selectedSemester);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .headers(downloadHeaders(buildExportFileName(summary, selectedSemester, "pdf")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveExportErrorMessage(ex));
            redirectAttributes.addAttribute("studentId", studentId);
            redirectAttributes.addAttribute("subjectId", subjectId);
            redirectAttributes.addAttribute("namHoc", namHoc);
            redirectAttributes.addAttribute("hocKy", selectedSemester);
            return "redirect:/admin/score/detail";
        }
    }

    @GetMapping("/edit")
    public String scoreEditPage(@RequestParam(value = "studentId", required = false) String studentId,
                                @RequestParam(value = "subjectId", required = false) String subjectId,
                                @RequestParam(value = "mon", required = false) String mon,
                                @RequestParam(value = "namHoc", required = false) String namHoc,
                                @RequestParam(value = "hocKy", required = false) String hocKy,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String resolvedStudentId = safeTrim(studentId);
        String resolvedSubjectId = firstNonBlank(subjectId, mon);
        String resolvedNamHoc = safeTrim(namHoc);

        if (resolvedStudentId == null || resolvedSubjectId == null || resolvedNamHoc == null) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Thiếu thông tin để mở trang chỉnh sửa điểm.");
            return "redirect:/admin/score";
        }

        try {
            ScoreGroupSummary summary = scoreManagementService.getScoreGroupSummary(
                    resolvedStudentId,
                    resolvedSubjectId,
                    resolvedNamHoc
            );
            ScoreCreateService.ScoreCreateFilter filter = new ScoreCreateService.ScoreCreateFilter();
            filter.setStudentId(resolvedStudentId);
            filter.setMon(resolvedSubjectId);
            filter.setNamHoc(resolvedNamHoc);
            filter.setHocKy(resolveSemester(hocKy));
            filter.setApplyFilter("1");

            ScoreCreateService.ScoreCreatePageData pageData = scoreCreateService.getCreatePageData(filter);
            model.addAttribute("activePage", "score");
            model.addAttribute("pageTitle", PAGE_TITLE_SCORE_EDIT);
            model.addAttribute("summary", summary);
            model.addAttribute("createData", pageData);
            model.addAttribute("filter", pageData.getFilter());
            model.addAttribute("formMode", "edit");
            return "admin/score-create";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/score";
        }
    }

    @PostMapping("/edit")
    public String scoreEditSubmit(@ModelAttribute ScoreCreateService.ScoreSaveRequest request,
                                  RedirectAttributes redirectAttributes) {
        // Edit mode keeps score + conduct updates, but class/course/search filters stay DB-derived.
        request.setKhoi(null);
        request.setKhoa(null);
        request.setLop(null);
        request.setQ(null);

        String targetSemester = resolveSemester(request.getHocKy());
        String studentId = request.getStudentId();
        String subjectId = request.getMon();
        String namHoc = request.getNamHoc();
        try {
            scoreCreateService.save(request);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_UPDATE_SUCCESS);
            return "redirect:/admin/score";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", resolveEditErrorMessage(ex));
        }

        return "redirect:/admin/score/edit?studentId=" + studentId
                + "&subjectId=" + subjectId
                + "&namHoc=" + namHoc
                + "&hocKy=" + targetSemester;
    }

    @PostMapping("/delete")
    public String deleteScoreGroup(@RequestParam("studentId") String studentId,
                                   @RequestParam("subjectId") String subjectId,
                                   @RequestParam("namHoc") String namHoc,
                                   RedirectAttributes redirectAttributes) {
        try {
            scoreManagementService.deleteScoreGroup(studentId, subjectId, namHoc);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_DELETE_SUCCESS);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/score";
    }

    private ScoreCreateService.ScoreCreatePageData loadDetailData(String studentId,
                                                                  String subjectId,
                                                                  String namHoc,
                                                                  String hocKy) {
        ScoreCreateService.ScoreCreateFilter filter = new ScoreCreateService.ScoreCreateFilter();
        filter.setStudentId(studentId);
        filter.setMon(subjectId);
        filter.setNamHoc(namHoc);
        filter.setHocKy(hocKy);
        filter.setApplyFilter("1");
        ScoreCreateService.ScoreCreatePageData pageData = scoreCreateService.getCreatePageData(filter);
        if (pageData == null || !pageData.isReadyForInput()) {
            throw new RuntimeException("Không tìm thấy dữ liệu chi tiết điểm để hiển thị.");
        }
        return pageData;
    }

    private HttpHeaders downloadHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );
        return headers;
    }

    private String buildExportFileName(ScoreGroupSummary summary, String hocKy, String extension) {
        String student = safeTrim(summary == null ? null : summary.getStudentId());
        String subject = safeTrim(summary == null ? null : summary.getSubjectId());
        String namHoc = safeTrim(summary == null ? null : summary.getNamHoc());
        return "chi-tiet-diem-"
                + (student == null ? "hs" : student)
                + "-"
                + (subject == null ? "mon" : subject)
                + "-"
                + (namHoc == null ? "nam-hoc" : namHoc)
                + "-"
                + semesterFileSuffix(hocKy)
                + "."
                + extension;
    }

    private String buildListExportFileName(String extension) {
        return "danh-sach-diem-" + EXPORT_FILE_DATE.format(LocalDate.now()) + "." + extension;
    }

    private void applySearchRedirectAttributes(RedirectAttributes redirectAttributes, ScoreSearch search) {
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
        if (safeTrim(search.getMon()) != null) {
            redirectAttributes.addAttribute("mon", search.getMon());
        }
        if (safeTrim(search.getHocKy()) != null) {
            redirectAttributes.addAttribute("hocKy", search.getHocKy());
        }
        if (safeTrim(search.getKhoa()) != null) {
            redirectAttributes.addAttribute("khoa", search.getKhoa());
        }
    }

    private String semesterFileSuffix(String hocKy) {
        if (SEMESTER_1.equals(hocKy)) {
            return "hk1";
        }
        if (SEMESTER_2.equals(hocKy)) {
            return "hk2";
        }
        return "ca-nam";
    }

    private void validateExportEligibility(ScoreCreateService.ScoreCreatePageData detailData, String selectedSemester) {
        boolean hasHk1 = hasSemesterAverage(detailData == null ? null : detailData.getHk1Input());
        boolean hasHk2 = hasSemesterAverage(detailData == null ? null : detailData.getHk2Input());

        if (SEMESTER_1.equals(selectedSemester) && !hasHk1) {
            throw new RuntimeException("Không thể xuất file học kỳ I vì học sinh chưa có điểm học kỳ I.");
        }
        if (SEMESTER_2.equals(selectedSemester) && !hasHk2) {
            throw new RuntimeException("Không thể xuất file học kỳ II vì học sinh chưa có điểm học kỳ II.");
        }
        if (SEMESTER_ALL.equals(selectedSemester)) {
            if (!hasHk1 && !hasHk2) {
                throw new RuntimeException("Không thể xuất file cả năm vì học sinh chưa có điểm học kỳ I và học kỳ II.");
            }
            if (!hasHk1) {
                throw new RuntimeException("Không thể xuất file cả năm vì học sinh chưa có điểm học kỳ I.");
            }
            if (!hasHk2) {
                throw new RuntimeException("Không thể xuất file cả năm vì học sinh chưa có điểm học kỳ II.");
            }
        }
    }

    private boolean hasSemesterAverage(ScoreCreateService.SemesterInput semesterInput) {
        return semesterInput != null && semesterInput.getAverage() != null;
    }

    private String resolveSemester(String hocKy) {
        if (hocKy == null) {
            return SEMESTER_ALL;
        }
        String trimmed = hocKy.trim();
        if (SEMESTER_1.equals(trimmed) || SEMESTER_2.equals(trimmed) || SEMESTER_ALL.equals(trimmed)) {
            return trimmed;
        }
        return SEMESTER_ALL;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String first, String second) {
        String firstTrimmed = safeTrim(first);
        if (firstTrimmed != null) {
            return firstTrimmed;
        }
        return safeTrim(second);
    }

    private String resolveEditErrorMessage(RuntimeException ex) {
        String message = safeTrim(ex == null ? null : ex.getMessage());
        if (message != null) {
            String normalized = message.toLowerCase();
            if ((normalized.contains("constraint_1") || normalized.contains("hoc_ky"))
                    && normalized.contains("conduct")) {
                return "Không thể cập nhật hạnh kiểm cả năm vì CSDL đang giới hạn học kỳ của conducts (chỉ 1 hoặc 2). "
                        + "Vui lòng chạy script db/manual/2026-03-20-conduct-allow-year-semester.sql rồi lưu lại.";
            }
            return message;
        }
        return "Không thể lưu chỉnh sửa điểm. Vui lòng kiểm tra lại dữ liệu và thử lại.";
    }

    private String resolveDetailErrorMessage(RuntimeException ex) {
        String message = safeTrim(ex == null ? null : ex.getMessage());
        if (message != null) {
            return message;
        }
        return "Không thể tải trang chi tiết điểm. Vui lòng thử lại.";
    }

    private String resolveExportErrorMessage(RuntimeException ex) {
        String message = safeTrim(ex == null ? null : ex.getMessage());
        if (message != null) {
            return message;
        }
        return "Không thể xuất file điểm. Vui lòng kiểm tra dữ liệu và thử lại.";
    }
}

