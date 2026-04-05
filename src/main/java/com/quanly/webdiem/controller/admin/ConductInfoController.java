package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.ConductManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/conduct")
public class ConductInfoController {

    private final ConductManagementService conductManagementService;
    private final ConductPageSupport conductPageSupport;

    public ConductInfoController(ConductManagementService conductManagementService,
                                 ConductPageSupport conductPageSupport) {
        this.conductManagementService = conductManagementService;
        this.conductPageSupport = conductPageSupport;
    }

    @GetMapping("/{eventId}/info")
    public String conductInfoPage(@PathVariable("eventId") Long eventId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            ConductManagementService.ConductRow detail = conductManagementService.getEventDetail(eventId);
            model.addAttribute("detail", detail);
            conductPageSupport.applyBasePage(model, ConductPageSupport.PAGE_TITLE_CONDUCT_INFO);
            return "admin/conduct-info";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/conduct";
        }
    }
}
