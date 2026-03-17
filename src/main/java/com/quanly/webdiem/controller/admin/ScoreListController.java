package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.ScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScoreEntryUpdate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
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
            stats = scoreManagementService.getStats();
            grades = scoreManagementService.getGrades();
            classes = scoreManagementService.getClasses();
            subjects = scoreManagementService.getSubjects();
            courses = scoreManagementService.getCourses();
        } catch (Exception ex) {
            LOGGER.error("Loi tai trang danh sach diem so", ex);
            pageResult = new ScorePageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new ScoreStats(0, 0, 0);
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
            LOGGER.error("Loi tai trang them diem so", ex);
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
    public String scoreEditPage(@RequestParam("studentId") String studentId,
                                @RequestParam("subjectId") String subjectId,
                                @RequestParam("namHoc") String namHoc,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            ScoreGroupSummary summary = scoreManagementService.getScoreGroupSummary(studentId, subjectId, namHoc);
            model.addAttribute("activePage", "score");
            model.addAttribute("pageTitle", PAGE_TITLE_SCORE_EDIT);
            model.addAttribute("summary", summary);
            model.addAttribute("entries", scoreManagementService.getScoreEntries(studentId, subjectId, namHoc));
            return "admin/score-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/score";
        }
    }

    @PostMapping("/edit")
    public String scoreEditSubmit(@RequestParam("studentId") String studentId,
                                  @RequestParam("subjectId") String subjectId,
                                  @RequestParam("namHoc") String namHoc,
                                  @RequestParam("scoreId") List<Integer> scoreIds,
                                  @RequestParam("scoreValue") List<String> scoreValues,
                                  @RequestParam(value = "scoreNote", required = false) List<String> scoreNotes,
                                  RedirectAttributes redirectAttributes) {
        try {
            List<ScoreEntryUpdate> updates = new ArrayList<>();
            for (int index = 0; index < scoreIds.size(); index++) {
                Integer scoreId = scoreIds.get(index);
                String scoreValue = index < scoreValues.size() ? scoreValues.get(index) : "";
                String scoreNote = (scoreNotes != null && index < scoreNotes.size()) ? scoreNotes.get(index) : "";
                updates.add(new ScoreEntryUpdate(scoreId, scoreValue, scoreNote));
            }

            scoreManagementService.updateScoreEntries(studentId, subjectId, namHoc, updates);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_UPDATE_SUCCESS);
            return "redirect:/admin/score/detail?studentId=" + studentId + "&subjectId=" + subjectId + "&namHoc=" + namHoc;
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/score/edit?studentId=" + studentId + "&subjectId=" + subjectId + "&namHoc=" + namHoc;
        }
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
}
