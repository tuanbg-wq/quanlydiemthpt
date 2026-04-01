package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.StudentService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/student")
@PreAuthorize("hasAnyAuthority('ROLE_GVCN','ROLE_Admin')")
public class TeacherStudentCreateController {

    private final StudentService studentService;
    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherStudentCreateController(StudentService studentService,
                                          TeacherStudentScopeService scopeService,
                                          TeacherPageModelHelper pageModelHelper) {
        this.studentService = studentService;
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/create")
    public String showCreateForm(Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            return "redirect:/teacher/student";
        }

        pageModelHelper.applyStudentPage(model, "Thêm học sinh lớp chủ nhiệm", scope);
        model.addAttribute("homeroomClassName", scope.getClassName());
        model.addAttribute("homeroomSchoolYear", scope.getSchoolYear());
        if (!model.containsAttribute("student")) {
            model.addAttribute("student", new Student());
        }
        return "teacher/student-create";
    }

    @PostMapping("/create")
    public String createStudent(@ModelAttribute Student student,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                Authentication authentication,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            return "redirect:/teacher/student";
        }

        ClassEntity homeroomClass;
        try {
            homeroomClass = scopeService.getHomeroomClassOrThrow(scope);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("student", student);
            model.addAttribute("homeroomClassName", scope.getClassName());
            model.addAttribute("homeroomSchoolYear", scope.getSchoolYear());
            pageModelHelper.applyStudentPage(model, "Thêm học sinh lớp chủ nhiệm", scope);
            return "teacher/student-create";
        }

        if (homeroomClass.getKhoaHoc() == null || homeroomClass.getKhoi() == null) {
            model.addAttribute("error", "Không thể xác định thông tin khóa học hoặc khối của lớp chủ nhiệm.");
            model.addAttribute("student", student);
            model.addAttribute("homeroomClassName", scope.getClassName());
            model.addAttribute("homeroomSchoolYear", scope.getSchoolYear());
            pageModelHelper.applyStudentPage(model, "Thêm học sinh lớp chủ nhiệm", scope);
            return "teacher/student-create";
        }

        try {
            studentService.createWithAutoCourseClass(
                    student,
                    homeroomClass.getKhoaHoc().getIdKhoa(),
                    homeroomClass.getKhoaHoc().getTenKhoa(),
                    homeroomClass.getIdLop(),
                    homeroomClass.getKhoi(),
                    avatar,
                    pageModelHelper.resolveUsername(authentication),
                    pageModelHelper.resolveIpAddress(request)
            );
            return "redirect:/teacher/student?created=true";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("student", student);
            model.addAttribute("homeroomClassName", scope.getClassName());
            model.addAttribute("homeroomSchoolYear", scope.getSchoolYear());
            pageModelHelper.applyStudentPage(model, "Thêm học sinh lớp chủ nhiệm", scope);
            return "teacher/student-create";
        }
    }
}
