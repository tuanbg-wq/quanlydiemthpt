package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.ScoreCreateService;
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
public class ScoreCreateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreCreateController.class);

    private final ScoreCreateService scoreCreateService;
    private final ScorePageSupport scorePageSupport;

    public ScoreCreateController(ScoreCreateService scoreCreateService,
                                 ScorePageSupport scorePageSupport) {
        this.scoreCreateService = scoreCreateService;
        this.scorePageSupport = scorePageSupport;
    }

    @GetMapping("/create")
    public String createScorePage(@ModelAttribute("filter") ScoreCreateService.ScoreCreateFilter filter,
                                  Model model) {
        try {
            ScoreCreateService.ScoreCreatePageData pageData = scoreCreateService.getCreatePageData(filter);
            scorePageSupport.applyCreatePage(model, pageData);
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải trang thêm điểm số", ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ScorePageSupport.PAGE_CREATE_ERROR_MESSAGE);
            try {
                ScoreCreateService.ScoreCreatePageData fallbackData =
                        scoreCreateService.getCreatePageData(new ScoreCreateService.ScoreCreateFilter());
                scorePageSupport.applyCreatePage(model, fallbackData);
            } catch (RuntimeException ignoreEx) {
                scorePageSupport.applyCreatePage(model, null);
            }
        }

        return "admin/score-create";
    }

    @PostMapping("/create")
    public String createScoreSubmit(@ModelAttribute ScoreCreateService.ScoreSaveRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            scoreCreateService.save(request);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ScorePageSupport.FLASH_CREATE_SUCCESS);
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
}
