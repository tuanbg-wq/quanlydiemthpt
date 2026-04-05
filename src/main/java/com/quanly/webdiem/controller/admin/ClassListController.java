package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.ClassSearch;
import com.quanly.webdiem.model.service.admin.ClassManagementService;
import com.quanly.webdiem.model.service.admin.ClassManagementService.ClassPageResult;
import com.quanly.webdiem.model.service.admin.ClassManagementService.ClassStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/class")
public class ClassListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassListController.class);

    private final ClassManagementService classManagementService;
    private final ClassPageModelHelper pageModelHelper;

    public ClassListController(ClassManagementService classManagementService,
                               ClassPageModelHelper pageModelHelper) {
        this.classManagementService = classManagementService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping
    public String classPage(@ModelAttribute("search") ClassSearch search,
                            Model model) {
        ClassPageResult pageResult;
        ClassStats stats;
        List<String> grades;
        List<ClassManagementService.CourseOption> courses;

        try {
            pageResult = classManagementService.search(search);
            stats = classManagementService.getStats();
            grades = classManagementService.getGrades();
            courses = classManagementService.getCourses();
        } catch (Exception ex) {
            LOGGER.error("Lỗi tải trang danh sách lớp học", ex);
            pageResult = new ClassPageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new ClassStats(0, 0, 0);
            grades = List.of();
            courses = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ClassPageModelHelper.PAGE_ERROR_MESSAGE);
        }

        pageModelHelper.applyListPage(model, pageResult, stats, grades, courses);
        return "admin/class";
    }
}
