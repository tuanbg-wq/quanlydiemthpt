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
        return resolveByUsername(username, null);
    }

    @Transactional(readOnly = true)
    public TeacherHomeroomScope resolveByUsername(String username, String preferredSchoolYear) {
        TeacherIdentity teacherIdentity = resolveTeacherIdentity(username);
        if (teacherIdentity == null) {
            return TeacherHomeroomScope.empty();
        }

        if (teacherIdentity.teacherId() == null) {
            return TeacherHomeroomScope.of(
                    null,
                    teacherIdentity.teacherName(),
                    null,
                    null,
                    null
            );
        }

        String teacherId = teacherIdentity.teacherId();
        String teacherName = teacherIdentity.teacherName();
        String normalizedPreferredYear = safeTrim(preferredSchoolYear);
        String latestSchoolYear = safeTrim(teacherDAO.findLatestHomeroomSchoolYearByTeacher(teacherId));

        String classId = null;
        if (normalizedPreferredYear != null) {
            classId = safeTrim(teacherDAO.findHomeroomClassIdByTeacherAndYear(teacherId, normalizedPreferredYear));
        }
        if (classId == null && latestSchoolYear != null) {
            classId = safeTrim(teacherDAO.findHomeroomClassIdByTeacherAndYear(teacherId, latestSchoolYear));
        }
        if (classId == null) {
            classId = safeTrim(teacherDAO.findHomeroomClassIdByTeacherAndYear(teacherId, null));
        }

        if (classId == null) {
            return TeacherHomeroomScope.of(
                    teacherId,
                    teacherName,
                    null,
                    null,
                    firstNonBlank(normalizedPreferredYear, latestSchoolYear)
            );
        }

        ClassEntity classEntity = classDAO.findById(classId).orElse(null);
        String className = classEntity == null ? classId : firstNonBlank(classEntity.getMaVaTenLop(), classId);
        String schoolYear = classEntity == null
                ? firstNonBlank(normalizedPreferredYear, latestSchoolYear)
                : firstNonBlank(classEntity.getNamHoc(), firstNonBlank(normalizedPreferredYear, latestSchoolYear));

        return TeacherHomeroomScope.of(teacherId, teacherName, classId, className, schoolYear);
    }

    @Transactional(readOnly = true)
    public List<String> findHomeroomSchoolYearsByUsername(String username) {
        TeacherIdentity teacherIdentity = resolveTeacherIdentity(username);
        if (teacherIdentity == null || teacherIdentity.teacherId() == null) {
            return List.of();
        }

        List<String> years = teacherDAO.findHomeroomSchoolYearsByTeacher(teacherIdentity.teacherId()).stream()
                .map(this::safeTrim)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();

        if (!years.isEmpty()) {
            return years;
        }

        String latestSchoolYear = safeTrim(teacherDAO.findLatestHomeroomSchoolYearByTeacher(teacherIdentity.teacherId()));
        return latestSchoolYear == null ? List.of() : List.of(latestSchoolYear);
    }

    private TeacherIdentity resolveTeacherIdentity(String username) {
        String resolvedUsername = safeTrim(username);
        if (resolvedUsername == null) {
            return null;
        }

        User user = userDAO.findByTenDangNhap(resolvedUsername).orElse(null);
        if (user == null || user.getIdTaiKhoan() == null) {
            return null;
        }

        List<Teacher> teachers = teacherDAO.findByIdTaiKhoan(user.getIdTaiKhoan());
        if (teachers.isEmpty()) {
            return new TeacherIdentity(null, resolvedUsername);
        }

        Teacher teacher = teachers.get(0);
        String teacherId = safeTrim(teacher.getIdGiaoVien());
        String teacherName = firstNonBlank(teacher.getHoTen(), resolvedUsername);
        return new TeacherIdentity(teacherId, teacherName);
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

    private record TeacherIdentity(String teacherId, String teacherName) {
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
