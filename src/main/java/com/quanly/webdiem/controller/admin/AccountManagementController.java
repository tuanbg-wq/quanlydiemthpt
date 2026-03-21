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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/account")
public class AccountManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManagementController.class);
    private static final String PAGE_TITLE = "Qu\u1ea3n l\u00fd t\u00e0i kho\u1ea3n";

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
            model.addAttribute("flashMessage", "Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch t\u00e0i kho\u1ea3n.");
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
        applyFormPageModel(model, "T\u1ea1o t\u00e0i kho\u1ea3n", true, null);
        return "admin/account-form";
    }

    @PostMapping("/create")
    public String createAccount(@Valid @ModelAttribute("accountForm") AccountUpsertForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            applyFormPageModel(model, "T\u1ea1o t\u00e0i kho\u1ea3n", true, null);
            return "admin/account-form";
        }

        try {
            accountService.createAccount(form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "T\u1ea1o t\u00e0i kho\u1ea3n th\u00e0nh c\u00f4ng.");
            return "redirect:/admin/account";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            applyFormPageModel(model, "T\u1ea1o t\u00e0i kho\u1ea3n", true, null);
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
            applyFormPageModel(model, "Ch\u1ec9nh s\u1eeda t\u00e0i kho\u1ea3n", false, accountId);
            return "admin/account-form";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/account";
        }
    }

    @GetMapping("/{accountId}/info")
    public String accountInfo(@PathVariable("accountId") Integer accountId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activePage", "account");
            model.addAttribute("pageTitle", "Th\u00f4ng tin t\u00e0i kho\u1ea3n");
            model.addAttribute("accountInfo", accountService.getAccountInfo(accountId));
            return "admin/account-info";
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
            applyFormPageModel(model, "Ch\u1ec9nh s\u1eeda t\u00e0i kho\u1ea3n", false, accountId);
            return "admin/account-form";
        }

        try {
            accountService.updateAccount(accountId, form);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "C\u1eadp nh\u1eadt t\u00e0i kho\u1ea3n th\u00e0nh c\u00f4ng.");
            return "redirect:/admin/account";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("accountId", accountId);
            applyFormPageModel(model, "Ch\u1ec9nh s\u1eeda t\u00e0i kho\u1ea3n", false, accountId);
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
            redirectAttributes.addFlashAttribute("flashMessage", "C\u1eadp nh\u1eadt tr\u1ea1ng th\u00e1i t\u00e0i kho\u1ea3n th\u00e0nh c\u00f4ng.");
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
            redirectAttributes.addFlashAttribute("flashMessage", "X\u00f3a t\u00e0i kho\u1ea3n th\u00e0nh c\u00f4ng.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/account";
    }

    @GetMapping("/suggest/teachers")
    @ResponseBody
    public List<AccountManagementService.TeacherSuggestionItem> suggestTeachers(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "accountId", required = false) Integer accountId
    ) {
        return accountService.suggestTeachers(query, accountId);
    }

    @GetMapping("/teacher-profile")
    @ResponseBody
    public AccountManagementService.TeacherProfile teacherProfile(
            @RequestParam(name = "teacherId", required = false) String teacherId,
            @RequestParam(name = "accountId", required = false) Integer accountId
    ) {
        return accountService.getTeacherProfile(teacherId, accountId);
    }

    private void applyFormPageModel(Model model, String pageTitle, boolean creatingMode, Integer accountId) {
        model.addAttribute("activePage", "account");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("creatingMode", creatingMode);
        model.addAttribute("roleSelections", accountService.getRoleSelections());
        if (!creatingMode && accountId != null) {
            model.addAttribute("currentPasswordHash", accountService.getCurrentPasswordHash(accountId));
        }
    }
}
