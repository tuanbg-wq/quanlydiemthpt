package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.SubjectCreateForm;
import com.quanly.webdiem.model.service.admin.SubjectService;
import com.quanly.webdiem.model.service.admin.SubjectService.SuggestionItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/subject")
public class SubjectCreateController {

    private static final String TITLE_CREATE = "Th\u00eam M\u00f4n H\u1ecdc M\u1edbi";
    private static final String TITLE_EDIT = "Ch\u1ec9nh S\u1eeda M\u00f4n H\u1ecdc";

    private static final String FLASH_CREATE_SUCCESS = "Th\u00eam m\u00f4n h\u1ecdc th\u00e0nh c\u00f4ng.";
    private static final String FLASH_UPDATE_SUCCESS = "C\u1eadp nh\u1eadt m\u00f4n h\u1ecdc th\u00e0nh c\u00f4ng.";
    private static final String FLASH_DELETE_SUCCESS = "X\u00f3a m\u00f4n h\u1ecdc th\u00e0nh c\u00f4ng.";
    private static final String FLASH_DELETE_ERROR = "Kh\u00f4ng th\u1ec3 x\u00f3a m\u00f4n h\u1ecdc v\u00ec c\u00f3 d\u1eef li\u1ec7u li\u00ean quan.";

    private final SubjectService subjectService;

    public SubjectCreateController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("subjectForm")) {
            model.addAttribute("subjectForm", new SubjectCreateForm());
        }

        applyFormPage(model, TITLE_CREATE);
        return "admin/subject-create";
    }

    @PostMapping("/create")
    public String createSubject(@ModelAttribute("subjectForm") SubjectCreateForm form,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            subjectService.createSubject(form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_CREATE_SUCCESS);
            return "redirect:/admin/subject";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            applyFormPage(model, TITLE_CREATE);
            return "admin/subject-create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        model.addAttribute("subjectForm", subjectService.getEditForm(id));
        model.addAttribute("subjectId", id);
        applyFormPage(model, TITLE_EDIT);
        return "admin/subject-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateSubject(@PathVariable("id") String id,
                                @ModelAttribute("subjectForm") SubjectCreateForm form,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            subjectService.updateSubject(id, form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_UPDATE_SUCCESS);
            return "redirect:/admin/subject";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("subjectId", id);
            applyFormPage(model, TITLE_EDIT);
            return "admin/subject-edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteSubject(@PathVariable("id") String id,
                                RedirectAttributes redirectAttributes) {
        try {
            subjectService.deleteSubject(id);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_DELETE_SUCCESS);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_DELETE_ERROR);
        }
        return "redirect:/admin/subject";
    }

    @GetMapping("/suggest/courses")
    @ResponseBody
    public List<SuggestionItem> suggestCourses(@RequestParam(value = "q", required = false) String q) {
        return subjectService.suggestCourses(q);
    }

    @GetMapping("/suggest/school-years")
    @ResponseBody
    public List<SuggestionItem> suggestSchoolYears(@RequestParam(value = "q", required = false) String q) {
        return subjectService.suggestSchoolYears(q);
    }

    @GetMapping("/suggest/teachers")
    @ResponseBody
    public List<SuggestionItem> suggestTeachers(@RequestParam(value = "q", required = false) String q) {
        return subjectService.suggestTeachers(q);
    }

    private void applyFormPage(Model model, String pageTitle) {
        model.addAttribute("activePage", "subject");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("departments", subjectService.getDepartments());
    }
}
