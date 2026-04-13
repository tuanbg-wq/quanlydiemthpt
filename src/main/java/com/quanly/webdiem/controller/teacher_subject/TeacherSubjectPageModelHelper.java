package com.quanly.webdiem.controller.teacher_subject;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class TeacherSubjectPageModelHelper {

    public void applyBasePage(Model model,
                              String activePage,
                              String pageTitle) {
        model.addAttribute("activePage", activePage);
        model.addAttribute("pageTitle", pageTitle);
    }

    public String resolveUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }
        return authentication.getName().trim();
    }
}

