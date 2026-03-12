package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.TeacherSearch;
import com.quanly.webdiem.model.service.admin.TeacherService;
import com.quanly.webdiem.model.service.admin.TeacherService.TeacherPageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/teacher")
public class TeacherListController {

    private static final String PAGE_TITLE_TEACHER = "Qu\u1ea3n l\u00fd gi\u00e1o vi\u00ean";
    private static final String PAGE_ERROR_MESSAGE = "Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch gi\u00e1o vi\u00ean.";
    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherListController.class);
    private final TeacherService teacherService;

    public TeacherListController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String teacherPage(@ModelAttribute("search") TeacherSearch search,
                              Model model) {
        TeacherPageResult pageResult;
        List<String> subjects;
        List<String> grades;
        List<String> statuses;

        try {
            pageResult = teacherService.search(search);
            subjects = teacherService.getSubjects();
            grades = teacherService.getGrades();
            statuses = teacherService.getStatuses();
        } catch (Exception ex) {
            LOGGER.error("Loi tai trang danh sach giao vien", ex);
            pageResult = new TeacherPageResult(List.of(), 1, 1, 0, 0, 0);
            subjects = List.of();
            grades = List.of();
            statuses = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", PAGE_ERROR_MESSAGE);
        }

        model.addAttribute("activePage", "teacher");
        model.addAttribute("pageTitle", PAGE_TITLE_TEACHER);
        model.addAttribute("teachers", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("subjects", subjects);
        model.addAttribute("grades", grades);
        model.addAttribute("statuses", statuses);
        return "admin/teacher";
    }
}
