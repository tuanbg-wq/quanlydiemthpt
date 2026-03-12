package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.SubjectSearch;
import com.quanly.webdiem.model.service.admin.SubjectService;
import com.quanly.webdiem.model.service.admin.SubjectService.SubjectPageResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/subject")
public class SubjectListController {

    private static final String PAGE_TITLE_SUBJECT = "Qu\u1ea3n L\u00fd M\u00f4n H\u1ecdc";

    private final SubjectService subjectService;

    public SubjectListController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public String subjectPage(@ModelAttribute("search") SubjectSearch search,
                              Model model) {
        SubjectPageResult pageResult = subjectService.search(search);

        model.addAttribute("activePage", "subject");
        model.addAttribute("pageTitle", PAGE_TITLE_SUBJECT);
        model.addAttribute("subjects", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("grades", subjectService.getGrades());
        model.addAttribute("departments", subjectService.getDepartments());

        return "admin/subject";
    }
}
