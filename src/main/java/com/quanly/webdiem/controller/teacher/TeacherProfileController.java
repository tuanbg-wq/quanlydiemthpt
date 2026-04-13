package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.form.TeacherProfileUpdateForm;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherProfileService;
import com.quanly.webdiem.model.service.teacher.TeacherProfileService.TeacherProfilePageData;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
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
@RequestMapping("/teacher/profile")
@PreAuthorize("hasAnyAuthority('ROLE_Giao_vien','ROLE_GVCN','ROLE_Admin')")
public class TeacherProfileController {

    private static final String PAGE_TITLE = "Thông tin cá nhân";

    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;
    private final TeacherProfileService teacherProfileService;

    public TeacherProfileController(TeacherStudentScopeService scopeService,
                                    TeacherPageModelHelper pageModelHelper,
                                    TeacherProfileService teacherProfileService) {
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
        this.teacherProfileService = teacherProfileService;
    }

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        TeacherProfilePageData pageData = teacherProfileService.getProfilePageData(username);
        applyProfilePage(model, scope, pageData);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", pageData.getForm());
        }
        return "teacher/profile";
    }

    @PostMapping
    public String updateProfile(@ModelAttribute("form") TeacherProfileUpdateForm form,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        TeacherProfilePageData pageData = teacherProfileService.getProfilePageData(username);
        Map<String, String> fieldErrors = teacherProfileService.validateForUpdate(username, form);

        if (!fieldErrors.isEmpty()) {
            applyProfilePage(model, scope, pageData);
            model.addAttribute("form", form);
            model.addAttribute("fieldErrors", fieldErrors);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Vui lòng kiểm tra lại thông tin cập nhật.");
            return "teacher/profile";
        }

        try {
            teacherProfileService.updateProfile(username, form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Cập nhật thông tin cá nhân thành công.");
            return "redirect:/teacher/profile";
        } catch (RuntimeException ex) {
            applyProfilePage(model, scope, pageData);
            model.addAttribute("form", form);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ex.getMessage());
            return "teacher/profile";
        }
    }

    private void applyProfilePage(Model model,
                                  TeacherHomeroomScope scope,
                                  TeacherProfilePageData pageData) {
        pageModelHelper.applyBasePage(model, "profile", PAGE_TITLE, scope);
        model.addAttribute("profileData", pageData);
        model.addAttribute("teacherInfo", pageData.getTeacherInfo());
    }
}
