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
        pageModelHelper.applyEditPage(model, student, "Cập nhật thông tin học sinh");
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
            pageModelHelper.applyEditPage(model, student, "Cập nhật thông tin học sinh");
            return "admin/student-edit";
        }
    }

    private Student findStudentOrThrow(String id) {
        return studentDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh"));
    }
}
