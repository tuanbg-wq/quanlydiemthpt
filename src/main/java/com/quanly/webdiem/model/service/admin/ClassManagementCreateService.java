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

    private static final String ERROR_CLASS_NAME_REQUIRED = "Tên lớp học là bắt buộc.";
    private static final String ERROR_GRADE_REQUIRED = "Khối lớp là bắt buộc.";
    private static final String ERROR_COURSE_REQUIRED = "Khóa học là bắt buộc.";
    private static final String ERROR_SCHOOL_YEAR_REQUIRED = "Năm học là bắt buộc.";
    private static final String ERROR_HOMEROOM_TEACHER_REQUIRED = "Vui lòng chọn giáo viên chủ nhiệm.";
    private static final String ERROR_HOMEROOM_TEACHER_DUPLICATE_NAME =
            "Tên giáo viên chủ nhiệm bị trùng. Vui lòng chọn đúng giáo viên từ danh sách gợi ý.";
    private static final String ERROR_HOMEROOM_TEACHER_INVALID =
            "Giáo viên chủ nhiệm không hợp lệ. Vui lòng chọn giáo viên từ danh sách gợi ý.";
    private static final String ERROR_HOMEROOM_TEACHER_ALREADY_ASSIGNED =
            "Giáo viên này đã là chủ nhiệm của lớp khác.";
    private static final String ERROR_CLASS_CODE_MISMATCH = "Mã lớp không khớp với tên lớp.";
    private static final String ERROR_CLASS_ALREADY_EXISTS = "Lớp học đã tồn tại.";
    private static final String ERROR_COURSE_NOT_FOUND = "Khóa học không tồn tại.";
    private static final String ERROR_NOTE_TOO_LONG = "Ghi chú không được vượt quá 1000 ký tự.";
    private static final String ERROR_CREATE_FAILED = "Không thể tạo lớp học. Vui lòng kiểm tra lại dữ liệu.";

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
            labelBuilder.append(" (Khối ").append(grade).append(")");
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