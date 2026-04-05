package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.ConductListExportService;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.admin.ConductStudentCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/conduct")
public class ConductListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConductListController.class);

    private final ConductManagementService conductManagementService;
    private final ConductListExportService conductListExportService;
    private final ActivityLogService activityLogService;
    private final ConductPageSupport conductPageSupport;

    public ConductListController(ConductManagementService conductManagementService,
                                 ConductListExportService conductListExportService,
                                 ActivityLogService activityLogService,
                                 ConductPageSupport conductPageSupport) {
        this.conductManagementService = conductManagementService;
        this.conductListExportService = conductListExportService;
        this.activityLogService = activityLogService;
        this.conductPageSupport = conductPageSupport;
    }

    @GetMapping
    public String conductPage(@ModelAttribute("search") ConductSearch search, Model model) {
        ConductManagementService.ConductPageResult pageResult;
        ConductManagementService.ConductStats stats;
        List<String> grades;
        List<ConductManagementService.FilterOption> classes;
        List<ConductManagementService.FilterOption> courses;
        List<ActivityLogService.ConductActivityItem> activityLogs;

        try {
            pageResult = conductManagementService.search(search);
            stats = conductManagementService.getStats(search);
            grades = conductManagementService.getGrades();
            classes = conductManagementService.getClasses();
            courses = conductManagementService.getCourses();
            activityLogs = activityLogService.getRecentConductActivities(null, 20);
        } catch (Exception ex) {
            LOGGER.error("Lỗi tải trang khen thưởng/kỷ luật", ex);
            pageResult = new ConductManagementService.ConductPageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new ConductManagementService.ConductStats(0, 0, 0, 0, 0);
            grades = List.of();
            classes = List.of();
            courses = List.of();
            activityLogs = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", ConductPageSupport.PAGE_ERROR_MESSAGE);
        }

        conductPageSupport.applyListPage(model, pageResult, stats, grades, classes, courses, activityLogs);
        return "admin/conduct";
    }

    @GetMapping("/suggest-students")
    @ResponseBody
    public List<ConductStudentCandidate> suggestManagementStudents(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "khoi", required = false) String khoi,
            @RequestParam(value = "lop", required = false) String lop,
            @RequestParam(value = "khoa", required = false) String khoa) {
        return conductManagementService.suggestStudentsForReward(q, khoi, lop, khoa);
    }

    @GetMapping("/export/excel")
    public Object exportListExcel(@ModelAttribute("search") ConductSearch search,
                                  RedirectAttributes redirectAttributes) {
        try {
            List<ConductManagementService.ConductRow> rows = conductManagementService.getRowsForExport(search);
            if (rows.isEmpty()) {
                throw new RuntimeException("Không có dữ liệu khen thưởng/kỷ luật phù hợp để xuất Excel.");
            }

            byte[] content = conductListExportService.exportExcel(rows, search);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .headers(conductPageSupport.downloadHeaders(conductPageSupport.buildListExportFileName("xlsx")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", conductPageSupport.resolveExportErrorMessage(ex));
            conductPageSupport.applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/admin/conduct";
        }
    }

    @GetMapping("/export/pdf")
    public Object exportListPdf(@ModelAttribute("search") ConductSearch search,
                                RedirectAttributes redirectAttributes) {
        try {
            List<ConductManagementService.ConductRow> rows = conductManagementService.getRowsForExport(search);
            if (rows.isEmpty()) {
                throw new RuntimeException("Không có dữ liệu khen thưởng/kỷ luật phù hợp để xuất PDF.");
            }

            byte[] content = conductListExportService.exportPdf(rows, search);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .headers(conductPageSupport.downloadHeaders(conductPageSupport.buildListExportFileName("pdf")))
                    .body(content);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", conductPageSupport.resolveExportErrorMessage(ex));
            conductPageSupport.applySearchRedirectAttributes(redirectAttributes, search);
            return "redirect:/admin/conduct";
        }
    }
}
