package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.form.ClassCreateForm;
import com.quanly.webdiem.model.service.shared.ClassCodeSupport;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ClassManagementUpdateService {

    private static final String ERROR_CLASS_NOT_FOUND = "Kh\u00f4ng t\u00ecm th\u1ea5y l\u1edbp h\u1ecdc.";
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
    private static final String ERROR_CLASS_ALREADY_EXISTS = "M\u00e3 l\u1edbp \u0111\u00e3 t\u1ed3n t\u1ea1i.";
    private static final String ERROR_CLASS_RENAME_BLOCKED = "Kh\u00f4ng th\u1ec3 \u0111\u1ed5i m\u00e3 l\u1edbp do c\u00f3 d\u1eef li\u1ec7u li\u00ean quan.";
    private static final String ERROR_CLASS_RENAME_FAILED = "Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt m\u00e3 l\u1edbp h\u1ecdc.";
    private static final String ERROR_COURSE_NOT_FOUND = "Kh\u00f3a h\u1ecdc kh\u00f4ng t\u1ed3n t\u1ea1i.";
    private static final String ERROR_NOTE_TOO_LONG = "Ghi ch\u00fa kh\u00f4ng \u0111\u01b0\u1ee3c v\u01b0\u1ee3t qu\u00e1 1000 k\u00fd t\u1ef1.";
    private static final String ERROR_UPDATE_FAILED = "Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt l\u1edbp h\u1ecdc. Vui l\u00f2ng ki\u1ec3m tra l\u1ea1i d\u1eef li\u1ec7u.";

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

    @Transactional
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

        String targetClassCode = classCodeParts.classCode();
        String normalizedClassName = classNameParts.className();

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

        if (!normalizedClassId.equalsIgnoreCase(targetClassCode)
                && classDAO.countByClassIdIgnoreCase(targetClassCode) > 0) {
            throw new RuntimeException(ERROR_CLASS_ALREADY_EXISTS);
        }

        classEntity.setTenLop(normalizedClassName);
        classEntity.setKhoi(grade);
        classEntity.setKhoaHoc(course);
        classEntity.setNamHoc(schoolYear);
        classEntity.setIdGvcn(homeroomTeacherId);
        classEntity.setGhiChu(note);

        try {
            classDAO.save(classEntity);
            classDAO.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException(ERROR_UPDATE_FAILED);
        }

        if (!normalizedClassId.equalsIgnoreCase(targetClassCode)) {
            renameClassWithReferences(normalizedClassId, targetClassCode, homeroomTeacherId);
        }
    }

    private void renameClassWithReferences(String sourceClassCode,
                                           String targetClassCode,
                                           String homeroomTeacherId) {
        try {
            int created = classDAO.createCloneForCodeRename(sourceClassCode, targetClassCode);
            if (created != 1) {
                throw new RuntimeException(ERROR_CLASS_RENAME_FAILED);
            }

            classDAO.reassignClassIdInStudents(sourceClassCode, targetClassCode);
            classDAO.reassignClassIdInTeachingAssignments(sourceClassCode, targetClassCode);
            classDAO.reassignOldClassIdInStudentHistory(sourceClassCode, targetClassCode);
            classDAO.reassignNewClassIdInStudentHistory(sourceClassCode, targetClassCode);

            int deleted = classDAO.deleteByClassIdIgnoreCase(sourceClassCode);
            if (deleted != 1) {
                throw new RuntimeException(ERROR_CLASS_RENAME_FAILED);
            }

            int assigned = teacherDAO.assignHomeroomTeacherToClass(targetClassCode, homeroomTeacherId);
            if (assigned != 1) {
                throw new RuntimeException(ERROR_CLASS_RENAME_FAILED);
            }
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException(ERROR_CLASS_RENAME_BLOCKED);
        }
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