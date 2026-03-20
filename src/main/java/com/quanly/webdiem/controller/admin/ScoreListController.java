package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.ScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScoreGroupSummary;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScorePageResult;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScoreStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    private final ScoreManagementService scoreManagementService;
    private final ScoreCreateService scoreCreateService;

    public ScoreListController(ScoreManagementService scoreManagementService,
                               ScoreCreateService scoreCreateService) {
        this.scoreManagementService = scoreManagementService;
        this.scoreCreateService = scoreCreateService;
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
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            ScoreGroupSummary summary = scoreManagementService.getScoreGroupSummary(studentId, subjectId, namHoc);
            model.addAttribute("activePage", "score");
            model.addAttribute("pageTitle", PAGE_TITLE_SCORE_DETAIL);
            model.addAttribute("summary", summary);
            model.addAttribute("entries", scoreManagementService.getScoreEntries(studentId, subjectId, namHoc));
            return "admin/score-detail";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/score";
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
}
