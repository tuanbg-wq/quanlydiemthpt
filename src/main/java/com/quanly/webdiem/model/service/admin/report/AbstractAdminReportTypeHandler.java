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
        return value;
    }
}
