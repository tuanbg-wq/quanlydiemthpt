package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.TeacherAccountCreateForm;
import com.quanly.webdiem.model.service.admin.AccountManagementService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/account")
public class AccountManagementController {

    private static final String PAGE_TITLE = "Quan li tai khoan";

    private final AccountManagementService accountManagementService;

    public AccountManagementController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @GetMapping
    public String accountPage(Model model) {
        if (!model.containsAttribute("teacherAccountForm")) {
            model.addAttribute("teacherAccountForm", new TeacherAccountCreateForm());
        }

        applyPageModel(model);
        return "admin/account";
    }

    @PostMapping("/teacher/create")
    public String createTeacherAccount(@Valid @ModelAttribute("teacherAccountForm") TeacherAccountCreateForm form,
                                       BindingResult bindingResult,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            applyPageModel(model);
            return "admin/account";
        }

        try {
            accountManagementService.createTeacherAccount(form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Tao tai khoan giao vien thanh cong.");
            return "redirect:/admin/account";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            applyPageModel(model);
            return "admin/account";
        }
    }

    private void applyPageModel(Model model) {
        model.addAttribute("activePage", "account");
        model.addAttribute("pageTitle", PAGE_TITLE);
        model.addAttribute("accounts", accountManagementService.getAllAccounts());
    }
}
