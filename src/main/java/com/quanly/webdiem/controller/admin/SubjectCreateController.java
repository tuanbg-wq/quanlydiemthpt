package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.form.SubjectCreateForm;
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

    private static final String TITLE_CREATE = "Thêm Môn Học Mới";
    private static final String TITLE_EDIT = "Chỉnh Sửa Môn Học";

    private static final String FLASH_CREATE_SUCCESS = "Thêm môn học thành công.";
    private static final String FLASH_UPDATE_SUCCESS = "Cập nhật môn học thành công.";
    private static final String FLASH_DELETE_SUCCESS = "Xóa môn học thành công.";
    private static final String FLASH_DELETE_ERROR = "Không thể xóa môn học vì có dữ liệu liên quan.";

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
