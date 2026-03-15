package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.StudentService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/admin/student")
public class StudentEditController {

    private final StudentDAO studentDAO;
    private final StudentService studentService;
    private final StudentPageModelHelper pageModelHelper;

    public StudentEditController(StudentDAO studentDAO,
                                 StudentService studentService,
                                 StudentPageModelHelper pageModelHelper) {
        this.studentDAO = studentDAO;
        this.studentService = studentService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/{id}/edit")
    public String showEdit(@PathVariable String id, Model model) {
        Student student = findStudentOrThrow(id);
        pageModelHelper.applyEditPage(model, student, "C\u1eadp nh\u1eadt th\u00f4ng tin h\u1ecdc sinh");
        return "admin/student-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateStudent(@PathVariable String id,
                                @ModelAttribute Student formStudent,
                                @RequestParam("courseId") String courseId,
                                @RequestParam(value = "tenKhoa", required = false) String tenKhoa,
                                @RequestParam("khoi") Integer khoi,
                                @RequestParam("currentClassId") String currentClassId,
                                @RequestParam(value = "transferClassId", required = false) String transferClassId,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                Authentication authentication,
                                HttpServletRequest request,
                                Model model) {
        try {
            studentService.updateStudent(
                    id,
                    formStudent,
                    courseId,
                    tenKhoa,
                    khoi,
                    currentClassId,
                    transferClassId,
                    avatar,
                    authentication != null ? authentication.getName() : null,
                    request != null ? request.getRemoteAddr() : null
            );

            return "redirect:/admin/student?updated=true";

        } catch (RuntimeException ex) {
            Student student = findStudentOrThrow(id);
            model.addAttribute("error", ex.getMessage());
            pageModelHelper.applyEditPage(model, student, "C\u1eadp nh\u1eadt th\u00f4ng tin h\u1ecdc sinh");
            return "admin/student-edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteStudent(@PathVariable String id,
                                @RequestParam(value = "classId", required = false) String classId,
                                RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            if (classId != null && !classId.isBlank()) {
                redirectAttributes.addFlashAttribute("flashType", "success");
                redirectAttributes.addFlashAttribute("flashMessage", "X\u00f3a h\u1ecdc sinh th\u00e0nh c\u00f4ng.");
                return "redirect:/admin/class/" + classId.trim().toUpperCase() + "/info";
            }
            return "redirect:/admin/student?deleted=true";
        } catch (RuntimeException ex) {
            if (classId != null && !classId.isBlank()) {
                redirectAttributes.addFlashAttribute("flashType", "error");
                redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
                return "redirect:/admin/class/" + classId.trim().toUpperCase() + "/info";
            }
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/student";
        }
    }

    private Student findStudentOrThrow(String id) {
        return studentDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Kh\u00f4ng t\u00ecm th\u1ea5y h\u1ecdc sinh"));
    }
}
