package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.SubjectInfoService;
import com.quanly.webdiem.model.service.admin.SubjectInfoService.SubjectInfoView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/subject")
public class SubjectInfoController {

    private static final String PAGE_TITLE = "Thông Tin Môn Học";

    private final SubjectInfoService subjectInfoService;

    public SubjectInfoController(SubjectInfoService subjectInfoService) {
        this.subjectInfoService = subjectInfoService;
    }

    @GetMapping("/{id}/info")
    public String showSubjectInfo(@PathVariable("id") String id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            SubjectInfoView subjectInfo = subjectInfoService.getSubjectInfo(id);
            model.addAttribute("activePage", "subject");
            model.addAttribute("pageTitle", PAGE_TITLE);
            model.addAttribute("subjectInfo", subjectInfo);
            return "admin/subject-info";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/admin/subject";
        }
    }
}
