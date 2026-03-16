package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.ScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScorePageResult;
import com.quanly.webdiem.model.service.admin.ScoreManagementService.ScoreStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/score")
public class ScoreListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreListController.class);
    private static final String PAGE_TITLE_SCORE = "Qu\u1ea3n l\u00fd \u0111i\u1ec3m s\u1ed1";
    private static final String PAGE_TITLE_SCORE_CREATE = "Th\u00eam \u0111i\u1ec3m s\u1ed1";
    private static final String PAGE_ERROR_MESSAGE = "Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch \u0111i\u1ec3m s\u1ed1.";

    private final ScoreManagementService scoreManagementService;

    public ScoreListController(ScoreManagementService scoreManagementService) {
        this.scoreManagementService = scoreManagementService;
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
    public String createScorePage(Model model) {
        model.addAttribute("activePage", "score");
        model.addAttribute("pageTitle", PAGE_TITLE_SCORE_CREATE);
        return "admin/score-create";
    }
}
