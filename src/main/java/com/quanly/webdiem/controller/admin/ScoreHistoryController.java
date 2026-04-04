package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.ScoreHistorySearch;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/score/history")
public class ScoreHistoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreHistoryController.class);
    private static final int HISTORY_LIMIT = 200;

    private final ActivityLogService activityLogService;

    public ScoreHistoryController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public String scoreHistoryPage(@ModelAttribute("search") ScoreHistorySearch search, Model model) {
        List<ActivityLogService.ScoreActivityItem> historyItems;
        try {
            historyItems = activityLogService.getRecentScoreActivities(
                    search == null ? null : search.getQ(),
                    search == null ? null : search.getHanhDong(),
                    search == null ? null : search.getVaiTro(),
                    HISTORY_LIMIT
            );
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải lịch sử thao tác điểm", ex);
            historyItems = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Không thể tải lịch sử nhập, sửa, xóa điểm.");
        }

        model.addAttribute("activePage", "score");
        model.addAttribute("pageTitle", "Lịch sử thao tác điểm");
        model.addAttribute("historyItems", historyItems);
        model.addAttribute("historyLimit", HISTORY_LIMIT);
        model.addAttribute("historyCount", historyItems.size());
        return "admin/score-history";
    }
}
