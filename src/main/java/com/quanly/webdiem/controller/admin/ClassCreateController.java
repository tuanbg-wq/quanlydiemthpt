package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.form.ClassCreateForm;
import com.quanly.webdiem.model.form.CourseCreateForm;
import com.quanly.webdiem.model.service.admin.ClassManagementService;
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
@RequestMapping("/admin/class")
public class ClassCreateController {

    private final ClassManagementService classManagementService;
    private final ClassPageModelHelper pageModelHelper;

    public ClassCreateController(ClassManagementService classManagementService,
                                 ClassPageModelHelper pageModelHelper) {
        this.classManagementService = classManagementService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/create")
    public String createClassPage(Model model) {
        pageModelHelper.applyCreatePage(model);
        return "admin/class-create";
    }

    @PostMapping("/create")
    public String createClass(@ModelAttribute("classForm") ClassCreateForm classForm,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            classManagementService.createClass(classForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_CREATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            pageModelHelper.applyCreatePage(model);
            return "admin/class-create";
        }
    }

    @GetMapping("/course/create")
    public String createCoursePage(Model model) {
        pageModelHelper.applyCourseCreatePage(model);
        return "admin/class-course-create";
    }

    @PostMapping("/course/create")
    public String createCourse(@ModelAttribute("courseForm") CourseCreateForm courseForm,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            classManagementService.createCourse(courseForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_COURSE_CREATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            pageModelHelper.applyCourseCreatePage(model);
            return "admin/class-course-create";
        }
    }

    @GetMapping("/suggest/homeroom-teachers")
    @ResponseBody
    public List<ClassManagementService.SuggestionItem> suggestHomeroomTeachers(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "classId", required = false) String classId
    ) {
        return classManagementService.suggestHomeroomTeachers(query, classId);
    }

    @GetMapping("/suggest/class-codes")
    @ResponseBody
    public List<ClassManagementService.SuggestionItem> suggestClassCodes(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "courseId", required = false) String courseId,
            @RequestParam(name = "grade", required = false) String grade,
            @RequestParam(name = "excludeClassId", required = false) String excludeClassId
    ) {
        return classManagementService.suggestClassCodes(query, courseId, grade, excludeClassId);
    }
}
