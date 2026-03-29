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

    private static final String ERROR_CLASS_NOT_FOUND = "Không tìm thấy lớp học.";
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
    private static final String ERROR_CLASS_ALREADY_EXISTS = "Mã lớp đã tồn tại.";
    private static final String ERROR_CLASS_RENAME_BLOCKED = "Không thể đổi mã lớp do có dữ liệu liên quan.";
    private static final String ERROR_CLASS_RENAME_FAILED = "Không thể cập nhật mã lớp học.";
    private static final String ERROR_COURSE_NOT_FOUND = "Khóa học không tồn tại.";
    private static final String ERROR_NOTE_TOO_LONG = "Ghi chú không được vượt quá 1000 ký tự.";
    private static final String ERROR_UPDATE_FAILED = "Không thể cập nhật lớp học. Vui lòng kiểm tra lại dữ liệu.";

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