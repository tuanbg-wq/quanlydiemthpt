package com.quanly.webdiem.model.service.shared;

import com.quanly.webdiem.model.service.admin.SubjectService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SubjectSharedService {

    public static final String HOC_KY_HK1 = "HK1";
    public static final String HOC_KY_HK2 = "HK2";
    public static final String HOC_KY_CA_NAM = "CA_NAM";

    public String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Integer parseGrade(String khoi) {
        String normalized = normalize(khoi);
        if (normalized == null) {
            return null;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    public String resolveHocKyCode(int hocKyCount, int hocKyMin) {
        if (hocKyCount <= 0) {
            return null;
        }
        if (hocKyCount >= 2) {
            return HOC_KY_CA_NAM;
        }
        if (hocKyMin == 1) {
            return HOC_KY_HK1;
        }
        if (hocKyMin == 2) {
            return HOC_KY_HK2;
        }
        return null;
    }

    public String toHocKyCode(String rawValue) {
        String value = normalize(rawValue);
        if (value == null) {
            return null;
        }

        String upper = value.toUpperCase(Locale.ROOT);
        if (HOC_KY_HK1.equals(upper) || "HOC KY 1".equals(upper)) {
            return HOC_KY_HK1;
        }
        if (HOC_KY_HK2.equals(upper) || "HOC KY 2".equals(upper)) {
            return HOC_KY_HK2;
        }
        if (HOC_KY_CA_NAM.equals(upper) || "CA NAM".equals(upper)) {
            return HOC_KY_CA_NAM;
        }
        return null;
    }

    public String toHocKyDisplay(String hocKyCode) {
        if (HOC_KY_HK1.equals(hocKyCode)) {
            return "Hoc ky 1";
        }
        if (HOC_KY_HK2.equals(hocKyCode)) {
            return "Hoc ky 2";
        }
        return "Ca nam";
    }

    public Map<String, String> parseMetadata(String description) {
        Map<String, String> metadata = new LinkedHashMap<>();
        String normalizedDescription = normalize(description);
        if (normalizedDescription == null) {
            return metadata;
        }

        String[] lines = normalizedDescription.split("\\R");
        for (String line : lines) {
            if (line == null || !line.contains(":")) {
                continue;
            }

            String[] pair = line.split(":", 2);
            String key = normalize(pair[0]);
            String value = pair.length > 1 ? normalize(pair[1]) : null;
            if (key != null && value != null) {
                metadata.put(key, value);
            }
        }

        return metadata;
    }

    public String parseCourseId(String courseText) {
        String value = normalize(courseText);
        if (value == null) {
            return null;
        }

        int dashIndex = value.indexOf(" - ");
        if (dashIndex > 0) {
            return normalize(value.substring(0, dashIndex));
        }
        return value;
    }

    public String parseTeacherId(String teacherText) {
        String value = normalize(teacherText);
        if (value == null || "-".equals(value)) {
            return null;
        }

        int open = value.lastIndexOf('(');
        int close = value.lastIndexOf(')');
        if (open >= 0 && close > open) {
            String id = normalize(value.substring(open + 1, close));
            if (id != null) {
                return id;
            }
        }
        return value;
    }

    public List<String> splitCsv(String input, String regexDelimiter) {
        if (input == null || input.isBlank()) {
            return Collections.emptyList();
        }

        String[] parts = input.split(regexDelimiter);
        List<String> results = new ArrayList<>();
        for (String part : parts) {
            String normalized = normalize(part);
            if (normalized != null && !results.contains(normalized)) {
                results.add(normalized);
            }
        }
        return results;
    }

    public String defaultIfBlank(String value, String fallback) {
        String normalized = normalize(value);
        return normalized == null ? fallback : normalized;
    }

    public SubjectService.TeacherOption mapTeacherOption(Object[] row) {
        String id = asString(row, 0, null);
        String name = asString(row, 1, null);
        return new SubjectService.TeacherOption(id, name);
    }

    public String lowerCaseFirst(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }

    public String asString(Object[] row, int index, String fallback) {
        if (row == null || index >= row.length || row[index] == null) {
            return fallback;
        }
        String value = row[index].toString();
        return value == null ? fallback : value.trim();
    }

    public int asInt(Object[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return 0;
        }

        Object value = row[index];
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
