package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.ClassCreateForm;
import com.quanly.webdiem.model.entity.ClassSearch;
import com.quanly.webdiem.model.service.admin.ClassManagementService;
import com.quanly.webdiem.model.service.admin.ClassManagementService.ClassPageResult;
import com.quanly.webdiem.model.service.admin.ClassManagementService.ClassStats;
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

import java.util.List;

@Controller
@RequestMapping("/admin/class")
public class ClassListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassListController.class);
    private static final String PAGE_TITLE_CLASS = "Qu\u1ea3n l\u00fd l\u1edbp h\u1ecdc";
    private static final String PAGE_TITLE_CLASS_CREATE = "Th\u00eam l\u1edbp h\u1ecdc m\u1edbi";
    private static final String PAGE_ERROR_MESSAGE = "Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch l\u1edbp h\u1ecdc.";
    private static final String FLASH_CREATE_SUCCESS = "T\u1ea1o l\u1edbp h\u1ecdc th\u00e0nh c\u00f4ng.";

    private final ClassManagementService classManagementService;

    public ClassListController(ClassManagementService classManagementService) {
        this.classManagementService = classManagementService;
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
            LOGGER.error("Loi tai trang danh sach lop hoc", ex);
            pageResult = new ClassPageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new ClassStats(0, 0, 0);
            grades = List.of();
            courses = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", PAGE_ERROR_MESSAGE);
        }

        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", PAGE_TITLE_CLASS);
        model.addAttribute("classes", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("stats", stats);
        model.addAttribute("grades", grades);
        model.addAttribute("courses", courses);
        return "admin/class";
    }

    @GetMapping("/create")
    public String createClassPage(Model model) {
        if (!model.containsAttribute("classForm")) {
            model.addAttribute("classForm", new ClassCreateForm());
        }
        applyCreatePageModel(model);
        return "admin/class-create";
    }

    @PostMapping("/create")
    public String createClass(@ModelAttribute("classForm") ClassCreateForm classForm,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            classManagementService.createClass(classForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_CREATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            applyCreatePageModel(model);
            return "admin/class-create";
        }
    }

    @GetMapping("/suggest/homeroom-teachers")
    @ResponseBody
    public List<ClassManagementService.SuggestionItem> suggestHomeroomTeachers(
            @RequestParam(name = "q", required = false) String query
    ) {
        return classManagementService.suggestHomeroomTeachers(query);
    }

    private void applyCreatePageModel(Model model) {
        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", PAGE_TITLE_CLASS_CREATE);
        model.addAttribute("courseOptions", classManagementService.getCoursesForCreate());
        model.addAttribute("gradeOptions", List.of(10, 11, 12));
    }
}
