package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherStudentCreateService;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/teacher/student")
@PreAuthorize("hasAnyAuthority('ROLE_Giao_vien','ROLE_GVCN','ROLE_Admin')")
public class TeacherStudentCreateController {

    private static final String ERROR_NO_HOMEROOM_CLASS = "Tài khoản chưa được phân công lớp chủ nhiệm.";

    private final TeacherStudentCreateService teacherStudentCreateService;
    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;

    public TeacherStudentCreateController(TeacherStudentCreateService teacherStudentCreateService,
                                          TeacherStudentScopeService scopeService,
                                          TeacherPageModelHelper pageModelHelper) {
        this.teacherStudentCreateService = teacherStudentCreateService;
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam(value = "schoolYear", required = false) String schoolYear,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsernameAndSchoolYear(username, schoolYear);
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ERROR_NO_HOMEROOM_CLASS);
            return "redirect:/teacher/student";
        }

        pageModelHelper.applyStudentPage(model, "Thêm học sinh lớp chủ nhiệm", scope);
        model.addAttribute("selectedSchoolYear", scope.getSchoolYear());
        model.addAttribute("homeroomClassName", scope.getClassName());
        model.addAttribute("homeroomSchoolYear", scope.getSchoolYear());
        addSuggestedStudentId(model);
        if (!model.containsAttribute("student")) {
            model.addAttribute("student", new Student());
        }
        return "teacher/student-create";
    }

    @GetMapping("/suggest/next-student-id")
    @ResponseBody
    public ResponseEntity<Map<String, String>> suggestNextStudentId(Authentication authentication) {
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(pageModelHelper.resolveUsername(authentication));
        if (!scopeService.hasHomeroomClass(scope)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ERROR_NO_HOMEROOM_CLASS));
        }
        return ResponseEntity.ok(Map.of("suggestedStudentId", teacherStudentCreateService.suggestNextStudentId()));
    }

    @PostMapping("/create")
    public String createStudent(@ModelAttribute Student student,
                                @RequestParam(value = "schoolYear", required = false) String schoolYear,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                Authentication authentication,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsernameAndSchoolYear(username, schoolYear);
        if (!scopeService.hasHomeroomClass(scope)) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ERROR_NO_HOMEROOM_CLASS);
            return "redirect:/teacher/student";
        }

        try {
            teacherStudentCreateService.createStudentForHomeroom(
                    student,
                    scope,
                    avatar,
                    username,
                    pageModelHelper.resolveIpAddress(request)
            );
            return "redirect:/teacher/student?created=true";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("student", student);
            model.addAttribute("selectedSchoolYear", scope.getSchoolYear());
            model.addAttribute("homeroomClassName", scope.getClassName());
            model.addAttribute("homeroomSchoolYear", scope.getSchoolYear());
            addSuggestedStudentId(model);
            pageModelHelper.applyStudentPage(model, "Thêm học sinh lớp chủ nhiệm", scope);
            return "teacher/student-create";
        }
    }

    private void addSuggestedStudentId(Model model) {
        if (model == null || model.containsAttribute("suggestedStudentId")) {
            return;
        }
        try {
            model.addAttribute("suggestedStudentId", teacherStudentCreateService.suggestNextStudentId());
        } catch (RuntimeException ex) {
            model.addAttribute("suggestedStudentId", "HS001");
        }
    }
}
