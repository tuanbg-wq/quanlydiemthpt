package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.AccountSearch;
import com.quanly.webdiem.model.entity.AccountUpsertForm;
import com.quanly.webdiem.model.service.admin.AccountManagementService;
import com.quanly.webdiem.model.service.admin.AccountManagementService.AccountPageResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/account")
public class AccountManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManagementController.class);
    private static final String PAGE_TITLE = "Quan ly tai khoan";

    private final AccountManagementService accountService;

    public AccountManagementController(AccountManagementService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String accountPage(@ModelAttribute("search") AccountSearch search, Model model) {
        AccountPageResult pageResult;
        AccountManagementService.AccountStats stats;
        try {
            pageResult = accountService.search(search);
            stats = accountService.getStats();
        } catch (Exception ex) {
            LOGGER.error("Loi tai trang quan ly tai khoan", ex);
            pageResult = new AccountPageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new AccountManagementService.AccountStats(0, 0, 0, 0);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Khong the tai danh sach tai khoan.");
        }

        model.addAttribute("activePage", "account");
        model.addAttribute("pageTitle", PAGE_TITLE);
        model.addAttribute("stats", stats);
        model.addAttribute("accounts", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("roleFilters", accountService.getRoleFilters());
        model.addAttribute("statusFilters", accountService.getStatusFilters());
        model.addAttribute("gradeFilters", accountService.getGradeFilters());
        return "admin/account";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("accountForm")) {
            model.addAttribute("accountForm", accountService.initCreateForm());
        }
        applyFormPageModel(model, "Tao tai khoan", true);
        return "admin/account-form";
    }

    @PostMapping("/create")
    public String createAccount(@Valid @ModelAttribute("accountForm") AccountUpsertForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            applyFormPageModel(model, "Tao tai khoan", true);
            return "admin/account-form";
        }

        try {
            accountService.createAccount(form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Tao tai khoan thanh cong.");
            return "redirect:/admin/account";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            applyFormPageModel(model, "Tao tai khoan", true);
            return "admin/account-form";
        }
    }

    @GetMapping("/{accountId}/edit")
    public String editForm(@PathVariable("accountId") Integer accountId, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("accountForm")) {
                model.addAttribute("accountForm", accountService.getEditForm(accountId));
            }
            model.addAttribute("accountId", accountId);
            applyFormPageModel(model, "Chinh sua tai khoan", false);
            return "admin/account-form";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/account";
        }
    }

    @PostMapping("/{accountId}/edit")
    public String updateAccount(@PathVariable("accountId") Integer accountId,
                                @Valid @ModelAttribute("accountForm") AccountUpsertForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("accountId", accountId);
            applyFormPageModel(model, "Chinh sua tai khoan", false);
            return "admin/account-form";
        }

        try {
            accountService.updateAccount(accountId, form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Cap nhat tai khoan thanh cong.");
            return "redirect:/admin/account";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("accountId", accountId);
            applyFormPageModel(model, "Chinh sua tai khoan", false);
            return "admin/account-form";
        }
    }

    @PostMapping("/{accountId}/toggle-lock")
    public String toggleLock(@PathVariable("accountId") Integer accountId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            accountService.toggleLock(accountId, principal == null ? null : principal.getName());
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Cap nhat trang thai tai khoan thanh cong.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/account";
    }

    @PostMapping("/{accountId}/delete")
    public String deleteAccount(@PathVariable("accountId") Integer accountId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteAccount(accountId, principal == null ? null : principal.getName());
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Xoa tai khoan thanh cong.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/account";
    }

    private void applyFormPageModel(Model model, String pageTitle, boolean creatingMode) {
        model.addAttribute("activePage", "account");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("creatingMode", creatingMode);
        model.addAttribute("roleSelections", accountService.getRoleSelections());
    }
}
