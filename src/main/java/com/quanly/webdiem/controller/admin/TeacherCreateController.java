package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.TeacherCreateForm;
import com.quanly.webdiem.model.service.admin.TeacherCreateService;
import com.quanly.webdiem.model.service.admin.TeacherCreateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/teacher")
public class TeacherCreateController {

    private static final String PAGE_TITLE = "Thêm Giáo Viên Mới";
    private static final String FLASH_SUCCESS = "Thêm giáo viên thành công.";
    private static final String FLASH_ERROR = "Không thể thêm giáo viên. Vui lòng kiểm tra lại dữ liệu.";
    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherCreateController.class);

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
        } catch (Exception ex) {
            LOGGER.error("Loi khi them giao vien", ex);
            model.addAttribute("error", ex.getMessage() == null ? FLASH_ERROR : ex.getMessage());
            applyFormPage(model);
            return "admin/teacher-create";
        }
    }

    @GetMapping("/suggest/subject-classes")
    @ResponseBody
    public List<TeacherCreateService.ClassSuggestionItem> suggestSubjectClasses(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "namHoc", required = false) String namHoc,
            @RequestParam(value = "subjectId", required = false) String subjectId) {
        return teacherCreateService.suggestSubjectClasses(q, namHoc, subjectId);
    }

    @GetMapping("/suggest/homeroom-classes")
    @ResponseBody
    public List<TeacherCreateService.ClassSuggestionItem> suggestHomeroomClasses(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "namHoc", required = false) String namHoc,
            @RequestParam(value = "mode", required = false, defaultValue = "create") String mode) {
        boolean includeAssigned = "edit".equalsIgnoreCase(mode);
        return teacherCreateService.suggestHomeroomClasses(q, namHoc, includeAssigned);
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

