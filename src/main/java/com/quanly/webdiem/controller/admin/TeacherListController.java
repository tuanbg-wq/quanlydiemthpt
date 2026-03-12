package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.TeacherSearch;
import com.quanly.webdiem.model.service.admin.TeacherService;
import com.quanly.webdiem.model.service.admin.TeacherService.TeacherPageResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/teacher")
public class TeacherListController {

    private static final String PAGE_TITLE_TEACHER = "Qu\u1ea3n l\u00fd gi\u00e1o vi\u00ean";
    private final TeacherService teacherService;

    public TeacherListController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String teacherPage(@ModelAttribute("search") TeacherSearch search,
                              Model model) {
        TeacherPageResult pageResult = teacherService.search(search);

        model.addAttribute("activePage", "teacher");
        model.addAttribute("pageTitle", PAGE_TITLE_TEACHER);
        model.addAttribute("teachers", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("subjects", teacherService.getSubjects());
        model.addAttribute("grades", teacherService.getGrades());
        model.addAttribute("statuses", teacherService.getStatuses());
        return "admin/teacher";
    }
}
