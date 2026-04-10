package com.quanly.webdiem.model.service.admin.report;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AdminReportExportHistoryService {

    private static final int MAX_ITEMS = 60;
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final List<HistoryItem> items = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public void append(AdminReportType type,
                       String format,
                       String createdBy,
                       long totalRows,
                       String filterSummary) {
        long id = sequence.incrementAndGet();
        String resolvedCreator = (createdBy == null || createdBy.isBlank()) ? "Quản trị" : createdBy.trim();
        String resolvedFormat = (format == null || format.isBlank()) ? "PDF" : format.trim().toUpperCase();
        LocalDateTime createdAt = LocalDateTime.now();

        HistoryItem item = new HistoryItem(
                id,
                type == null ? "Báo cáo" : type.getTitle(),
                resolvedFormat,
                "Hoàn thành",
                resolvedCreator,
                createdAt.format(DISPLAY_TIME_FORMAT),
                totalRows,
                filterSummary == null || filterSummary.isBlank() ? "Không dùng bộ lọc" : filterSummary
        );

        items.add(0, item);
        trimToMaxSize();
    }

    public List<HistoryItem> getLatest() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    private void trimToMaxSize() {
        while (items.size() > MAX_ITEMS) {
            int lastIndex = items.size() - 1;
            if (lastIndex >= 0) {
                items.remove(lastIndex);
            } else {
                break;
            }
        }
    }

    public static class HistoryItem {
        private final long id;
        private final String reportType;
        private final String format;
        private final String status;
        private final String createdBy;
        private final String createdAt;
        private final long totalRows;
        private final String filterSummary;

        public HistoryItem(long id,
                           String reportType,
                           String format,
                           String status,
                           String createdBy,
                           String createdAt,
                           long totalRows,
                           String filterSummary) {
            this.id = id;
            this.reportType = reportType;
            this.format = format;
            this.status = status;
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.totalRows = totalRows;
            this.filterSummary = filterSummary;
        }

        public long getId() {
            return id;
        }

        public String getReportType() {
            return reportType;
        }

        public String getFormat() {
            return format;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public long getTotalRows() {
            return totalRows;
        }

        public String getFilterSummary() {
            return filterSummary;
        }
    }
}
