package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherHomeroomScopeService {

    private final UserDAO userDAO;
    private final TeacherDAO teacherDAO;
    private final ClassDAO classDAO;

    public TeacherHomeroomScopeService(UserDAO userDAO,
                                       TeacherDAO teacherDAO,
                                       ClassDAO classDAO) {
        this.userDAO = userDAO;
        this.teacherDAO = teacherDAO;
        this.classDAO = classDAO;
    }

    @Transactional(readOnly = true)
    public TeacherHomeroomScope resolveByUsername(String username) {
        String resolvedUsername = safeTrim(username);
        if (resolvedUsername == null) {
            return TeacherHomeroomScope.empty();
        }

        User user = userDAO.findByTenDangNhap(resolvedUsername).orElse(null);
        if (user == null || user.getIdTaiKhoan() == null) {
            return TeacherHomeroomScope.empty();
        }

        List<Teacher> teachers = teacherDAO.findByIdTaiKhoan(user.getIdTaiKhoan());
        if (teachers.isEmpty()) {
            return TeacherHomeroomScope.of(
                    null,
                    resolvedUsername,
                    null,
                    null,
                    null
            );
        }

        Teacher teacher = teachers.get(0);
        String teacherId = safeTrim(teacher.getIdGiaoVien());
        String teacherName = firstNonBlank(teacher.getHoTen(), resolvedUsername);

        if (teacherId == null) {
            return TeacherHomeroomScope.of(
                    null,
                    teacherName,
                    null,
                    null,
                    null
            );
        }

        String latestSchoolYear = safeTrim(teacherDAO.findLatestHomeroomSchoolYearByTeacher(teacherId));
        String classId = safeTrim(teacherDAO.findHomeroomClassIdByTeacherAndYear(teacherId, latestSchoolYear));
        if (classId == null) {
            classId = safeTrim(teacherDAO.findHomeroomClassIdByTeacherAndYear(teacherId, null));
        }

        if (classId == null) {
            return TeacherHomeroomScope.of(
                    teacherId,
                    teacherName,
                    null,
                    null,
                    latestSchoolYear
            );
        }

        ClassEntity classEntity = classDAO.findById(classId).orElse(null);
        String className = classEntity == null ? classId : firstNonBlank(classEntity.getMaVaTenLop(), classId);
        String schoolYear = classEntity == null
                ? latestSchoolYear
                : firstNonBlank(classEntity.getNamHoc(), latestSchoolYear);

        return TeacherHomeroomScope.of(
                teacherId,
                teacherName,
                classId,
                className,
                schoolYear
        );
    }

    private String firstNonBlank(String value, String fallback) {
        String trimmed = safeTrim(value);
        if (trimmed != null) {
            return trimmed;
        }
        return fallback;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static class TeacherHomeroomScope {
        private final String teacherId;
        private final String teacherName;
        private final String classId;
        private final String className;
        private final String schoolYear;

        private TeacherHomeroomScope(String teacherId,
                                     String teacherName,
                                     String classId,
                                     String className,
                                     String schoolYear) {
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.classId = classId;
            this.className = className;
            this.schoolYear = schoolYear;
        }

        public static TeacherHomeroomScope empty() {
            return new TeacherHomeroomScope(null, null, null, null, null);
        }

        public static TeacherHomeroomScope of(String teacherId,
                                              String teacherName,
                                              String classId,
                                              String className,
                                              String schoolYear) {
            return new TeacherHomeroomScope(teacherId, teacherName, classId, className, schoolYear);
        }

        public String getTeacherId() {
            return teacherId;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public String getClassId() {
            return classId;
        }

        public String getClassName() {
            return className;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public boolean hasHomeroomClass() {
            return classId != null && !classId.isBlank();
        }
    }
}
