package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.form.ClassCreateForm;
import com.quanly.webdiem.model.form.CourseCreateForm;
import com.quanly.webdiem.model.search.ClassSearch;
import com.quanly.webdiem.model.service.admin.ClassManagementService;
import com.quanly.webdiem.model.service.admin.ClassManagementService.ClassPageResult;
import com.quanly.webdiem.model.service.admin.ClassManagementService.ClassStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/class")
public class ClassListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassListController.class);
    private static final String PAGE_TITLE_CLASS = "Quản lý lớp học";
    private static final String PAGE_TITLE_CLASS_CREATE = "Thêm lớp học mới";
    private static final String PAGE_TITLE_CLASS_EDIT = "Chỉnh sửa lớp học";
    private static final String PAGE_TITLE_COURSE_CREATE = "Thêm khóa học mới";
    private static final String PAGE_TITLE_COURSE_EDIT = "Chỉnh sửa khóa học";
    private static final String PAGE_ERROR_MESSAGE = "Không thể tải danh sách lớp học.";
    private static final String FLASH_CREATE_SUCCESS = "Tạo lớp học thành công.";
    private static final String FLASH_COURSE_CREATE_SUCCESS = "Tạo khóa học thành công.";
    private static final String FLASH_COURSE_UPDATE_SUCCESS = "Cập nhật khóa học thành công.";
    private static final String FLASH_COURSE_DELETE_SUCCESS = "Xóa khóa học thành công.";
    private static final String FLASH_UPDATE_SUCCESS = "Cập nhật lớp học thành công.";
    private static final String FLASH_DELETE_SUCCESS = "Xóa lớp học thành công.";
    private static final String FLASH_CLASS_NOT_FOUND = "Không tìm thấy lớp học.";
    private static final String FLASH_COURSE_NOT_FOUND = "Không tìm thấy khóa học.";

    private final ClassManagementService classManagementService;

    public ClassListController(ClassManagementService classManagementService) {
        this.classManagementService = classManagementService;
    }

    @GetMapping
    public String classPage(@ModelAttribute("search") ClassSearch search,
                            Model model) {
        ClassPageResult pageResult;
        ClassStats stats;
        List<String> grades;
        List<ClassManagementService.CourseOption> courses;

        try {
            pageResult = classManagementService.search(search);
            stats = classManagementService.getStats();
            grades = classManagementService.getGrades();
            courses = classManagementService.getCourses();
        } catch (Exception ex) {
            LOGGER.error("Lỗi tải trang danh sách lớp học", ex);
            pageResult = new ClassPageResult(List.of(), 1, 1, 0, 0, 0);
            stats = new ClassStats(0, 0, 0);
            grades = List.of();
            courses = List.of();
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", PAGE_ERROR_MESSAGE);
        }

        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", PAGE_TITLE_CLASS);
        model.addAttribute("classes", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("stats", stats);
        model.addAttribute("grades", grades);
        model.addAttribute("courses", courses);
        return "admin/class";
    }

    @GetMapping("/create")
    public String createClassPage(Model model) {
        if (!model.containsAttribute("classForm")) {
            model.addAttribute("classForm", new ClassCreateForm());
        }
        applyCreatePageModel(model);
        return "admin/class-create";
    }

    @PostMapping("/create")
    public String createClass(@ModelAttribute("classForm") ClassCreateForm classForm,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            classManagementService.createClass(classForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_CREATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            applyCreatePageModel(model);
            return "admin/class-create";
        }
    }

    @GetMapping("/course/create")
    public String createCoursePage(Model model) {
        if (!model.containsAttribute("courseForm")) {
            CourseCreateForm courseForm = new CourseCreateForm();
            LocalDate today = LocalDate.now();
            courseForm.setNgayBatDau(today);
            courseForm.setNgayKetThuc(today.plusYears(3));
            courseForm.setTrangThai("dang_hoc");
            model.addAttribute("courseForm", courseForm);
        }
        applyCoursePageModel(model, PAGE_TITLE_COURSE_CREATE);
        return "admin/class-course-create";
    }

    @PostMapping("/course/create")
    public String createCourse(@ModelAttribute("courseForm") CourseCreateForm courseForm,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            classManagementService.createCourse(courseForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_COURSE_CREATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            applyCoursePageModel(model, PAGE_TITLE_COURSE_CREATE);
            return "admin/class-course-create";
        }
    }

    @GetMapping("/course/{courseId}/edit")
    public String editCoursePage(@PathVariable("courseId") String courseId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("courseForm")) {
                model.addAttribute("courseForm", classManagementService.getCourseFormForEdit(courseId));
            }
            applyCourseEditPageModel(model, courseId);
            return "admin/class-course-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_COURSE_NOT_FOUND);
            return "redirect:/admin/class";
        }
    }

    @PostMapping("/course/{courseId}/edit")
    public String editCourse(@PathVariable("courseId") String courseId,
                             @ModelAttribute("courseForm") CourseCreateForm courseForm,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            classManagementService.updateCourse(courseId, courseForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_COURSE_UPDATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            if (!classManagementService.courseExists(courseId)) {
                redirectAttributes.addFlashAttribute("flashType", "error");
                redirectAttributes.addFlashAttribute("flashMessage", FLASH_COURSE_NOT_FOUND);
                return "redirect:/admin/class";
            }
            model.addAttribute("error", ex.getMessage());
            applyCourseEditPageModel(model, courseId);
            return "admin/class-course-edit";
        }
    }

    @PostMapping("/course/{courseId}/delete")
    public String deleteCourse(@PathVariable("courseId") String courseId,
                               RedirectAttributes redirectAttributes) {
        try {
            classManagementService.deleteCourse(courseId);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_COURSE_DELETE_SUCCESS);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/class";
    }

    @GetMapping("/suggest/homeroom-teachers")
    @ResponseBody
    public List<ClassManagementService.SuggestionItem> suggestHomeroomTeachers(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "classId", required = false) String classId
    ) {
        return classManagementService.suggestHomeroomTeachers(query, classId);
    }

    @GetMapping("/suggest/class-codes")
    @ResponseBody
    public List<ClassManagementService.SuggestionItem> suggestClassCodes(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "courseId", required = false) String courseId,
            @RequestParam(name = "grade", required = false) String grade,
            @RequestParam(name = "excludeClassId", required = false) String excludeClassId
    ) {
        return classManagementService.suggestClassCodes(query, courseId, grade, excludeClassId);
    }

    @GetMapping("/{classId}/edit")
    public String editClassPage(@PathVariable("classId") String classId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("classForm")) {
                model.addAttribute("classForm", classManagementService.getClassFormForEdit(classId));
            }
            applyEditPageModel(model, classId);
            return "admin/class-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_CLASS_NOT_FOUND);
            return "redirect:/admin/class";
        }
    }

    @PostMapping("/{classId}/edit")
    public String editClass(@PathVariable("classId") String classId,
                            @ModelAttribute("classForm") ClassCreateForm classForm,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            classManagementService.updateClass(classId, classForm);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_UPDATE_SUCCESS);
            return "redirect:/admin/class";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            applyEditPageModel(model, classId);
            return "admin/class-edit";
        }
    }

    @PostMapping("/{classId}/delete")
    public String deleteClass(@PathVariable("classId") String classId,
                              RedirectAttributes redirectAttributes) {
        try {
            classManagementService.deleteClass(classId);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", FLASH_DELETE_SUCCESS);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/class";
    }

    private void applyCreatePageModel(Model model) {
        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", PAGE_TITLE_CLASS_CREATE);
        model.addAttribute("courseOptions", classManagementService.getCoursesForCreate());
        model.addAttribute("gradeOptions", List.of(10, 11, 12));
    }

    private void applyEditPageModel(Model model, String classId) {
        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", PAGE_TITLE_CLASS_EDIT);
        model.addAttribute("classId", classId);
        model.addAttribute("courseOptions", classManagementService.getCoursesForCreate());
        model.addAttribute("gradeOptions", List.of(10, 11, 12));
    }

    private void applyCoursePageModel(Model model, String pageTitle) {
        model.addAttribute("activePage", "class");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("courseStatusOptions", List.of(
                new StatusOption("dang_hoc", "Đang học"),
                new StatusOption("da_tot_nghiep", "Đã tốt nghiệp")
        ));
    }

    private void applyCourseEditPageModel(Model model, String courseId) {
        applyCoursePageModel(model, PAGE_TITLE_COURSE_EDIT);
        model.addAttribute("courseId", courseId);
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
