package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/score")
public class ScoreEditController {

    private final ScoreManagementService scoreManagementService;
    private final ScoreCreateService scoreCreateService;
    private final ScorePageSupport scorePageSupport;

    public ScoreEditController(ScoreManagementService scoreManagementService,
                               ScoreCreateService scoreCreateService,
                               ScorePageSupport scorePageSupport) {
        this.scoreManagementService = scoreManagementService;
        this.scoreCreateService = scoreCreateService;
        this.scorePageSupport = scorePageSupport;
    }

    @GetMapping("/edit")
    public String scoreEditPage(@RequestParam(value = "studentId", required = false) String studentId,
                                @RequestParam(value = "subjectId", required = false) String subjectId,
                                @RequestParam(value = "mon", required = false) String mon,
                                @RequestParam(value = "namHoc", required = false) String namHoc,
                                @RequestParam(value = "hocKy", required = false) String hocKy,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String resolvedStudentId = scorePageSupport.safeTrim(studentId);
        String resolvedSubjectId = scorePageSupport.firstNonBlank(subjectId, mon);
        String resolvedNamHoc = scorePageSupport.safeTrim(namHoc);

        if (resolvedStudentId == null || resolvedSubjectId == null || resolvedNamHoc == null) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Thiếu thông tin để mở trang chỉnh sửa điểm.");
            return "redirect:/admin/score";
        }

        try {
            ScoreManagementService.ScoreGroupSummary summary = scoreManagementService.getScoreGroupSummary(
                    resolvedStudentId,
                    resolvedSubjectId,
                    resolvedNamHoc
            );
            ScoreCreateService.ScoreCreateFilter filter = new ScoreCreateService.ScoreCreateFilter();
            filter.setStudentId(resolvedStudentId);
            filter.setMon(resolvedSubjectId);
            filter.setNamHoc(resolvedNamHoc);
            filter.setHocKy(scorePageSupport.resolveSemester(hocKy));
            filter.setApplyFilter("1");

            ScoreCreateService.ScoreCreatePageData pageData = scoreCreateService.getCreatePageData(filter);
            scorePageSupport.applyEditPage(model, summary, pageData);
            return "admin/score-create";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/score";
        }
    }

    @PostMapping("/edit")
    public String scoreEditSubmit(@ModelAttribute ScoreCreateService.ScoreSaveRequest request,
                                  RedirectAttributes redirectAttributes) {
        request.setKhoi(null);
        request.setKhoa(null);
        request.setLop(null);
        request.setQ(null);

        String targetSemester = scorePageSupport.resolveSemester(request.getHocKy());
        String studentId = request.getStudentId();
        String subjectId = request.getMon();
        String namHoc = request.getNamHoc();
        try {
            scoreCreateService.save(request);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ScorePageSupport.FLASH_UPDATE_SUCCESS);
            return "redirect:/admin/score";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", scorePageSupport.resolveEditErrorMessage(ex));
        }

        return "redirect:/admin/score/edit?studentId=" + studentId
                + "&subjectId=" + subjectId
                + "&namHoc=" + namHoc
                + "&hocKy=" + targetSemester;
    }
}
