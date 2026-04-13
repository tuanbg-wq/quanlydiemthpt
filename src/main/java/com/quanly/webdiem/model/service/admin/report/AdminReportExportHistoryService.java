package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.dao.ReportExportHistoryDAO;
import com.quanly.webdiem.model.entity.ReportExportHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class AdminReportExportHistoryService {

    private static final int MAX_ITEMS = 60;
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReportExportHistoryDAO reportExportHistoryDAO;

    public AdminReportExportHistoryService(ReportExportHistoryDAO reportExportHistoryDAO) {
        this.reportExportHistoryDAO = reportExportHistoryDAO;
    }

    public void append(AdminReportType type,
                       String format,
                       String createdBy,
                       String createdRoleCode,
                       long totalRows,
                       String filterSummary) {
        String resolvedCreator = (createdBy == null || createdBy.isBlank())
                ? "Qu\u1ea3n tr\u1ecb"
                : createdBy.trim();
        String resolvedRoleCode = normalizeRoleCode(createdRoleCode);
        String resolvedFormat = (format == null || format.isBlank()) ? "PDF" : format.trim().toUpperCase();
        String resolvedFilterSummary = sanitizeFilterSummary(filterSummary);
        LocalDateTime createdAt = LocalDateTime.now();

        ReportExportHistory history = new ReportExportHistory();
        history.setReportType(type == null ? "B\u00e1o c\u00e1o" : type.getTitle());
        history.setReportTypeCode(type == null ? "" : type.getCode());
        history.setExportFormat(resolvedFormat);
        history.setStatus("Ho\u00e0n th\u00e0nh");
        history.setCreatedBy(resolvedCreator);
        history.setCreatedRoleCode(resolvedRoleCode);
        history.setCreatedAt(createdAt);
        history.setTotalRows(Math.max(0L, totalRows));
        history.setFilterSummary(resolvedFilterSummary);

        reportExportHistoryDAO.save(history);
    }

    public List<HistoryItem> getLatest() {
        return reportExportHistoryDAO.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(0, MAX_ITEMS))
                .stream()
                .map(this::toHistoryItem)
                .toList();
    }

    public List<HistoryItem> getLatestByActorAndType(String createdBy,
                                                     String createdRoleCode,
                                                     String reportTypeCode,
                                                     int limit) {
        String resolvedCreator = sanitizeCreatedBy(createdBy);
        String resolvedRoleCode = normalizeRoleCode(createdRoleCode);
        String resolvedTypeCode = normalizeReportTypeCode(reportTypeCode);
        if (resolvedCreator == null || resolvedTypeCode == null) {
            return List.of();
        }

        int pageSize = limit <= 0 ? MAX_ITEMS : Math.min(limit, MAX_ITEMS);
        return reportExportHistoryDAO
                .findAllByCreatedByIgnoreCaseAndCreatedRoleCodeIgnoreCaseAndReportTypeCodeIgnoreCaseOrderByCreatedAtDescIdDesc(
                        resolvedCreator,
                        resolvedRoleCode,
                        resolvedTypeCode,
                        PageRequest.of(0, pageSize)
                )
                .stream()
                .map(this::toHistoryItem)
                .toList();
    }

    private String sanitizeCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.isBlank()) {
            return null;
        }
        return createdBy.trim();
    }

    private String normalizeReportTypeCode(String reportTypeCode) {
        if (reportTypeCode == null || reportTypeCode.isBlank()) {
            return null;
        }
        return reportTypeCode.trim().toLowerCase(Locale.ROOT);
    }

    private String sanitizeFilterSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            return "Kh\u00f4ng d\u00f9ng b\u1ed9 l\u1ecdc";
        }

        String trimmed = summary.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        boolean suspicious = (normalized.length() > 80
                && normalized.contains("=")
                && normalized.contains(",")
                && ((normalized.contains("sec-fetch") && normalized.contains("user-agent"))
                || (normalized.contains("accept-language") && normalized.contains("cookie=jsessionid"))))
                || normalized.startsWith("{sec-fetch-")
                || normalized.contains("sec-fetch-mode=")
                || normalized.contains("user-agent=")
                || normalized.contains("cookie=jsessionid")
                || normalized.contains("accept-language=");

        if (suspicious) {
            return "Kh\u00f4ng d\u00f9ng b\u1ed9 l\u1ecdc";
        }

        return trimmed.length() > 220 ? trimmed.substring(0, 220) : trimmed;
    }

    private String normalizeRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return "ADMIN";
        }
        String normalized = roleCode.trim().toUpperCase(Locale.ROOT);
        if ("GVCN".equals(normalized) || "GVBM".equals(normalized) || "ADMIN".equals(normalized)) {
            return normalized;
        }
        return "ADMIN";
    }

    private String displayRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return "Admin";
        }
        return switch (roleCode.trim().toUpperCase(Locale.ROOT)) {
            case "GVCN" -> "GVCN";
            case "GVBM" -> "GVBM";
            default -> "Admin";
        };
    }

    private HistoryItem toHistoryItem(ReportExportHistory history) {
        LocalDateTime createdAt = history.getCreatedAt();
        String createdAtText = createdAt == null ? "" : createdAt.format(DISPLAY_TIME_FORMAT);
        long totalRows = history.getTotalRows() == null ? 0L : history.getTotalRows();
        long id = history.getId() == null ? 0L : history.getId();
        String roleCode = normalizeRoleCode(history.getCreatedRoleCode());

        return new HistoryItem(
                id,
                history.getReportType(),
                history.getReportTypeCode(),
                history.getExportFormat(),
                history.getStatus(),
                history.getCreatedBy(),
                roleCode,
                displayRole(roleCode),
                createdAtText,
                createdAt,
                totalRows,
                history.getFilterSummary()
        );
    }

    public static class HistoryItem {
        private final long id;
        private final String reportType;
        private final String reportTypeCode;
        private final String format;
        private final String status;
        private final String createdBy;
        private final String createdRoleCode;
        private final String createdRole;
        private final String createdAt;
        private final LocalDateTime createdAtValue;
        private final long totalRows;
        private final String filterSummary;

        public HistoryItem(long id,
                           String reportType,
                           String reportTypeCode,
                           String format,
                           String status,
                           String createdBy,
                           String createdRoleCode,
                           String createdRole,
                           String createdAt,
                           LocalDateTime createdAtValue,
                           long totalRows,
                           String filterSummary) {
            this.id = id;
            this.reportType = reportType;
            this.reportTypeCode = reportTypeCode;
            this.format = format;
            this.status = status;
            this.createdBy = createdBy;
            this.createdRoleCode = createdRoleCode;
            this.createdRole = createdRole;
            this.createdAt = createdAt;
            this.createdAtValue = createdAtValue;
            this.totalRows = totalRows;
            this.filterSummary = filterSummary;
        }

        public long getId() {
            return id;
        }

        public String getReportType() {
            return reportType;
        }

        public String getReportTypeCode() {
            return reportTypeCode;
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

        public String getCreatedRoleCode() {
            return createdRoleCode;
        }

        public String getCreatedRole() {
            return createdRole;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getCreatedAtValue() {
            return createdAtValue;
        }

        public long getTotalRows() {
            return totalRows;
        }

        public String getFilterSummary() {
            if (filterSummary == null || filterSummary.isBlank()) {
                return "Kh\u00f4ng d\u00f9ng b\u1ed9 l\u1ecdc";
            }
            String normalized = filterSummary.toLowerCase(Locale.ROOT);
            boolean suspicious = (normalized.length() > 80
                    && normalized.contains("=")
                    && normalized.contains(",")
                    && ((normalized.contains("sec-fetch") && normalized.contains("user-agent"))
                    || (normalized.contains("accept-language") && normalized.contains("cookie=jsessionid"))))
                    || normalized.startsWith("{sec-fetch-")
                    || normalized.contains("sec-fetch-mode=")
                    || normalized.contains("user-agent=")
                    || normalized.contains("cookie=jsessionid")
                    || normalized.contains("accept-language=");
            return suspicious ? "Kh\u00f4ng d\u00f9ng b\u1ed9 l\u1ecdc" : filterSummary;
        }
    }
}
