package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.admin.StudentService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/teacher/student")
@PreAuthorize("hasAnyAuthority('ROLE_GVCN','ROLE_Admin')")
public class TeacherStudentListController {

    private final StudentService studentService;
    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherStudentListController(StudentService studentService,
                                        TeacherStudentScopeService scopeService,
                                        TeacherPageModelHelper pageModelHelper) {
        this.studentService = studentService;
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping
    public String studentPage(@ModelAttribute("search") StudentSearch search,
                              Authentication authentication,
                              Model model) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        pageModelHelper.applyStudentPage(model, "Quản lý học sinh chủ nhiệm", scope);

        if (!scopeService.hasHomeroomClass(scope)) {
            model.addAttribute("students", List.of());
            model.addAttribute("showHistoryColumn", false);
            model.addAttribute("warningMessage", "Tài khoản chưa được phân công lớp chủ nhiệm.");
            return "teacher/student";
        }

        search.setCourseId(null);
        search.setKhoi(null);
        search.setClassId(scope.getClassId());

        model.addAttribute("students", studentService.search(search));
        model.addAttribute("showHistoryColumn", search.getHistoryType() != null && !search.getHistoryType().isBlank());
        return "teacher/student";
    }
}
