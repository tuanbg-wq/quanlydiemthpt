package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.entity.Course;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CourseManagementDeleteService {

    private final CourseDAO courseDAO;

    public CourseManagementDeleteService(CourseDAO courseDAO) {
        this.courseDAO = courseDAO;
    }

    public void deleteCourse(String courseId) {
        Course course = findCourseOrThrow(courseId);
        try {
            courseDAO.delete(course);
            courseDAO.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException(
                    "Không thể xóa khóa học này vì đang có dữ liệu lớp học hoặc môn học liên quan."
            );
        }
    }

    private Course findCourseOrThrow(String courseId) {
        String normalizedCourseId = normalizeUpper(courseId);
        if (normalizedCourseId == null) {
            throw new RuntimeException("Không tìm thấy khóa học.");
        }
        return courseDAO.findById(normalizedCourseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học."));
    }

    private String normalizeUpper(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}
