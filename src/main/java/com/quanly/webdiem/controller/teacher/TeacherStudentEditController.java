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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/student")
@PreAuthorize("hasAnyAuthority('ROLE_GVCN','ROLE_Admin')")
public class TeacherStudentEditController {

    private final StudentService studentService;
    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherStudentEditController(StudentService studentService,
                                        TeacherStudentScopeService scopeService,
                                        TeacherPageModelHelper pageModelHelper) {
        this.studentService = studentService;
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/{id}/edit")
    public String showEdit(@PathVariable String id,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            return "redirect:/teacher/student";
        }

        Student student;
        try {
            student = scopeService.getStudentInScopeOrThrow(id, scope);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/teacher/student";
        }

        studentService.populateConductForStudent(student);
        pageModelHelper.applyStudentPage(model, "Cập nhật thông tin học sinh", scope);
        model.addAttribute("student", student);
        model.addAttribute("classes", scopeService.getTransferClassOptions());
        return "teacher/student-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateStudent(@PathVariable String id,
                                @ModelAttribute Student formStudent,
                                @RequestParam(value = "transferClassId", required = false) String transferClassId,
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

        Student currentStudent;
        try {
            currentStudent = scopeService.getStudentInScopeOrThrow(id, scope);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/teacher/student";
        }

        ClassEntity currentClass = currentStudent.getLop();
        if (currentClass == null || currentClass.getKhoaHoc() == null || currentClass.getKhoi() == null) {
            model.addAttribute("error", "Không xác định được lớp hiện tại của học sinh.");
            studentService.populateConductForStudent(currentStudent);
            pageModelHelper.applyStudentPage(model, "Cập nhật thông tin học sinh", scope);
            model.addAttribute("student", currentStudent);
            model.addAttribute("classes", scopeService.getTransferClassOptions());
            return "teacher/student-edit";
        }

        try {
            studentService.updateStudent(
                    id,
                    formStudent,
                    currentClass.getKhoaHoc().getIdKhoa(),
                    currentClass.getKhoaHoc().getTenKhoa(),
                    currentClass.getKhoi(),
                    currentClass.getIdLop(),
                    transferClassId,
                    avatar,
                    pageModelHelper.resolveUsername(authentication),
                    pageModelHelper.resolveIpAddress(request)
            );
            return "redirect:/teacher/student?updated=true";
        } catch (RuntimeException ex) {
            Student student = scopeService.getStudentOrThrow(id);
            studentService.populateConductForStudent(student);
            model.addAttribute("error", ex.getMessage());
            pageModelHelper.applyStudentPage(model, "Cập nhật thông tin học sinh", scope);
            model.addAttribute("student", student);
            model.addAttribute("classes", scopeService.getTransferClassOptions());
            return "teacher/student-edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteStudent(@PathVariable String id,
                                Authentication authentication,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            return "redirect:/teacher/student";
        }

        try {
            scopeService.getStudentInScopeOrThrow(id, scope);
            studentService.deleteStudent(
                    id,
                    pageModelHelper.resolveUsername(authentication),
                    pageModelHelper.resolveIpAddress(request)
            );
            return "redirect:/teacher/student?deleted=true";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/teacher/student";
        }
    }
}
