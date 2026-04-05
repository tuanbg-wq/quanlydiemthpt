package com.quanly.webdiem.model.service.admin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TeacherClassDisplaySupport {

    private static final Pattern CLASS_ID_PREFIX_PATTERN = Pattern.compile("^([A-Za-z0-9]+)");

    private TeacherClassDisplaySupport() {
    }

    static List<String> parseClassIds(String rawValue) {
        String normalizedRawValue = normalize(rawValue);
        if (normalizedRawValue == null) {
            return List.of();
        }

        String[] tokens = normalizedRawValue.split("[,;\\n]+");
        LinkedHashSet<String> uniqueClassIds = new LinkedHashSet<>();
        for (String token : tokens) {
            String classId = extractClassId(token);
            if (classId != null) {
                uniqueClassIds.add(classId);
            }
        }

        return new ArrayList<>(uniqueClassIds);
    }

    static String extractClassId(String rawValue) {
        String normalizedRawValue = normalize(rawValue);
        if (normalizedRawValue == null) {
            return null;
        }

        Matcher matcher = CLASS_ID_PREFIX_PATTERN.matcher(normalizedRawValue);
        if (!matcher.find()) {
            return normalizedRawValue.toUpperCase(Locale.ROOT);
        }

        return matcher.group(1).toUpperCase(Locale.ROOT);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
