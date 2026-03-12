package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.TeacherCreateForm;
import com.quanly.webdiem.model.service.admin.TeacherCreateService;
import com.quanly.webdiem.model.service.admin.TeacherCreateValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/teacher")
public class TeacherCreateController {

    private static final String PAGE_TITLE = "Th\u00eam Gi\u00e1o Vi\u00ean M\u1edbi";
    private static final String FLASH_SUCCESS = "Th\u00eam gi\u00e1o vi\u00ean th\u00e0nh c\u00f4ng.";
    private static final String FLASH_ERROR = "Kh\u00f4ng th\u1ec3 th\u00eam gi\u00e1o vi\u00ean. Vui l\u00f2ng ki\u1ec3m tra l\u1ea1i d\u1eef li\u1ec7u.";

    private final TeacherCreateService teacherCreateService;
    private final TeacherCreateValidator teacherCreateValidator;

    public TeacherCreateController(TeacherCreateService teacherCreateService,
                                   TeacherCreateValidator teacherCreateValidator) {
        this.teacherCreateService = teacherCreateService;
        this.teacherCreateValidator = teacherCreateValidator;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("teacherForm")) {
            TeacherCreateForm teacherForm = new TeacherCreateForm();
            teacherCreateService.applyDefaultValues(teacherForm);
            model.addAttribute("teacherForm", teacherForm);
        }

        applyFormPage(model);
        return "admin/teacher-create";
    }

    @PostMapping("/create")
    public String createTeacher(@ModelAttribute("teacherForm") TeacherCreateForm teacherForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        teacherCreateService.applyDefaultValues(teacherForm);
        teacherCreateValidator.validateForCreate(teacherForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("fieldErrors", toFieldErrorMap(bindingResult));
            applyFormPage(model);
            return "admin/teacher-create";
        }

        try {
            teacherCreateService.createTeacher(teacherForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_SUCCESS);
            return "redirect:/admin/teacher";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage() == null ? FLASH_ERROR : ex.getMessage());
            applyFormPage(model);
            return "admin/teacher-create";
        }
    }

    private void applyFormPage(Model model) {
        model.addAttribute("activePage", "teacher");
        model.addAttribute("pageTitle", PAGE_TITLE);
        model.addAttribute("subjectOptions", teacherCreateService.getSubjectsForForm());
        model.addAttribute("roleOptions", teacherCreateService.getTeacherRoleTypes());
        model.addAttribute("genderOptions", teacherCreateService.getGenderOptions());
        model.addAttribute("degreeOptions", teacherCreateService.getDegreeOptions());
        model.addAttribute("statusOptions", teacherCreateService.getStatusOptions());
        model.addAttribute("suggestedTeacherId", teacherCreateService.suggestNextTeacherId());
        model.addAttribute("today", LocalDate.now());
    }

    private Map<String, String> toFieldErrorMap(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return fieldErrors;
    }
}

