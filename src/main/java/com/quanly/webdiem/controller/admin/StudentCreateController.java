package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/student")
public class StudentCreateController {

    private final StudentService studentService;
    private final StudentPageModelHelper pageModelHelper;

    public StudentCreateController(StudentService studentService,
                                   StudentPageModelHelper pageModelHelper) {
        this.studentService = studentService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        applyCreatePage(model);
        return "admin/student-create";
    }

    @GetMapping("/suggest/student-id")
    @ResponseBody
    public Map<String, String> suggestStudentId() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("suggestedStudentId", studentService.suggestNextStudentId());
        return response;
    }

    @PostMapping("/create")
    public String createStudent(@ModelAttribute Student student,
                                @RequestParam("courseId") String courseId,
                                @RequestParam(value = "tenKhoa", required = false) String tenKhoa,
                                @RequestParam("idLop") String idLop,
                                @RequestParam("khoi") Integer khoi,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                Authentication authentication,
                                HttpServletRequest request,
                                Model model) {
        try {
            studentService.createWithAutoCourseClass(
                    student,
                    courseId,
                    tenKhoa,
                    idLop,
                    khoi,
                    avatar,
                    authentication != null ? authentication.getName() : null,
                    request != null ? request.getRemoteAddr() : null
            );

            return "redirect:/admin/student?created=true";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("student", student);
            applyCreatePage(model);
            return "admin/student-create";
        }
    }

    private void applyCreatePage(Model model) {
        pageModelHelper.applyCreatePage(model);
        if (!model.containsAttribute("suggestedStudentId")) {
            try {
                model.addAttribute("suggestedStudentId", studentService.suggestNextStudentId());
            } catch (RuntimeException ex) {
                model.addAttribute("suggestedStudentId", "HS001");
            }
        }
    }
}
