package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.TeacherRoleDAO;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.service.FileStorageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class TeacherDeleteService {

    private final TeacherDAO teacherDAO;
    private final TeacherRoleDAO teacherRoleDAO;
    private final FileStorageService fileStorageService;

    public TeacherDeleteService(TeacherDAO teacherDAO,
                                TeacherRoleDAO teacherRoleDAO,
                                FileStorageService fileStorageService) {
        this.teacherDAO = teacherDAO;
        this.teacherRoleDAO = teacherRoleDAO;
        this.fileStorageService = fileStorageService;
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
            deleteAvatarQuietly(teacher.getAnh());
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể xóa giáo viên vì còn dữ liệu liên quan.");
        }
    }

    private void deleteAvatarQuietly(String storedPath) {
        try {
            fileStorageService.deleteStoredFile(storedPath);
        } catch (RuntimeException ignored) {
            // Best effort cleanup after DB delete.
        }
    }

    private String normalizeTeacherId(String teacherId) {
        if (teacherId == null || teacherId.trim().isEmpty()) {
            throw new RuntimeException("Mã giáo viên không hợp lệ.");
        }
        return teacherId.trim().toUpperCase(Locale.ROOT);
    }
}
