package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.TeacherRoleDAO;
import com.quanly.webdiem.model.entity.Teacher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class TeacherDeleteService {

    private final TeacherDAO teacherDAO;
    private final TeacherRoleDAO teacherRoleDAO;

    public TeacherDeleteService(TeacherDAO teacherDAO, TeacherRoleDAO teacherRoleDAO) {
        this.teacherDAO = teacherDAO;
        this.teacherRoleDAO = teacherRoleDAO;
    }

    @Transactional
    public void deleteTeacher(String teacherId) {
        String normalizedTeacherId = normalizeTeacherId(teacherId);
        Teacher teacher = teacherDAO.findById(normalizedTeacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên."));

        if (teacherDAO.countHomeroomClassReferences(normalizedTeacherId) > 0) {
            throw new RuntimeException("Không thể xóa giáo viên đang làm giáo viên chủ nhiệm.");
        }

        if (teacherDAO.countTeachingAssignmentReferences(normalizedTeacherId) > 0) {
            throw new RuntimeException("Không thể xóa giáo viên đã có phân công giảng dạy.");
        }

        teacherRoleDAO.deleteByTeacherId(normalizedTeacherId);

        try {
            teacherDAO.delete(teacher);
            teacherDAO.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể xóa giáo viên vì còn dữ liệu liên quan.");
        }
    }

    private String normalizeTeacherId(String teacherId) {
        if (teacherId == null || teacherId.trim().isEmpty()) {
            throw new RuntimeException("Mã giáo viên không hợp lệ.");
        }
        return teacherId.trim().toUpperCase(Locale.ROOT);
    }
}
