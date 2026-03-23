package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.form.ClassCreateForm;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.service.shared.ClassCodeSupport;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ClassManagementUpdateService {

    private static final String ERROR_CLASS_NOT_FOUND = "Khong tim thay lop hoc.";
    private static final String ERROR_CLASS_NAME_REQUIRED = "Ten lop hoc la bat buoc.";
    private static final String ERROR_GRADE_REQUIRED = "Khoi lop la bat buoc.";
    private static final String ERROR_COURSE_REQUIRED = "Khoa hoc la bat buoc.";
    private static final String ERROR_SCHOOL_YEAR_REQUIRED = "Nam hoc la bat buoc.";
    private static final String ERROR_HOMEROOM_TEACHER_REQUIRED = "Vui long chon giao vien chu nhiem.";
    private static final String ERROR_HOMEROOM_TEACHER_DUPLICATE_NAME =
            "Ten giao vien chu nhiem bi trung. Vui long chon dung giao vien tu danh sach goi y.";
    private static final String ERROR_HOMEROOM_TEACHER_INVALID =
            "Giao vien chu nhiem khong hop le. Vui long chon giao vien tu danh sach goi y.";
    private static final String ERROR_HOMEROOM_TEACHER_ALREADY_ASSIGNED =
            "Giao vien nay da la chu nhiem cua lop khac.";
    private static final String ERROR_COURSE_NOT_FOUND = "Khoa hoc khong ton tai.";
    private static final String ERROR_NOTE_TOO_LONG = "Ghi chu khong duoc vuot qua 1000 ky tu.";
    private static final String ERROR_UPDATE_FAILED = "Khong the cap nhat lop hoc. Vui long kiem tra lai du lieu.";

    private final ClassDAO classDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;

    public ClassManagementUpdateService(ClassDAO classDAO,
                                        CourseDAO courseDAO,
                                        TeacherDAO teacherDAO) {
        this.classDAO = classDAO;
        this.courseDAO = courseDAO;
        this.teacherDAO = teacherDAO;
    }

    public ClassCreateForm getClassFormForEdit(String classId) {
        String normalizedClassId = normalizeUpper(classId);
        if (normalizedClassId == null) {
            throw new RuntimeException(ERROR_CLASS_NOT_FOUND);
        }

        ClassEntity classEntity = classDAO.findById(normalizedClassId)
                .orElseThrow(() -> new RuntimeException(ERROR_CLASS_NOT_FOUND));

        ClassCreateForm form = new ClassCreateForm();
        form.setMaLop(classEntity.getIdLop());
        form.setTenLop(classEntity.getTenLop());
        form.setKhoi(classEntity.getKhoi() == null ? "" : String.valueOf(classEntity.getKhoi()));
        form.setIdKhoa(classEntity.getKhoaHoc() == null ? "" : classEntity.getKhoaHoc().getIdKhoa());
        form.setNamHoc(classEntity.getNamHoc());
        form.setIdGvcn(classEntity.getIdGvcn());
        form.setGvcnDisplay(resolveTeacherDisplay(classEntity.getIdGvcn()));
        form.setGhiChu(classEntity.getGhiChu());
        return form;
    }

    public void updateClass(String classId, ClassCreateForm form) {
        String normalizedClassId = normalizeUpper(classId);
        if (normalizedClassId == null) {
            throw new RuntimeException(ERROR_CLASS_NOT_FOUND);
        }

        ClassEntity classEntity = classDAO.findById(normalizedClassId)
                .orElseThrow(() -> new RuntimeException(ERROR_CLASS_NOT_FOUND));

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

        String normalizedClassName;
        try {
            normalizedClassName = ClassCodeSupport.buildFromClassName(courseId, className, grade).className();
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        String schoolYear = normalize(form == null ? null : form.getNamHoc());
        if (schoolYear == null) {
            throw new RuntimeException(ERROR_SCHOOL_YEAR_REQUIRED);
        }
        String note = normalizeNote(form == null ? null : form.getGhiChu());

        String homeroomTeacherId = normalizeUpper(form == null ? null : form.getIdGvcn());
        if (homeroomTeacherId == null) {
            homeroomTeacherId = resolveHomeroomTeacherIdFromDisplayName(
                    form == null ? null : form.getGvcnDisplay(),
                    normalizedClassId
            );
        }

        Course course = courseDAO.findById(courseId)
                .orElseThrow(() -> new RuntimeException(ERROR_COURSE_NOT_FOUND));

        if (teacherDAO.countActiveByTeacherId(homeroomTeacherId) <= 0) {
            throw new RuntimeException(ERROR_HOMEROOM_TEACHER_INVALID);
        }

        if (classDAO.countOtherHomeroomClassesByTeacherId(homeroomTeacherId, normalizedClassId) > 0) {
            throw new RuntimeException(ERROR_HOMEROOM_TEACHER_ALREADY_ASSIGNED);
        }

        classEntity.setTenLop(normalizedClassName);
        classEntity.setKhoi(grade);
        classEntity.setKhoaHoc(course);
        classEntity.setNamHoc(schoolYear);
        classEntity.setIdGvcn(homeroomTeacherId);
        classEntity.setGhiChu(note);

        try {
            classDAO.save(classEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException(ERROR_UPDATE_FAILED);
        }
    }

    private String resolveTeacherDisplay(String teacherId) {
        String normalizedTeacherId = normalizeUpper(teacherId);
        if (normalizedTeacherId == null) {
            return "";
        }

        Teacher teacher = teacherDAO.findById(normalizedTeacherId).orElse(null);
        if (teacher == null || teacher.getHoTen() == null || teacher.getHoTen().isBlank()) {
            return normalizedTeacherId;
        }
        return teacher.getHoTen().trim();
    }

    private String resolveHomeroomTeacherIdFromDisplayName(String displayName, String classId) {
        String normalizedDisplayName = normalize(displayName);
        if (normalizedDisplayName == null) {
            throw new RuntimeException(ERROR_HOMEROOM_TEACHER_REQUIRED);
        }

        List<String> matchedTeacherIds =
                teacherDAO.findAvailableHomeroomTeacherIdsByExactNameForClass(normalizedDisplayName, classId);
        if (matchedTeacherIds.size() == 1) {
            return normalizeUpper(matchedTeacherIds.get(0));
        }
        if (matchedTeacherIds.size() > 1) {
            throw new RuntimeException(ERROR_HOMEROOM_TEACHER_DUPLICATE_NAME);
        }
        throw new RuntimeException(ERROR_HOMEROOM_TEACHER_INVALID);
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
}
