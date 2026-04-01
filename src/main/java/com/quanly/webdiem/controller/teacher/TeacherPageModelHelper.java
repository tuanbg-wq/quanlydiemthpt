package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class TeacherPageModelHelper {

    public void applyBasePage(Model model,
                              String activePage,
                              String pageTitle,
                              TeacherHomeroomScope scope) {
        model.addAttribute("activePage", activePage);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("scope", scope == null ? TeacherHomeroomScope.empty() : scope);
    }

    public void applyStudentPage(Model model,
                                 String pageTitle,
                                 TeacherHomeroomScope scope) {
        applyBasePage(model, "student", pageTitle, scope);
    }

    public String resolveUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }
        return authentication.getName().trim();
    }

    public String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = safeTrim(request.getHeader("X-Forwarded-For"));
        if (forwarded != null) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex >= 0 ? safeTrim(forwarded.substring(0, commaIndex)) : forwarded;
        }
        return safeTrim(request.getRemoteAddr());
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
