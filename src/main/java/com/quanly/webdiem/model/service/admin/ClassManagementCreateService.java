package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.ClassCreateForm;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Course;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ClassManagementCreateService {

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
        String normalizedQuery = normalize(query);
        return teacherDAO.suggestActiveHomeroomTeachers(normalizedQuery).stream()
                .map(this::mapTeacherSuggestion)
                .toList();
    }

    public void createClass(ClassCreateForm form) {
        String className = normalize(form == null ? null : form.getTenLop());
        if (className == null) {
            throw new RuntimeException("Tên lớp học là bắt buộc.");
        }

        String classCode = className.toUpperCase(Locale.ROOT);
        Integer grade = parseGrade(form == null ? null : form.getKhoi());
        if (grade == null) {
            throw new RuntimeException("Khối lớp là bắt buộc.");
        }

        String courseId = normalizeUpper(form == null ? null : form.getIdKhoa());
        if (courseId == null) {
            throw new RuntimeException("Khóa học là bắt buộc.");
        }

        String schoolYear = normalize(form == null ? null : form.getNamHoc());
        if (schoolYear == null) {
            throw new RuntimeException("Năm học là bắt buộc.");
        }

        String homeroomTeacherId = normalizeUpper(form == null ? null : form.getIdGvcn());
        if (homeroomTeacherId == null) {
            throw new RuntimeException("Vui lòng chọn giáo viên chủ nhiệm.");
        }

        if (classDAO.existsById(classCode)) {
            throw new RuntimeException("Lớp học đã tồn tại.");
        }

        Course course = courseDAO.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại."));

        if (teacherDAO.countActiveByTeacherId(homeroomTeacherId) <= 0) {
            throw new RuntimeException("Giáo viên chủ nhiệm không hợp lệ.");
        }

        ClassEntity classEntity = new ClassEntity();
        classEntity.setIdLop(classCode);
        classEntity.setTenLop(classCode);
        classEntity.setKhoi(grade);
        classEntity.setKhoaHoc(course);
        classEntity.setNamHoc(schoolYear);
        classEntity.setSiSo(0);
        classEntity.setIdGvcn(homeroomTeacherId);

        try {
            classDAO.save(classEntity);
        } catch (DataIntegrityViolationException ex) {
            if (containsIgnoreCase(ex.getMessage(), "unique_gvcn")) {
                throw new RuntimeException("Giáo viên này đã là chủ nhiệm của lớp khác.");
            }
            throw new RuntimeException("Không thể tạo lớp học. Vui lòng kiểm tra lại dữ liệu.");
        }
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

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null || keyword == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }
}
