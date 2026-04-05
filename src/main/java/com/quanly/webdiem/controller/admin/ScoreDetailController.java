package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.admin.ScoreDetailExportService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/score")
public class ScoreDetailController {

    private final ScoreManagementService scoreManagementService;
    private final ScoreDetailExportService scoreDetailExportService;
    private final ScorePageSupport scorePageSupport;

    public ScoreDetailController(ScoreManagementService scoreManagementService,
                                 ScoreDetailExportService scoreDetailExportService,
                                 ScorePageSupport scorePageSupport) {
        this.scoreManagementService = scoreManagementService;
        this.scoreDetailExportService = scoreDetailExportService;
        this.scorePageSupport = scorePageSupport;
    }

    @GetMapping("/detail")
    public String scoreDetailPage(@RequestParam("studentId") String studentId,
                                  @RequestParam("subjectId") String subjectId,
                                  @RequestParam("namHoc") String namHoc,
                                  @RequestParam(value = "hocKy", required = false) String hocKy,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        String selectedSemester = scorePageSupport.resolveSemester(hocKy);
        try {
            ScoreManagementService.ScoreGroupSummary summary =
                    scoreManagementService.getScoreGroupSummary(studentId, subjectId, namHoc);
            ScoreCreateService.ScoreCreatePageData detailData =
                    scorePageSupport.loadDetailData(studentId, subjectId, namHoc, selectedSemester);
            scorePageSupport.applyDetailPage(model, summary, detailData, selectedSemester);
            return "admin/score-detail";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", scorePageSupport.resolveDetailErrorMessage(ex));
            return "redirect:/admin/score";
        }
    }

    @GetMapping("/detail/export/excel")
    public Object exportDetailExcel(@RequestParam("studentId") String studentId,
                                    @RequestParam("subjectId") String subjectId,
                                    @RequestParam("namHoc") String namHoc,
                                    @RequestParam(value = "hocKy", required = false) String hocKy,
                                    RedirectAttributes redirectAttributes) {
        String selectedSemester = scorePageSupport.resolveSemester(hocKy);
        try {
            ScoreManagementService.ScoreGroupSummary summary =
                    scoreManagementService.getScoreGroupSummary(studentId, subjectId, namHoc);
            ScoreCreateService.ScoreCreatePageData detailData =
                    scorePageSupport.loadDetailData(studentId, subjectId, namHoc, selectedSemester);
            scorePageSupport.validateExportEligibility(detailData, selectedSemester);
            byte[] content = scoreDetailExportService.exportExcel(summary, detailData, selectedSemester);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .headers(scorePageSupport.downloadHeaders(
                            scorePageSupport.buildExportFileName(summary, selectedSemester, "xlsx")
                    ))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", scorePageSupport.resolveExportErrorMessage(ex));
            redirectAttributes.addAttribute("studentId", studentId);
            redirectAttributes.addAttribute("subjectId", subjectId);
            redirectAttributes.addAttribute("namHoc", namHoc);
            redirectAttributes.addAttribute("hocKy", selectedSemester);
            return "redirect:/admin/score/detail";
        }
    }

    @GetMapping("/detail/export/pdf")
    public Object exportDetailPdf(@RequestParam("studentId") String studentId,
                                  @RequestParam("subjectId") String subjectId,
                                  @RequestParam("namHoc") String namHoc,
                                  @RequestParam(value = "hocKy", required = false) String hocKy,
                                  RedirectAttributes redirectAttributes) {
        String selectedSemester = scorePageSupport.resolveSemester(hocKy);
        try {
            ScoreManagementService.ScoreGroupSummary summary =
                    scoreManagementService.getScoreGroupSummary(studentId, subjectId, namHoc);
            ScoreCreateService.ScoreCreatePageData detailData =
                    scorePageSupport.loadDetailData(studentId, subjectId, namHoc, selectedSemester);
            scorePageSupport.validateExportEligibility(detailData, selectedSemester);
            byte[] content = scoreDetailExportService.exportPdf(summary, detailData, selectedSemester);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .headers(scorePageSupport.downloadHeaders(
                            scorePageSupport.buildExportFileName(summary, selectedSemester, "pdf")
                    ))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", scorePageSupport.resolveExportErrorMessage(ex));
            redirectAttributes.addAttribute("studentId", studentId);
            redirectAttributes.addAttribute("subjectId", subjectId);
            redirectAttributes.addAttribute("namHoc", namHoc);
            redirectAttributes.addAttribute("hocKy", selectedSemester);
            return "redirect:/admin/score/detail";
        }
    }
}
