package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateFilter;
import com.quanly.webdiem.model.service.admin.ConductRewardCreatePageData;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateRequest;
import com.quanly.webdiem.model.service.admin.ConductStudentCandidate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/conduct")
public class ConductCreateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConductCreateController.class);

    private final ConductManagementService conductManagementService;
    private final ActivityLogService activityLogService;
    private final ConductPageSupport conductPageSupport;

    public ConductCreateController(ConductManagementService conductManagementService,
                                   ActivityLogService activityLogService,
                                   ConductPageSupport conductPageSupport) {
        this.conductManagementService = conductManagementService;
        this.activityLogService = activityLogService;
        this.conductPageSupport = conductPageSupport;
    }

    @GetMapping("/reward/create")
    public String rewardCreatePage(@ModelAttribute("filter") ConductRewardCreateFilter filter,
                                   Model model) {
        ConductRewardCreatePageData pageData = loadCreatePageData(filter, model, "thêm khen thưởng");
        model.addAttribute("pageData", pageData);
        model.addAttribute("filter", pageData.getFilter());
        model.addAttribute("form", new ConductRewardCreateRequest());
        conductPageSupport.applyBasePage(model, ConductPageSupport.PAGE_TITLE_CONDUCT_CREATE);
        return "admin/conduct-create";
    }

    @PostMapping("/reward/create")
    public String rewardCreateSubmit(@ModelAttribute("form") ConductRewardCreateRequest form,
                                     Authentication authentication,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        try {
            conductManagementService.createReward(form);
            ConductManagementService.ConductRow latest = conductManagementService.getLatestEventByStudentAndType(
                    form.getStudentId(),
                    ConductManagementService.LOAI_KHEN_THUONG
            );
            activityLogService.logConductCreated(
                    ConductManagementService.LOAI_KHEN_THUONG,
                    latest == null ? null : latest.getEventId(),
                    latest == null ? form.getStudentId() : latest.getIdHocSinh(),
                    latest == null ? null : latest.getTenHocSinh(),
                    latest == null ? form.getSoQuyetDinh() : latest.getSoQuyetDinh(),
                    conductPageSupport.resolveUsername(authentication),
                    conductPageSupport.resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Thêm khen thưởng thành công.");
            return "redirect:/admin/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        conductPageSupport.applyRewardCreateRedirectAttributes(redirectAttributes, form);
        redirectAttributes.addAttribute("studentId", form.getStudentId());
        return "redirect:/admin/conduct/reward/create";
    }

    @GetMapping("/discipline/create")
    public String disciplineCreatePage(@ModelAttribute("filter") ConductRewardCreateFilter filter,
                                       Model model) {
        ConductRewardCreatePageData pageData = loadCreatePageData(filter, model, "thêm kỷ luật");
        model.addAttribute("pageData", pageData);
        model.addAttribute("filter", pageData.getFilter());
        model.addAttribute("form", new ConductRewardCreateRequest());
        conductPageSupport.applyBasePage(model, ConductPageSupport.PAGE_TITLE_CONDUCT_DISCIPLINE_CREATE);
        return "admin/conduct-discipline-create";
    }

    @PostMapping("/discipline/create")
    public String disciplineCreateSubmit(@ModelAttribute("form") ConductRewardCreateRequest form,
                                         Authentication authentication,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        try {
            conductManagementService.createDiscipline(form);
            ConductManagementService.ConductRow latest = conductManagementService.getLatestEventByStudentAndType(
                    form.getStudentId(),
                    ConductManagementService.LOAI_KY_LUAT
            );
            activityLogService.logConductCreated(
                    ConductManagementService.LOAI_KY_LUAT,
                    latest == null ? null : latest.getEventId(),
                    latest == null ? form.getStudentId() : latest.getIdHocSinh(),
                    latest == null ? null : latest.getTenHocSinh(),
                    latest == null ? form.getSoQuyetDinh() : latest.getSoQuyetDinh(),
                    conductPageSupport.resolveUsername(authentication),
                    conductPageSupport.resolveIpAddress(request)
            );
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Thêm kỷ luật thành công.");
            return "redirect:/admin/conduct";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        conductPageSupport.applyRewardCreateRedirectAttributes(redirectAttributes, form);
        redirectAttributes.addAttribute("studentId", form.getStudentId());
        return "redirect:/admin/conduct/discipline/create";
    }

    @GetMapping("/reward/suggest-students")
    @ResponseBody
    public List<ConductStudentCandidate> suggestRewardStudents(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "khoi", required = false) String khoi,
            @RequestParam(value = "lop", required = false) String lop,
            @RequestParam(value = "khoa", required = false) String khoa) {
        return conductManagementService.suggestStudentsForReward(q, khoi, lop, khoa);
    }

    private ConductRewardCreatePageData loadCreatePageData(ConductRewardCreateFilter filter,
                                                           Model model,
                                                           String actionName) {
        try {
            return conductManagementService.getRewardCreatePageData(filter);
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải trang {}", actionName, ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ex.getMessage());
            return conductPageSupport.emptyRewardCreatePageData();
        }
    }
}
