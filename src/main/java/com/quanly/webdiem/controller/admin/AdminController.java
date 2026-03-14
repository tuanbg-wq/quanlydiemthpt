package com.quanly.webdiem.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"", "/"})
    public String root() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");

        // Demo data for dashboard cards.
        model.addAttribute("displayName", "Qu\u1ea3n tr\u1ecb");
        model.addAttribute("soHocSinh", 4);
        model.addAttribute("soMonHoc", 3);
        model.addAttribute("soLop", 4);

        return "admin/dashboard";
    }

    @GetMapping("/students")
    public String students(Model model) {
        model.addAttribute("activePage", "students");
        return "admin/students";
    }

    @GetMapping("/subjects")
    public String subjects(Model model) {
        model.addAttribute("activePage", "subjects");
        return "admin/subjects";
    }

    @GetMapping("/teachers")
    public String teachers(Model model) {
        model.addAttribute("activePage", "teachers");
        return "admin/teachers";
    }

    @GetMapping("/scores")
    public String scores(Model model) {
        model.addAttribute("activePage", "scores");
        return "admin/scores";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("activePage", "accounts");
        return "admin/accounts";
    }
}
