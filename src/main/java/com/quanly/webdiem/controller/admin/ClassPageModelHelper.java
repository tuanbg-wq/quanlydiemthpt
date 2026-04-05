package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.form.ClassCreateForm;
import com.quanly.webdiem.model.form.CourseCreateForm;
import com.quanly.webdiem.model.service.admin.ClassManagementService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

@Component
public class ClassPageModelHelper {

    public static final String PAGE_TITLE_CLASS = "Quản lý lớp học";
    public static final String PAGE_TITLE_CLASS_CREATE = "Thêm lớp học mới";
    public static final String PAGE_TITLE_CLASS_EDIT = "Chỉnh sửa lớp học";
    public static final String PAGE_TITLE_COURSE_CREATE = "Thêm khóa học mới";
    public static final String PAGE_TITLE_COURSE_EDIT = "Chỉnh sửa khóa học";
    public static final String PAGE_ERROR_MESSAGE = "Không thể tải danh sách lớp học.";
    public static final String FLASH_CREATE_SUCCESS = "Tạo lớp học thành công.";
    public static final String FLASH_COURSE_CREATE_SUCCESS = "Tạo khóa học thành công.";
    public static final String FLASH_COURSE_UPDATE_SUCCESS = "Cập nhật khóa học thành công.";
    public static final String FLASH_COURSE_DELETE_SUCCESS = "Xóa khóa học thành công.";
    public static final String FLASH_UPDATE_SUCCESS = "Cập nhật lớp học thành công.";
    public static final String FLASH_DELETE_SUCCESS = "Xóa lớp học thành công.";
    public static final String FLASH_CLASS_NOT_FOUND = "Không tìm thấy lớp học.";
    public static final String FLASH_COURSE_NOT_FOUND = "Không tìm thấy khóa học.";

    private final ClassManagementService classManagementService;

    public ClassPageModelHelper(ClassManagementService classManagementService) {
        this.classManagementService = classManagementService;
    }

    public void applyListPage(Model model,
                              ClassManagementService.ClassPageResult pageResult,
                              ClassManagementService.ClassStats stats,
                              List<String> grades,
                              List<ClassManagementService.CourseOption> courses) {
        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", PAGE_TITLE_CLASS);
        model.addAttribute("classes", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("stats", stats);
        model.addAttribute("grades", grades);
        model.addAttribute("courses", courses);
    }

    public void applyCreatePage(Model model) {
        if (!model.containsAttribute("classForm")) {
            model.addAttribute("classForm", new ClassCreateForm());
        }
        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", PAGE_TITLE_CLASS_CREATE);
        model.addAttribute("courseOptions", classManagementService.getCoursesForCreate());
        model.addAttribute("gradeOptions", List.of(10, 11, 12));
    }

    public void applyEditPage(Model model, String classId) {
        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", PAGE_TITLE_CLASS_EDIT);
        model.addAttribute("classId", classId);
        model.addAttribute("courseOptions", classManagementService.getCoursesForCreate());
        model.addAttribute("gradeOptions", List.of(10, 11, 12));
    }

    public void applyCourseCreatePage(Model model) {
        if (!model.containsAttribute("courseForm")) {
            CourseCreateForm courseForm = new CourseCreateForm();
            LocalDate today = LocalDate.now();
            courseForm.setNgayBatDau(today);
            courseForm.setNgayKetThuc(today.plusYears(3));
            courseForm.setTrangThai("dang_hoc");
            model.addAttribute("courseForm", courseForm);
        }
        applyCoursePage(model, PAGE_TITLE_COURSE_CREATE);
    }

    public void applyCourseEditPage(Model model, String courseId) {
        applyCoursePage(model, PAGE_TITLE_COURSE_EDIT);
        model.addAttribute("courseId", courseId);
    }

    private void applyCoursePage(Model model, String pageTitle) {
        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("courseStatusOptions", List.of(
                new StatusOption("dang_hoc", "Đang học"),
                new StatusOption("da_tot_nghiep", "Đã tốt nghiệp")
        ));
    }

    public static final class StatusOption {
        private final String value;
        private final String label;

        public StatusOption(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}
