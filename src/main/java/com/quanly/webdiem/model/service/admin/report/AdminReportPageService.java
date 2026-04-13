package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.search.AdminReportSearch;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AdminReportPageService {

    private static final int PREVIEW_PAGE_SIZE = 6;

    private final Map<AdminReportType, AdminReportTypeHandler> handlers;
    private final AdminReportExportHistoryService historyService;
    private final AdminReportFileExportService fileExportService;

    public AdminReportPageService(List<AdminReportTypeHandler> handlers,
                                  AdminReportExportHistoryService historyService,
                                  AdminReportFileExportService fileExportService) {
        this.handlers = new EnumMap<>(AdminReportType.class);
        handlers.forEach(handler -> this.handlers.put(handler.getType(), handler));
        this.historyService = historyService;
        this.fileExportService = fileExportService;
    }

    public ReportPagePayload buildPage(AdminReportSearch search) {
        AdminReportType selectedType = resolveType(search);
        AdminReportTypeHandler handler = resolveHandler(selectedType);
        AdminReportTypeResult result = handler.buildResult(search);
        boolean previewVisible = search != null && search.isPreviewRequested();
        AdminReportPreview safePreview = sanitizePreview(result.getPreview());

        int previewPage = search == null ? 1 : search.resolvePreviewPageOrDefault();
        int totalPreviewPages = resolveTotalPreviewPages(safePreview);
        if (previewPage > totalPreviewPages) {
            previewPage = totalPreviewPages;
        }
        AdminReportPreview pagedPreview = paginatePreview(safePreview, previewPage);

        return new ReportPagePayload(
                selectedType,
                Arrays.asList(AdminReportType.values()),
                result.getFilters(),
                pagedPreview,
                filterHistory(historyService.getLatest(), search),
                buildHistoryTypeOptions(),
                List.of(
                        new AdminReportFilterOption("", "Tất cả định dạng"),
                        new AdminReportFilterOption("PDF", "PDF"),
                        new AdminReportFilterOption("XLSX", "Excel")
                ),
                List.of(
                        new AdminReportFilterOption("", "Tất cả thời gian"),
                        new AdminReportFilterOption("today", "Hôm nay"),
                        new AdminReportFilterOption("7d", "7 ngày gần nhất"),
                        new AdminReportFilterOption("30d", "30 ngày gần nhất")
                ),
                buildHistoryRoleOptions(),
                previewVisible,
                previewPage,
                totalPreviewPages
        );
    }

    public ExportResult createExport(AdminReportSearch search,
                                     String format,
                                     String createdBy,
                                     String createdRole) {
        AdminReportType selectedType = resolveType(search);
        AdminReportTypeHandler handler = resolveHandler(selectedType);
        AdminReportTypeResult result = handler.buildResult(search);
        AdminReportPreview safePreview = sanitizePreview(result.getPreview());
        if (safePreview.getRows().isEmpty()) {
            throw new IllegalStateException("Không có dữ liệu phù hợp bộ lọc để xuất file.");
        }

        String resolvedFormat = normalizeFormat(format);
        AdminReportFileExportService.ExportFile exportFile = fileExportService.createFile(
                selectedType,
                safePreview,
                resolvedFormat
        );

        String filterSummary = handler.buildFilterSummary(search);
        long totalRows = safePreview.getRows().size();
        historyService.append(selectedType, resolvedFormat, createdBy, createdRole, totalRows, filterSummary);

        String fileName = buildFileName(selectedType, exportFile.getExtension());
        String message = "Đã tạo báo cáo " + selectedType.getTitle() + " dạng "
                + displayFormat(resolvedFormat) + " (" + totalRows + " bản ghi).";
        return new ExportResult(
                message,
                fileName,
                exportFile.getContent(),
                exportFile.getMediaType(),
                resolvedFormat,
                totalRows
        );
    }

    private String buildFileName(AdminReportType selectedType, String extension) {
        String code = selectedType == null ? "report" : selectedType.getCode();
        String safeExtension = (extension == null || extension.isBlank()) ? "pdf" : extension.trim().toLowerCase(Locale.ROOT);
        return "admin-report-" + code + "-" + LocalDate.now() + "." + safeExtension;
    }

    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            return "PDF";
        }
        String normalized = format.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("PDF") && !normalized.equals("XLSX")) {
            return "PDF";
        }
        return normalized;
    }

    private String displayFormat(String format) {
        if ("XLSX".equals(format)) {
            return "Excel";
        }
        return format;
    }

    private List<AdminReportFilterOption> buildHistoryTypeOptions() {
        List<AdminReportFilterOption> options = new ArrayList<>();
        options.add(new AdminReportFilterOption("", "Tất cả loại báo cáo"));
        for (AdminReportType value : AdminReportType.values()) {
            options.add(new AdminReportFilterOption(value.getCode(), value.getTitle()));
        }
        return options;
    }

    private List<AdminReportFilterOption> buildHistoryRoleOptions() {
        return List.of(
                new AdminReportFilterOption("", "Tất cả vai trò"),
                new AdminReportFilterOption("ADMIN", "Admin"),
                new AdminReportFilterOption("GVCN", "GVCN"),
                new AdminReportFilterOption("GVBM", "GVBM")
        );
    }

    private List<AdminReportExportHistoryService.HistoryItem> filterHistory(
            List<AdminReportExportHistoryService.HistoryItem> items,
            AdminReportSearch search) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        if (search == null) {
            return items;
        }

        String typeFilter = normalize(search.getHistoryType());
        String formatFilter = normalizeUpper(search.getHistoryFormat());
        String timeFilter = normalize(search.getHistoryTime());
        String roleFilter = normalizeUpper(search.getHistoryRole());
        LocalDate selectedDate = parseDate(search.getHistoryDate());
        YearMonth selectedMonth = parseYearMonth(search.getHistoryMonth());
        Integer selectedYear = parseYear(search.getHistoryYear());

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        return items.stream()
                .filter(item -> typeFilter == null
                        || typeFilter.equals(normalize(item.getReportTypeCode())))
                .filter(item -> formatFilter == null
                        || formatFilter.equals(normalizeUpper(item.getFormat())))
                .filter(item -> roleFilter == null
                        || roleFilter.equals(normalizeUpper(item.getCreatedRoleCode())))
                .filter(item -> {
                    LocalDateTime createdAt = item.getCreatedAtValue();
                    if (createdAt == null) {
                        return false;
                    }
                    LocalDate createdDate = createdAt.toLocalDate();
                    if (selectedDate != null && !createdDate.isEqual(selectedDate)) {
                        return false;
                    }
                    if (selectedMonth != null && !YearMonth.from(createdDate).equals(selectedMonth)) {
                        return false;
                    }
                    if (selectedYear != null && createdDate.getYear() != selectedYear) {
                        return false;
                    }
                    if (timeFilter == null) {
                        return true;
                    }
                    return switch (timeFilter) {
                        case "today" -> createdDate.isEqual(today);
                        case "7d" -> !createdAt.isBefore(now.minusDays(7));
                        case "30d" -> !createdAt.isBefore(now.minusDays(30));
                        default -> true;
                    };
                })
                .toList();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private YearMonth parseYearMonth(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Integer parseYear(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int year = Integer.parseInt(value.trim());
            return year > 0 ? year : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeUpper(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private AdminReportPreview sanitizePreview(AdminReportPreview preview) {
        if (preview == null) {
            return new AdminReportPreview(List.of(), List.of(), List.of(), "Không có dữ liệu.", 0);
        }

        List<List<String>> safeRows = new ArrayList<>();
        long droppedRows = 0;

        for (List<String> row : preview.getRows()) {
            if (row == null || row.isEmpty()) {
                continue;
            }

            boolean suspiciousRow = false;
            List<String> safeCells = new ArrayList<>();
            for (String cell : row) {
                if (looksLikeHeaderNoise(cell)) {
                    suspiciousRow = true;
                    break;
                }
                safeCells.add(sanitizeCell(cell));
            }

            if (suspiciousRow) {
                droppedRows++;
                continue;
            }
            if (safeCells.stream().noneMatch(this::hasData)) {
                droppedRows++;
                continue;
            }
            safeRows.add(safeCells);
        }

        long safeTotalRows = preview.getTotalRows() - droppedRows;
        if (safeTotalRows < 0) {
            safeTotalRows = 0;
        }

        return new AdminReportPreview(
                preview.getMetrics(),
                preview.getHeaders(),
                safeRows,
                preview.getEmptyMessage(),
                safeTotalRows
        );
    }

    private int resolveTotalPreviewPages(AdminReportPreview preview) {
        if (preview == null) {
            return 1;
        }
        int totalRows = preview.getRows() == null ? 0 : preview.getRows().size();
        if (totalRows <= 0) {
            return 1;
        }
        return (totalRows + PREVIEW_PAGE_SIZE - 1) / PREVIEW_PAGE_SIZE;
    }

    private AdminReportPreview paginatePreview(AdminReportPreview preview, int page) {
        if (preview == null || preview.getRows() == null || preview.getRows().isEmpty()) {
            return preview;
        }
        int safePage = Math.max(1, page);
        int fromIndex = (safePage - 1) * PREVIEW_PAGE_SIZE;
        if (fromIndex >= preview.getRows().size()) {
            fromIndex = 0;
        }
        int toIndex = Math.min(fromIndex + PREVIEW_PAGE_SIZE, preview.getRows().size());
        List<List<String>> pageRows = preview.getRows().subList(fromIndex, toIndex);
        return new AdminReportPreview(
                preview.getMetrics(),
                preview.getHeaders(),
                pageRows,
                preview.getEmptyMessage(),
                preview.getTotalRows()
        );
    }

    private boolean hasData(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return !"-".equals(value.trim());
    }

    private String sanitizeCell(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String trimmed = value.trim();
        if (looksLikeHeaderNoise(trimmed)) {
            return "-";
        }
        if (trimmed.length() > 160) {
            return trimmed.substring(0, 160);
        }
        return trimmed;
    }

    private boolean looksLikeHeaderNoise(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return false;
        }

        if ((normalized.startsWith("{") && normalized.contains("=") && normalized.contains(","))
                || (normalized.length() > 80 && normalized.contains("=") && normalized.contains(","))) {
            int headerSignals = 0;
            if (normalized.contains("sec-fetch")) headerSignals++;
            if (normalized.contains("user-agent")) headerSignals++;
            if (normalized.contains("accept-language")) headerSignals++;
            if (normalized.contains("accept-encoding")) headerSignals++;
            if (normalized.contains("cookie=")) headerSignals++;
            if (normalized.contains("referer=http")) headerSignals++;
            if (normalized.contains("host=localhost")) headerSignals++;
            if (normalized.contains("connection=keep-alive")) headerSignals++;
            if (headerSignals >= 2) {
                return true;
            }
        }

        return normalized.startsWith("{sec-fetch-")
                || normalized.startsWith("sec-fetch-")
                || normalized.contains("sec-fetch-mode=")
                || normalized.contains("sec-fetch-site=")
                || normalized.contains("sec-fetch-dest=")
                || normalized.contains("accept-language=")
                || normalized.contains("accept-encoding=")
                || normalized.contains("user-agent=")
                || normalized.contains("cookie=jsessionid")
                || normalized.contains("sec-ch-ua")
                || normalized.contains("upgrade-insecure-requests=")
                || normalized.contains("connection=keep-alive")
                || normalized.contains("host=localhost")
                || normalized.contains("referer=http");
    }

    private AdminReportType resolveType(AdminReportSearch search) {
        if (search == null) {
            return AdminReportType.SCORE;
        }
        return AdminReportType.fromCode(search.getType());
    }

    private AdminReportTypeHandler resolveHandler(AdminReportType type) {
        AdminReportTypeHandler handler = handlers.get(type);
        if (handler != null) {
            return handler;
        }
        AdminReportTypeHandler fallback = handlers.get(AdminReportType.SCORE);
        if (fallback == null) {
            throw new IllegalStateException("Không tìm thấy handler cho báo cáo SCORE.");
        }
        return fallback;
    }

    public static class ReportPagePayload {
        private final AdminReportType selectedType;
        private final List<AdminReportType> typeCards;
        private final AdminReportFilterBundle filters;
        private final AdminReportPreview preview;
        private final List<AdminReportExportHistoryService.HistoryItem> exportHistory;
        private final List<AdminReportFilterOption> historyTypeOptions;
        private final List<AdminReportFilterOption> historyFormatOptions;
        private final List<AdminReportFilterOption> historyTimeOptions;
        private final List<AdminReportFilterOption> historyRoleOptions;
        private final boolean previewVisible;
        private final int previewPage;
        private final int totalPreviewPages;

        public ReportPagePayload(AdminReportType selectedType,
                                 List<AdminReportType> typeCards,
                                 AdminReportFilterBundle filters,
                                 AdminReportPreview preview,
                                 List<AdminReportExportHistoryService.HistoryItem> exportHistory,
                                 List<AdminReportFilterOption> historyTypeOptions,
                                 List<AdminReportFilterOption> historyFormatOptions,
                                 List<AdminReportFilterOption> historyTimeOptions,
                                 List<AdminReportFilterOption> historyRoleOptions,
                                 boolean previewVisible,
                                 int previewPage,
                                 int totalPreviewPages) {
            this.selectedType = selectedType;
            this.typeCards = typeCards;
            this.filters = filters;
            this.preview = preview;
            this.exportHistory = exportHistory;
            this.historyTypeOptions = historyTypeOptions;
            this.historyFormatOptions = historyFormatOptions;
            this.historyTimeOptions = historyTimeOptions;
            this.historyRoleOptions = historyRoleOptions;
            this.previewVisible = previewVisible;
            this.previewPage = previewPage;
            this.totalPreviewPages = totalPreviewPages;
        }

        public AdminReportType getSelectedType() {
            return selectedType;
        }

        public List<AdminReportType> getTypeCards() {
            return typeCards;
        }

        public AdminReportFilterBundle getFilters() {
            return filters;
        }

        public AdminReportPreview getPreview() {
            return preview;
        }

        public List<AdminReportExportHistoryService.HistoryItem> getExportHistory() {
            return exportHistory;
        }

        public List<AdminReportFilterOption> getHistoryTypeOptions() {
            return historyTypeOptions;
        }

        public List<AdminReportFilterOption> getHistoryFormatOptions() {
            return historyFormatOptions;
        }

        public List<AdminReportFilterOption> getHistoryTimeOptions() {
            return historyTimeOptions;
        }

        public List<AdminReportFilterOption> getHistoryRoleOptions() {
            return historyRoleOptions;
        }

        public boolean isPreviewVisible() {
            return previewVisible;
        }

        public int getPreviewPage() {
            return previewPage;
        }

        public int getTotalPreviewPages() {
            return totalPreviewPages;
        }
    }

    public static class ExportResult {
        private final String message;
        private final String fileName;
        private final byte[] content;
        private final MediaType mediaType;
        private final String format;
        private final long totalRows;

        public ExportResult(String message,
                            String fileName,
                            byte[] content,
                            MediaType mediaType,
                            String format,
                            long totalRows) {
            this.message = message;
            this.fileName = fileName;
            this.content = content;
            this.mediaType = mediaType;
            this.format = format;
            this.totalRows = totalRows;
        }

        public String getMessage() {
            return message;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public String getFormat() {
            return format;
        }

        public long getTotalRows() {
            return totalRows;
        }
    }
}
