package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.form.ClassCreateForm;
import com.quanly.webdiem.model.form.CourseCreateForm;
import com.quanly.webdiem.model.service.admin.ClassManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/class")
public class ClassEditController {

    private final ClassManagementService classManagementService;
    private final ClassPageModelHelper pageModelHelper;

    public ClassEditController(ClassManagementService classManagementService,
                               ClassPageModelHelper pageModelHelper) {
        this.classManagementService = classManagementService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/{classId}/edit")
    public String editClassPage(@PathVariable("classId") String classId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("classForm")) {
                model.addAttribute("classForm", classManagementService.getClassFormForEdit(classId));
            }
            pageModelHelper.applyEditPage(model, classId);
            return "admin/class-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_CLASS_NOT_FOUND);
            return "redirect:/admin/class";
        }
    }

    @PostMapping("/{classId}/edit")
    public String editClass(@PathVariable("classId") String classId,
                            @ModelAttribute("classForm") ClassCreateForm classForm,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            classManagementService.updateClass(classId, classForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_UPDATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            pageModelHelper.applyEditPage(model, classId);
            return "admin/class-edit";
        }
    }

    @PostMapping("/{classId}/delete")
    public String deleteClass(@PathVariable("classId") String classId,
                              RedirectAttributes redirectAttributes) {
        try {
            classManagementService.deleteClass(classId);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_DELETE_SUCCESS);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/class";
    }

    @GetMapping("/course/{courseId}/edit")
    public String editCoursePage(@PathVariable("courseId") String courseId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("courseForm")) {
                model.addAttribute("courseForm", classManagementService.getCourseFormForEdit(courseId));
            }
            pageModelHelper.applyCourseEditPage(model, courseId);
            return "admin/class-course-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_COURSE_NOT_FOUND);
            return "redirect:/admin/class";
        }
    }

    @PostMapping("/course/{courseId}/edit")
    public String editCourse(@PathVariable("courseId") String courseId,
                             @ModelAttribute("courseForm") CourseCreateForm courseForm,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            classManagementService.updateCourse(courseId, courseForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_COURSE_UPDATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            if (!classManagementService.courseExists(courseId)) {
                redirectAttributes.addFlashAttribute("flashType", "error");
                redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_COURSE_NOT_FOUND);
                return "redirect:/admin/class";
            }
            model.addAttribute("error", ex.getMessage());
            pageModelHelper.applyCourseEditPage(model, courseId);
            return "admin/class-course-edit";
        }
    }

    @PostMapping("/course/{courseId}/delete")
    public String deleteCourse(@PathVariable("courseId") String courseId,
                               RedirectAttributes redirectAttributes) {
        try {
            classManagementService.deleteCourse(courseId);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ClassPageModelHelper.FLASH_COURSE_DELETE_SUCCESS);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/class";
    }
}
