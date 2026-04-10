package com.quanly.webdiem.model.service.admin.report;

import java.util.Collections;
import java.util.List;

public class AdminReportPreview {

    private final List<MetricItem> metrics;
    private final List<String> headers;
    private final List<List<String>> rows;
    private final String emptyMessage;
    private final long totalRows;

    public AdminReportPreview(List<MetricItem> metrics,
                              List<String> headers,
                              List<List<String>> rows,
                              String emptyMessage,
                              long totalRows) {
        this.metrics = metrics == null ? Collections.emptyList() : metrics;
        this.headers = headers == null ? Collections.emptyList() : headers;
        this.rows = rows == null ? Collections.emptyList() : rows;
        this.emptyMessage = emptyMessage;
        this.totalRows = totalRows;
    }

    public List<MetricItem> getMetrics() {
        return metrics;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public String getEmptyMessage() {
        return emptyMessage;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public static class MetricItem {
        private final String label;
        private final String value;
        private final String tone;

        public MetricItem(String label, String value, String tone) {
            this.label = label;
            this.value = value;
            this.tone = tone;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        public String getTone() {
            return tone;
        }
    }
}
