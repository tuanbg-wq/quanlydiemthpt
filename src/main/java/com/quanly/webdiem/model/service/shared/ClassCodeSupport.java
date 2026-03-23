package com.quanly.webdiem.model.service.shared;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClassCodeSupport {

    private static final Pattern CLASS_TOKEN_PATTERN = Pattern.compile("^(?:(10|11|12))?([A-Z]\\d{1,2})$");
    private static final Pattern CLASS_SUFFIX_PATTERN = Pattern.compile("^([A-Z]\\d{1,2})$");

    private ClassCodeSupport() {
    }

    public static ClassCodeParts buildFromClassName(String courseId, String classNameInput, Integer grade) {
        String normalizedCourseId = normalizeUpperAlphaNumeric(courseId);
        if (normalizedCourseId == null) {
            throw new IllegalArgumentException("Ma khoa hoc la bat buoc.");
        }

        if (grade == null || grade < 10 || grade > 12) {
            throw new IllegalArgumentException("Khoi lop khong hop le.");
        }

        String classToken = normalizeUpperAlphaNumeric(classNameInput);
        if (classToken == null) {
            throw new IllegalArgumentException("Ten lop hoc la bat buoc.");
        }

        Matcher matcher = CLASS_TOKEN_PATTERN.matcher(classToken);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Ten lop phai co dang 10A1 hoac A1.");
        }

        String gradeInName = matcher.group(1);
        if (gradeInName != null && Integer.parseInt(gradeInName) != grade) {
            throw new IllegalArgumentException("Khoi khong khop voi ten lop.");
        }

        String suffix = matcher.group(2);
        String classCode = normalizedCourseId + suffix;
        if (classCode.length() > 10) {
            throw new IllegalArgumentException("Ma lop vuot qua 10 ky tu.");
        }

        String normalizedClassName = grade + suffix;
        return new ClassCodeParts(classCode, normalizedClassName, suffix);
    }

    public static ClassCodeParts buildFromClassCode(String courseId, String classCodeInput, Integer grade) {
        String normalizedCourseId = normalizeUpperAlphaNumeric(courseId);
        if (normalizedCourseId == null) {
            throw new IllegalArgumentException("Ma khoa hoc la bat buoc.");
        }

        if (grade == null || grade < 10 || grade > 12) {
            throw new IllegalArgumentException("Khoi lop khong hop le.");
        }

        String classCode = normalizeUpperAlphaNumeric(classCodeInput);
        if (classCode == null) {
            throw new IllegalArgumentException("Ma lop la bat buoc.");
        }

        if (!classCode.startsWith(normalizedCourseId)) {
            throw new IllegalArgumentException("Ma lop phai bat dau bang ma khoa hoc.");
        }

        String suffix = classCode.substring(normalizedCourseId.length());
        if (!CLASS_SUFFIX_PATTERN.matcher(suffix).matches()) {
            throw new IllegalArgumentException("Ma lop phai co dang ma khoa + A1, vi du K06A1.");
        }

        String normalizedClassName = grade + suffix;
        return new ClassCodeParts(classCode, normalizedClassName, suffix);
    }

    public static String normalizeUpperAlphaNumeric(String value) {
        if (value == null) {
            return null;
        }

        String compact = value.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");

        if (compact.isEmpty()) {
            return null;
        }
        return compact;
    }

    public record ClassCodeParts(String classCode, String className, String suffix) {
    }
}
