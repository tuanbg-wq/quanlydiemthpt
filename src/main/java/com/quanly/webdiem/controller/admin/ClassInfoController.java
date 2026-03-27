package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.ClassInfoService;
import com.quanly.webdiem.model.service.admin.ClassInfoService.ClassInfoView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/class")
public class ClassInfoController {

    private static final String PAGE_TITLE = "Chi Ti\u1ebft L\u1edbp H\u1ecdc";

    private final ClassInfoService classInfoService;

    public ClassInfoController(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    @GetMapping("/{classId}/info")
    public String classInfoPage(@PathVariable("classId") String classId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            ClassInfoView classInfo = classInfoService.getClassInfo(classId);
            model.addAttribute("activePage", "class");
            model.addAttribute("pageTitle", PAGE_TITLE);
            model.addAttribute("classInfo", classInfo);
            return "admin/class-info";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/class";
        }
    }
}