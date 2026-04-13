package com.quanly.webdiem.model.service.admin.report;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

abstract class AbstractAdminReportTypeHandler implements AdminReportTypeHandler {

    protected List<AdminReportFilterOption> defaultSchoolYearOptions() {
        int currentYear = LocalDate.now().getYear();
        List<AdminReportFilterOption> options = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int startYear = currentYear - i;
            String schoolYear = startYear + "-" + (startYear + 1);
            options.add(new AdminReportFilterOption(schoolYear, schoolYear));
        }
        return options;
    }

    protected List<AdminReportFilterOption> defaultSemesterOptions() {
        return List.of(
                new AdminReportFilterOption("", "Tất cả học kỳ"),
                new AdminReportFilterOption("0", "Cả năm"),
                new AdminReportFilterOption("1", "Học kỳ 1"),
                new AdminReportFilterOption("2", "Học kỳ 2")
        );
    }

    protected AdminReportFilterOption option(String value, String label) {
        return new AdminReportFilterOption(value, label);
    }

    protected String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    protected String normalizeAscii(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String decomposed = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "");
    }

    protected String normalizeSortValue(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        String ascii = decomposed.replaceAll("\\p{M}+", "");
        return ascii.toLowerCase(Locale.ROOT);
    }

    protected String extractGivenNameForSort(String fullName) {
        String normalized = normalizeSortValue(fullName).replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }
        int lastSpace = normalized.lastIndexOf(' ');
        return lastSpace < 0 ? normalized : normalized.substring(lastSpace + 1);
    }

    protected String extractNamePrefixForSort(String fullName) {
        String normalized = normalizeSortValue(fullName).replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }
        int lastSpace = normalized.lastIndexOf(' ');
        return lastSpace < 0 ? "" : normalized.substring(0, lastSpace);
    }

    protected Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    protected String fallback(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String trimmed = value.trim();
        if (looksLikeHttpHeaderDump(trimmed)) {
            return "-";
        }
        if (trimmed.length() > 160) {
            return trimmed.substring(0, 160);
        }
        return trimmed;
    }

    protected boolean containsHeaderNoise(String... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (String value : values) {
            if (looksLikeHttpHeaderDump(value)) {
                return true;
            }
        }
        return false;
    }

    protected List<String> sanitizeRow(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> sanitized = new ArrayList<>();
        for (String value : values) {
            sanitized.add(fallback(value));
        }
        return sanitized;
    }

    protected boolean hasMeaningfulCell(List<String> row) {
        if (row == null || row.isEmpty()) {
            return false;
        }
        return row.stream().anyMatch(cell -> cell != null && !cell.isBlank() && !"-".equals(cell.trim()));
    }

    protected boolean looksLikeHttpHeaderDump(String value) {
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
                || normalized.contains("accept-language=")
                || normalized.contains("user-agent=")
                || normalized.contains("cookie=jsessionid")
                || normalized.contains("sec-ch-ua")
                || normalized.contains("upgrade-insecure-requests=")
                || normalized.contains("accept-encoding=")
                || normalized.contains("referer=http")
                || normalized.contains("sec-fetch-dest=")
                || normalized.contains("host=localhost")
                || normalized.contains("connection=keep-alive");
    }
}
