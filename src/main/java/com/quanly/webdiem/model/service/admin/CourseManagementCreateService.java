package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.entity.CourseCreateForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

@Service
public class CourseManagementCreateService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("dang_hoc", "da_tot_nghiep");

    private final CourseDAO courseDAO;

    public CourseManagementCreateService(CourseDAO courseDAO) {
        this.courseDAO = courseDAO;
    }

    public void createCourse(CourseCreateForm form) {
        String courseId = normalizeUpper(form == null ? null : form.getIdKhoa());
        String courseName = normalize(form == null ? null : form.getTenKhoa());
        LocalDate startDate = form == null ? null : form.getNgayBatDau();
        LocalDate endDate = form == null ? null : form.getNgayKetThuc();
        String status = normalize(form == null ? null : form.getTrangThai());

        if (courseId == null) {
            throw new RuntimeException("Mã khóa học là bắt buộc.");
        }
        if (courseId.length() > 10) {
            throw new RuntimeException("Mã khóa học không được vượt quá 10 ký tự.");
        }
        if (courseDAO.existsById(courseId)) {
            throw new RuntimeException("Mã khóa học đã tồn tại.");
        }

        if (courseName == null) {
            throw new RuntimeException("Tên khóa học là bắt buộc.");
        }
        if (courseName.length() > 100) {
            throw new RuntimeException("Tên khóa học không được vượt quá 100 ký tự.");
        }

        if (startDate == null) {
            throw new RuntimeException("Ngày bắt đầu là bắt buộc.");
        }
        if (endDate == null) {
            throw new RuntimeException("Ngày kết thúc là bắt buộc.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn ngày bắt đầu.");
        }

        String normalizedStatus = status == null ? "dang_hoc" : status.toLowerCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new RuntimeException("Trạng thái khóa học không hợp lệ.");
        }

        Course course = new Course();
        course.setIdKhoa(courseId);
        course.setTenKhoa(courseName);
        course.setNgayBatDau(startDate);
        course.setNgayKetThuc(endDate);
        course.setTrangThai(normalizedStatus);

        try {
            courseDAO.save(course);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể tạo khóa học. Vui lòng kiểm tra lại dữ liệu.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeUpper(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }
}
