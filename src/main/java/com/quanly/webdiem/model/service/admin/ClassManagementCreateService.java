package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.form.ClassCreateForm;
import com.quanly.webdiem.model.service.shared.ClassCodeSupport;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ClassManagementCreateService {

    private static final String ERROR_CLASS_NAME_REQUIRED = "T\u00ean l\u1edbp h\u1ecdc l\u00e0 b\u1eaft bu\u1ed9c.";
    private static final String ERROR_GRADE_REQUIRED = "Kh\u1ed1i l\u1edbp l\u00e0 b\u1eaft bu\u1ed9c.";
    private static final String ERROR_COURSE_REQUIRED = "Kh\u00f3a h\u1ecdc l\u00e0 b\u1eaft bu\u1ed9c.";
    private static final String ERROR_SCHOOL_YEAR_REQUIRED = "N\u0103m h\u1ecdc l\u00e0 b\u1eaft bu\u1ed9c.";
    private static final String ERROR_HOMEROOM_TEACHER_REQUIRED = "Vui l\u00f2ng ch\u1ecdn gi\u00e1o vi\u00ean ch\u1ee7 nhi\u1ec7m.";
    private static final String ERROR_HOMEROOM_TEACHER_DUPLICATE_NAME =
            "T\u00ean gi\u00e1o vi\u00ean ch\u1ee7 nhi\u1ec7m b\u1ecb tr\u00f9ng. Vui l\u00f2ng ch\u1ecdn \u0111\u00fang gi\u00e1o vi\u00ean t\u1eeb danh s\u00e1ch g\u1ee3i \u00fd.";
    private static final String ERROR_HOMEROOM_TEACHER_INVALID =
            "Gi\u00e1o vi\u00ean ch\u1ee7 nhi\u1ec7m kh\u00f4ng h\u1ee3p l\u1ec7. Vui l\u00f2ng ch\u1ecdn gi\u00e1o vi\u00ean t\u1eeb danh s\u00e1ch g\u1ee3i \u00fd.";
    private static final String ERROR_HOMEROOM_TEACHER_ALREADY_ASSIGNED =
            "Gi\u00e1o vi\u00ean n\u00e0y \u0111\u00e3 l\u00e0 ch\u1ee7 nhi\u1ec7m c\u1ee7a l\u1edbp kh\u00e1c.";
    private static final String ERROR_CLASS_CODE_MISMATCH = "M\u00e3 l\u1edbp kh\u00f4ng kh\u1edbp v\u1edbi t\u00ean l\u1edbp.";
    private static final String ERROR_CLASS_ALREADY_EXISTS = "L\u1edbp h\u1ecdc \u0111\u00e3 t\u1ed3n t\u1ea1i.";
    private static final String ERROR_COURSE_NOT_FOUND = "Kh\u00f3a h\u1ecdc kh\u00f4ng t\u1ed3n t\u1ea1i.";
    private static final String ERROR_NOTE_TOO_LONG = "Ghi ch\u00fa kh\u00f4ng \u0111\u01b0\u1ee3c v\u01b0\u1ee3t qu\u00e1 1000 k\u00fd t\u1ef1.";
    private static final String ERROR_CREATE_FAILED = "Kh\u00f4ng th\u1ec3 t\u1ea1o l\u1edbp h\u1ecdc. Vui l\u00f2ng ki\u1ec3m tra l\u1ea1i d\u1eef li\u1ec7u.";

    private final ClassDAO classDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;

    public ClassManagementCreateService(ClassDAO classDAO,
                                        CourseDAO courseDAO,
                                        TeacherDAO teacherDAO) {
        this.classDAO = classDAO;
        this.courseDAO = courseDAO;
        this.teacherDAO = teacherDAO;
    }

    public List<ClassManagementService.CourseOption> getCoursesForCreate() {
        return courseDAO.findAll(Sort.by(Sort.Direction.ASC, "idKhoa")).stream()
                .map(course -> new ClassManagementService.CourseOption(course.getIdKhoa(), course.getTenKhoa()))
                .toList();
    }

    public List<ClassManagementService.SuggestionItem> suggestHomeroomTeachers(String query) {
        return suggestHomeroomTeachers(query, null);
    }

    public List<ClassManagementService.SuggestionItem> suggestHomeroomTeachers(String query, String classId) {
        String normalizedQuery = normalize(query);
        String normalizedClassId = normalizeUpper(classId);

        List<Object[]> rawRows = normalizedClassId == null
                ? teacherDAO.suggestActiveHomeroomTeachers(normalizedQuery)
                : teacherDAO.suggestActiveHomeroomTeachersForClass(normalizedQuery, normalizedClassId);

        return rawRows.stream()
                .map(this::mapTeacherSuggestion)
                .toList();
    }

    public List<ClassManagementService.SuggestionItem> suggestClassCodes(String query,
                                                                          String courseId,
                                                                          String grade,
                                                                          String excludeClassId) {
        String normalizedQuery = normalize(query);
        String normalizedCourseId = normalizeUpper(courseId);
        String normalizedGrade = normalizeGradeForSearch(grade);
        String normalizedExcludeClassId = normalizeUpper(excludeClassId);

        return classDAO.suggestClassCodes(
                        normalizedQuery,
                        normalizedCourseId,
                        normalizedGrade,
                        normalizedExcludeClassId
                ).stream()
                .map(this::mapClassCodeSuggestion)
                .toList();
    }

    public void createClass(ClassCreateForm form) {
        String className = normalize(form == null ? null : form.getTenLop());
        if (className == null) {
            throw new RuntimeException(ERROR_CLASS_NAME_REQUIRED);
        }

        Integer grade = parseGrade(form == null ? null : form.getKhoi());
        if (grade == null) {
            throw new RuntimeException(ERROR_GRADE_REQUIRED);
        }

        String courseId = normalizeUpper(form == null ? null : form.getIdKhoa());
        if (courseId == null) {
            throw new RuntimeException(ERROR_COURSE_REQUIRED);
        }

        ClassCodeSupport.ClassCodeParts classNameParts;
        try {
            classNameParts = ClassCodeSupport.buildFromClassName(courseId, className, grade);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        ClassCodeSupport.ClassCodeParts classCodeParts = resolveClassCodeParts(
                courseId,
                grade,
                classNameParts,
                form == null ? null : form.getMaLop()
        );

        String classCode = classCodeParts.classCode();
        String normalizedClassName = classNameParts.className();

        String schoolYear = normalize(form == null ? null : form.getNamHoc());
        if (schoolYear == null) {
            throw new RuntimeException(ERROR_SCHOOL_YEAR_REQUIRED);
        }

        String homeroomTeacherId = normalizeUpper(form == null ? null : form.getIdGvcn());
        if (homeroomTeacherId == null) {
            homeroomTeacherId = resolveHomeroomTeacherIdFromDisplayName(form == null ? null : form.getGvcnDisplay());
        }
        String note = normalizeNote(form == null ? null : form.getGhiChu());

        if (classDAO.countByClassIdIgnoreCase(classCode) > 0) {
            throw new RuntimeException(ERROR_CLASS_ALREADY_EXISTS);
        }

        Course course = courseDAO.findById(courseId)
                .orElseThrow(() -> new RuntimeException(ERROR_COURSE_NOT_FOUND));

        if (teacherDAO.countActiveByTeacherId(homeroomTeacherId) <= 0) {
            throw new RuntimeException(ERROR_HOMEROOM_TEACHER_INVALID);
        }

        if (teacherDAO.countHomeroomClassReferences(homeroomTeacherId) > 0) {
            throw new RuntimeException(ERROR_HOMEROOM_TEACHER_ALREADY_ASSIGNED);
        }

        ClassEntity classEntity = new ClassEntity();
        classEntity.setIdLop(classCode);
        classEntity.setTenLop(normalizedClassName);
        classEntity.setKhoi(grade);
        classEntity.setKhoaHoc(course);
        classEntity.setNamHoc(schoolYear);
        classEntity.setSiSo(0);
        classEntity.setIdGvcn(homeroomTeacherId);
        classEntity.setGhiChu(note);

        try {
            classDAO.save(classEntity);
        } catch (DataIntegrityViolationException ex) {
            if (containsIgnoreCase(ex.getMessage(), "unique_gvcn")
                    || containsIgnoreCase(ex.getMessage(), "id_gvcn")) {
                throw new RuntimeException(ERROR_HOMEROOM_TEACHER_ALREADY_ASSIGNED);
            }
            throw new RuntimeException(ERROR_CREATE_FAILED);
        }
    }

    private String resolveHomeroomTeacherIdFromDisplayName(String displayName) {
        String normalizedDisplayName = normalize(displayName);
        if (normalizedDisplayName == null) {
            throw new RuntimeException(ERROR_HOMEROOM_TEACHER_REQUIRED);
        }

        List<String> matchedTeacherIds = teacherDAO.findAvailableHomeroomTeacherIdsByExactName(normalizedDisplayName);
        if (matchedTeacherIds.size() == 1) {
            return normalizeUpper(matchedTeacherIds.get(0));
        }
        if (matchedTeacherIds.size() > 1) {
            throw new RuntimeException(ERROR_HOMEROOM_TEACHER_DUPLICATE_NAME);
        }
        throw new RuntimeException(ERROR_HOMEROOM_TEACHER_INVALID);
    }

    private ClassManagementService.SuggestionItem mapTeacherSuggestion(Object[] row) {
        String teacherId = asString(row, 0, "");
        String teacherName = asString(row, 1, teacherId);
        String email = asString(row, 2, "");

        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(teacherName).append(" (").append(teacherId).append(")");
        if (!email.isBlank()) {
            labelBuilder.append(" - ").append(email);
        }

        return new ClassManagementService.SuggestionItem(
                teacherId,
                labelBuilder.toString(),
                teacherName
        );
    }

    private ClassManagementService.SuggestionItem mapClassCodeSuggestion(Object[] row) {
        String classCode = asString(row, 0, "");
        String className = asString(row, 1, classCode);
        String grade = asString(row, 2, "");

        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(classCode);
        if (!className.isBlank() && !className.equalsIgnoreCase(classCode)) {
            labelBuilder.append(" - ").append(className);
        }
        if (!grade.isBlank()) {
            labelBuilder.append(" (Kh\u1ed1i ").append(grade).append(")");
        }

        return new ClassManagementService.SuggestionItem(
                classCode,
                labelBuilder.toString(),
                classCode
        );
    }

    private ClassCodeSupport.ClassCodeParts resolveClassCodeParts(String courseId,
                                                                   Integer grade,
                                                                   ClassCodeSupport.ClassCodeParts classNameParts,
                                                                   String classCodeInput) {
        String normalizedClassCodeInput = normalize(classCodeInput);
        if (normalizedClassCodeInput == null) {
            return classNameParts;
        }

        try {
            ClassCodeSupport.ClassCodeParts classCodeParts =
                    ClassCodeSupport.buildFromClassCode(courseId, normalizedClassCodeInput, grade);
            if (!classCodeParts.suffix().equalsIgnoreCase(classNameParts.suffix())) {
                throw new RuntimeException(ERROR_CLASS_CODE_MISMATCH);
            }
            return classCodeParts;
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private Integer parseGrade(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            int grade = Integer.parseInt(normalized);
            if (grade < 10 || grade > 12) {
                return null;
            }
            return grade;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeGradeForSearch(String value) {
        Integer parsedGrade = parseGrade(value);
        return parsedGrade == null ? null : String.valueOf(parsedGrade);
    }

    private String asString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }

        String value = row[index].toString().trim();
        if (value.isEmpty()) {
            return fallback;
        }
        return value;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private String normalizeUpper(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeNote(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 1000) {
            throw new RuntimeException(ERROR_NOTE_TOO_LONG);
        }
        return trimmed;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null || keyword == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }
}