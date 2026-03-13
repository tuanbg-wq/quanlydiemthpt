package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.TeacherInfoService;
import com.quanly.webdiem.model.service.admin.TeacherInfoService.TeacherInfoView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/teacher")
public class TeacherInfoController {

    private static final String PAGE_TITLE = "Thông Tin Giáo Viên";

    private final TeacherInfoService teacherInfoService;

    public TeacherInfoController(TeacherInfoService teacherInfoService) {
        this.teacherInfoService = teacherInfoService;
    }

    @GetMapping("/{id}/info")
    public String showTeacherInfo(@PathVariable("id") String id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            TeacherInfoView teacherInfo = teacherInfoService.getTeacherInfo(id);
            model.addAttribute("activePage", "teacher");
            model.addAttribute("pageTitle", PAGE_TITLE);
            model.addAttribute("teacherInfo", teacherInfo);
            return "admin/teacher-info";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/teacher";
        }
    }
}
