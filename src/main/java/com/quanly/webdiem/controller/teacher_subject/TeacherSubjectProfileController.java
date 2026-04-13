package com.quanly.webdiem.controller.teacher_subject;

import com.quanly.webdiem.model.form.TeacherProfileUpdateForm;
import com.quanly.webdiem.model.service.teacher.TeacherProfileService.TeacherProfilePageData;
import com.quanly.webdiem.model.service.teacher_subject.TeacherSubjectProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/teacher-subject/profile")
@PreAuthorize("hasAnyAuthority('ROLE_GVBM','ROLE_Giao_vien','ROLE_Admin')")
public class TeacherSubjectProfileController {

    private static final String PAGE_TITLE = "Thông tin cá nhân";

    private final TeacherSubjectPageModelHelper pageModelHelper;
    private final TeacherSubjectProfileService teacherSubjectProfileService;

    public TeacherSubjectProfileController(TeacherSubjectPageModelHelper pageModelHelper,
                                           TeacherSubjectProfileService teacherSubjectProfileService) {
        this.pageModelHelper = pageModelHelper;
        this.teacherSubjectProfileService = teacherSubjectProfileService;
    }

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherProfilePageData pageData = teacherSubjectProfileService.getProfilePageData(username);
        applyProfilePage(model, pageData);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", pageData.getForm());
        }
        return "teacher-subject/profile";
    }

    @PostMapping
    public String updateProfile(@ModelAttribute("form") TeacherProfileUpdateForm form,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherProfilePageData pageData = teacherSubjectProfileService.getProfilePageData(username);
        Map<String, String> fieldErrors = teacherSubjectProfileService.validateForUpdate(username, form);

        if (!fieldErrors.isEmpty()) {
            applyProfilePage(model, pageData);
            model.addAttribute("form", form);
            model.addAttribute("fieldErrors", fieldErrors);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Vui lòng kiểm tra lại thông tin cập nhật.");
            return "teacher-subject/profile";
        }

        try {
            teacherSubjectProfileService.updateProfile(username, form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Cập nhật thông tin cá nhân thành công.");
            return "redirect:/teacher-subject/profile";
        } catch (RuntimeException ex) {
            applyProfilePage(model, pageData);
            model.addAttribute("form", form);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ex.getMessage());
            return "teacher-subject/profile";
        }
    }

    private void applyProfilePage(Model model, TeacherProfilePageData pageData) {
        pageModelHelper.applyBasePage(model, "profile", PAGE_TITLE);
        model.addAttribute("profileData", pageData);
        model.addAttribute("teacherInfo", pageData.getTeacherInfo());
    }
}

