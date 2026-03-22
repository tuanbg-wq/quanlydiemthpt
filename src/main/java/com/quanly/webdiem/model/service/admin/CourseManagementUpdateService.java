package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.form.CourseCreateForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

@Service
public class CourseManagementUpdateService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("dang_hoc", "da_tot_nghiep");

    private final CourseDAO courseDAO;

    public CourseManagementUpdateService(CourseDAO courseDAO) {
        this.courseDAO = courseDAO;
    }

    public CourseCreateForm getCourseFormForEdit(String courseId) {
        Course course = findCourseOrThrow(courseId);

        CourseCreateForm form = new CourseCreateForm();
        form.setIdKhoa(course.getIdKhoa());
        form.setTenKhoa(course.getTenKhoa());
        form.setNgayBatDau(course.getNgayBatDau());
        form.setNgayKetThuc(course.getNgayKetThuc());
        form.setTrangThai(normalize(course.getTrangThai()));
        return form;
    }

    public void updateCourse(String courseId, CourseCreateForm form) {
        Course course = findCourseOrThrow(courseId);

        String normalizedCourseId = normalizeUpper(courseId);
        String formCourseId = normalizeUpper(form == null ? null : form.getIdKhoa());
        if (formCourseId == null) {
            throw new RuntimeException("Mã khóa học là bắt buộc.");
        }
        if (!normalizedCourseId.equals(formCourseId)) {
            throw new RuntimeException("Không thể thay đổi mã khóa học.");
        }

        String courseName = normalize(form == null ? null : form.getTenKhoa());
        LocalDate startDate = form == null ? null : form.getNgayBatDau();
        LocalDate endDate = form == null ? null : form.getNgayKetThuc();
        String status = normalize(form == null ? null : form.getTrangThai());

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

        course.setTenKhoa(courseName);
        course.setNgayBatDau(startDate);
        course.setNgayKetThuc(endDate);
        course.setTrangThai(normalizedStatus);

        try {
            courseDAO.save(course);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể cập nhật khóa học. Vui lòng kiểm tra lại dữ liệu.");
        }
    }

    public boolean existsCourse(String courseId) {
        String normalizedCourseId = normalizeUpper(courseId);
        return normalizedCourseId != null && courseDAO.existsById(normalizedCourseId);
    }

    private Course findCourseOrThrow(String courseId) {
        String normalizedCourseId = normalizeUpper(courseId);
        if (normalizedCourseId == null) {
            throw new RuntimeException("Không tìm thấy khóa học.");
        }
        return courseDAO.findById(normalizedCourseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học."));
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
