package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.TeacherCreateForm;
import com.quanly.webdiem.model.service.admin.TeacherCreateService;
import com.quanly.webdiem.model.service.admin.TeacherCreateValidator;
import com.quanly.webdiem.model.service.admin.TeacherEditService;
import com.quanly.webdiem.model.service.admin.TeacherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/admin/teacher")
public class TeacherEditController {

    private static final String PAGE_TITLE = "Ch\u1ec9nh S\u1eeda Gi\u00e1o Vi\u00ean";
    private static final String FLASH_UPDATE_SUCCESS = "C\u1eadp nh\u1eadt gi\u00e1o vi\u00ean th\u00e0nh c\u00f4ng.";
    private static final String FLASH_DELETE_SUCCESS = "X\u00f3a gi\u00e1o vi\u00ean th\u00e0nh c\u00f4ng.";
    private static final String FLASH_DELETE_ERROR = "Kh\u00f4ng th\u1ec3 x\u00f3a gi\u00e1o vi\u00ean.";
    private static final String FLASH_ERROR = "Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt gi\u00e1o vi\u00ean. Vui l\u00f2ng ki\u1ec3m tra l\u1ea1i d\u1eef li\u1ec7u.";

    private final TeacherCreateService teacherCreateService;
    private final TeacherCreateValidator teacherCreateValidator;
    private final TeacherEditService teacherEditService;
    private final TeacherService teacherService;

    public TeacherEditController(TeacherCreateService teacherCreateService,
                                 TeacherCreateValidator teacherCreateValidator,
                                 TeacherEditService teacherEditService,
                                 TeacherService teacherService) {
        this.teacherCreateService = teacherCreateService;
        this.teacherCreateValidator = teacherCreateValidator;
        this.teacherEditService = teacherEditService;
        this.teacherService = teacherService;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") String id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        String normalizedId = normalizeTeacherId(id);

        try {
            if (!model.containsAttribute("teacherForm")) {
                model.addAttribute("teacherForm", teacherEditService.getEditForm(normalizedId));
            }
            model.addAttribute("teacherId", normalizedId);
            model.addAttribute("currentAvatar", teacherEditService.getCurrentAvatarPath(normalizedId));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/teacher";
        }

        applyFormPage(model);
        return "admin/teacher-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateTeacher(@PathVariable("id") String id,
                                @ModelAttribute("teacherForm") TeacherCreateForm teacherForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String normalizedId = normalizeTeacherId(id);
        teacherCreateService.applyDefaultValues(teacherForm);
        teacherForm.setIdGiaoVien(normalizedId);

        teacherCreateValidator.validateForUpdate(normalizedId, teacherForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("fieldErrors", toFieldErrorMap(bindingResult));
            model.addAttribute("teacherId", normalizedId);
            model.addAttribute("currentAvatar", safeCurrentAvatar(normalizedId));
            applyFormPage(model);
            return "admin/teacher-edit";
        }

        try {
            teacherEditService.updateTeacher(normalizedId, teacherForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_UPDATE_SUCCESS);
            return "redirect:/admin/teacher";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage() == null ? FLASH_ERROR : ex.getMessage());
            model.addAttribute("teacherId", normalizedId);
            model.addAttribute("currentAvatar", safeCurrentAvatar(normalizedId));
            applyFormPage(model);
            return "admin/teacher-edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTeacher(@PathVariable("id") String id,
                                RedirectAttributes redirectAttributes) {
        String normalizedId = normalizeTeacherId(id);
        try {
            teacherService.deleteTeacher(normalizedId);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_DELETE_SUCCESS);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute(
                    "flashMessage",
                    ex.getMessage() == null ? FLASH_DELETE_ERROR : ex.getMessage()
            );
        }
        return "redirect:/admin/teacher";
    }

    private void applyFormPage(Model model) {
        model.addAttribute("activePage", "teacher");
        model.addAttribute("pageTitle", PAGE_TITLE);
        model.addAttribute("subjectOptions", teacherCreateService.getSubjectsForForm());
        model.addAttribute("roleOptions", teacherCreateService.getTeacherRoleTypes());
        model.addAttribute("genderOptions", teacherCreateService.getGenderOptions());
        model.addAttribute("degreeOptions", teacherCreateService.getDegreeOptions());
        model.addAttribute("statusOptions", teacherCreateService.getStatusOptions());
        model.addAttribute("today", LocalDate.now());
    }

    private Map<String, String> toFieldErrorMap(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return fieldErrors;
    }

    private String normalizeTeacherId(String teacherId) {
        if (teacherId == null || teacherId.trim().isEmpty()) {
            return teacherId;
        }
        return teacherId.trim().toUpperCase(Locale.ROOT);
    }

    private String safeCurrentAvatar(String teacherId) {
        try {
            return teacherEditService.getCurrentAvatarPath(teacherId);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
