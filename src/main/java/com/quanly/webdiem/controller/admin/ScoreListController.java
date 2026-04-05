package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.ScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreListExportService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/score")
public class ScoreListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreListController.class);

    private final ScoreManagementService scoreManagementService;
    private final ScoreListExportService scoreListExportService;
    private final ScorePageSupport scorePageSupport;

    public ScoreListController(ScoreManagementService scoreManagementService,
                               ScoreListExportService scoreListExportService,
                               ScorePageSupport scorePageSupport) {
        this.scoreManagementService = scoreManagementService;
        this.scoreListExportService = scoreListExportService;
        this.scorePageSupport = scorePageSupport;
    }

    @GetMapping
    public String scorePage(@ModelAttribute("search") ScoreSearch search,
                            Model model) {
        ScoreManagementService.ScorePageResult pageResult;
        ScoreManagementService.ScoreStats stats;
        List<String> grades;
        List<ScoreManagementService.FilterOption> classes;
        List<ScoreManagementService.FilterOption> subjects;
        List<ScoreManagementService.FilterOption> courses;

        try {
            pageResult = scoreManagementService.search(search);
            stats = scoreManagementService.getStats(search);
            grades = scoreManagementService.getGrades();
            classes = scoreManagementService.getClasses();
            subjects = scoreManagementService.getSubjects();
            courses = scoreManagementService.getCourses();
        } catch (Exception ex) {
            LOGGER.error("Lỗi tải trang danh sách điểm số", ex);
            pageResult = new ScoreManagementService.ScorePageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new ScoreManagementService.ScoreStats(0, 0, 0, 0, 0, 0, 0);
            grades = List.of();
            classes = List.of();
            subjects = List.of();
            courses = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ScorePageSupport.PAGE_ERROR_MESSAGE);
        }

        scorePageSupport.applyListPage(model, pageResult, stats, grades, classes, subjects, courses);
        return "admin/score";
    }

    @GetMapping("/export/excel")
    public Object exportListExcel(@ModelAttribute("search") ScoreSearch search,
                                  RedirectAttributes redirectAttributes) {
        try {
            List<ScoreManagementService.ScoreRow> rows = scoreManagementService.getRowsForExport(search);
            if (rows.isEmpty()) {
                throw new RuntimeException("Khong co du lieu diem phu hop bo loc de xuat Excel.");
            }

            byte[] content = scoreListExportService.exportExcel(rows, search);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .headers(scorePageSupport.downloadHeaders(scorePageSupport.buildListExportFileName("xlsx")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", scorePageSupport.resolveExportErrorMessage(ex));
            scorePageSupport.applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/admin/score";
        }
    }

    @GetMapping("/export/pdf")
    public Object exportListPdf(@ModelAttribute("search") ScoreSearch search,
                                RedirectAttributes redirectAttributes) {
        try {
            List<ScoreManagementService.ScoreRow> rows = scoreManagementService.getRowsForExport(search);
            if (rows.isEmpty()) {
                throw new RuntimeException("Khong co du lieu diem phu hop bo loc de xuat PDF.");
            }

            byte[] content = scoreListExportService.exportPdf(rows, search);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .headers(scorePageSupport.downloadHeaders(scorePageSupport.buildListExportFileName("pdf")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", scorePageSupport.resolveExportErrorMessage(ex));
            scorePageSupport.applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/admin/score";
        }
    }
}
