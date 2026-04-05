package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.ConductEventUpsertRequest;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/conduct")
public class ConductEditController {

    private final ConductManagementService conductManagementService;
    private final ActivityLogService activityLogService;
    private final ConductPageSupport conductPageSupport;

    public ConductEditController(ConductManagementService conductManagementService,
                                 ActivityLogService activityLogService,
                                 ConductPageSupport conductPageSupport) {
        this.conductManagementService = conductManagementService;
        this.activityLogService = activityLogService;
        this.conductPageSupport = conductPageSupport;
    }

    @GetMapping("/{eventId}/edit")
    public String conductEditPage(@PathVariable("eventId") Long eventId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            ConductEventUpsertRequest form = conductManagementService.getEditData(eventId);
            ConductManagementService.ConductRow detail = conductManagementService.getEventDetail(eventId);
            model.addAttribute("form", form);
            model.addAttribute("detail", detail);
            conductPageSupport.applyBasePage(model, ConductPageSupport.PAGE_TITLE_CONDUCT_EDIT);
            return "admin/conduct-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/conduct";
        }
    }

    @PostMapping("/{eventId}/edit")
    public String conductEditSubmit(@PathVariable("eventId") Long eventId,
                                    @ModelAttribute("form") ConductEventUpsertRequest form,
                                    Authentication authentication,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        form.setEventId(eventId);
        try {
            ConductManagementService.ConductRow beforeUpdate = conductManagementService.getEventDetail(eventId);
            conductManagementService.updateEvent(form);
            ConductManagementService.ConductRow afterUpdate = conductManagementService.getEventDetail(eventId);
            ConductManagementService.ConductRow logSource = afterUpdate != null ? afterUpdate : beforeUpdate;
            activityLogService.logConductUpdated(
                    logSource == null ? null : logSource.getLoai(),
                    logSource == null ? eventId : logSource.getEventId(),
                    logSource == null ? null : logSource.getIdHocSinh(),
                    logSource == null ? null : logSource.getTenHocSinh(),
                    logSource == null ? null : logSource.getSoQuyetDinh(),
                    conductPageSupport.resolveUsername(authentication),
                    conductPageSupport.resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Sửa quyết định thành công.");
            return "redirect:/admin/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/conduct/" + eventId + "/edit";
        }
    }

    @PostMapping("/{eventId}/delete")
    public String deleteConductEvent(@PathVariable("eventId") Long eventId,
                                     Authentication authentication,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        try {
            ConductManagementService.ConductRow existing = conductManagementService.getEventDetail(eventId);
            conductManagementService.deleteEvent(eventId);
            activityLogService.logConductDeleted(
                    existing == null ? null : existing.getLoai(),
                    existing == null ? eventId : existing.getEventId(),
                    existing == null ? null : existing.getIdHocSinh(),
                    existing == null ? null : existing.getTenHocSinh(),
                    existing == null ? null : existing.getSoQuyetDinh(),
                    conductPageSupport.resolveUsername(authentication),
                    conductPageSupport.resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Đã xóa quyết định.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/conduct";
    }
}
