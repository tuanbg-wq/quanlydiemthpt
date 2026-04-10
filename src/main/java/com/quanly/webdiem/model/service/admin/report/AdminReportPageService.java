package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.search.AdminReportSearch;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminReportPageService {

    private final Map<AdminReportType, AdminReportTypeHandler> handlers;
    private final AdminReportExportHistoryService historyService;

    public AdminReportPageService(List<AdminReportTypeHandler> handlers,
                                  AdminReportExportHistoryService historyService) {
        this.handlers = new EnumMap<>(AdminReportType.class);
        handlers.forEach(handler -> this.handlers.put(handler.getType(), handler));
        this.historyService = historyService;
    }

    public ReportPagePayload buildPage(AdminReportSearch search) {
        AdminReportType selectedType = resolveType(search);
        AdminReportTypeHandler handler = resolveHandler(selectedType);
        AdminReportTypeResult result = handler.buildResult(search);

        return new ReportPagePayload(
                selectedType,
                Arrays.asList(AdminReportType.values()),
                result.getFilters(),
                result.getPreview(),
                historyService.getLatest()
        );
    }

    public ExportResult createExport(AdminReportSearch search, String format, String createdBy) {
        AdminReportType selectedType = resolveType(search);
        AdminReportTypeHandler handler = resolveHandler(selectedType);
        AdminReportTypeResult result = handler.buildResult(search);
        String filterSummary = handler.buildFilterSummary(search);
        long totalRows = result.getPreview() == null ? 0 : result.getPreview().getTotalRows();

        historyService.append(selectedType, format, createdBy, totalRows, filterSummary);

        String resolvedFormat = (format == null || format.isBlank()) ? "PDF" : format.trim().toUpperCase();
        String message = "Đã tạo báo cáo " + selectedType.getTitle() + " dạng " + resolvedFormat + " (" + totalRows + " bản ghi).";
        return new ExportResult(message);
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

        public ReportPagePayload(AdminReportType selectedType,
                                 List<AdminReportType> typeCards,
                                 AdminReportFilterBundle filters,
                                 AdminReportPreview preview,
                                 List<AdminReportExportHistoryService.HistoryItem> exportHistory) {
            this.selectedType = selectedType;
            this.typeCards = typeCards;
            this.filters = filters;
            this.preview = preview;
            this.exportHistory = exportHistory;
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
    }

    public static class ExportResult {
        private final String message;

        public ExportResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
