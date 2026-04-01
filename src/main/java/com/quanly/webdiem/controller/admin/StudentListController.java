package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.ActivityLog;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.admin.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/student")
public class StudentListController {

    private final StudentService studentService;
    private final ActivityLogService activityLogService;
    private final StudentPageModelHelper pageModelHelper;

    public StudentListController(StudentService studentService,
                                 ActivityLogService activityLogService,
                                 StudentPageModelHelper pageModelHelper) {
        this.studentService = studentService;
        this.activityLogService = activityLogService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping
    public String studentPage(@ModelAttribute("search") StudentSearch search,
                              Model model) {
        pageModelHelper.applyBasePage(model, "Quản Lý Học Sinh");

        List<Student> students = studentService.search(search);
        model.addAttribute("students", students);

        pageModelHelper.applyListFilters(model);

        boolean showHistoryColumn =
                search.getHistoryType() != null && !search.getHistoryType().isBlank();
        model.addAttribute("showHistoryColumn", showHistoryColumn);

        List<String> studentIds = students.stream()
                .map(Student::getIdHocSinh)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        List<ActivityLog> studentHistoryLogs = activityLogService.getStudentLogsByStudentIds(studentIds, 5);
        model.addAttribute("studentHistoryLogs", studentHistoryLogs);

        Map<String, String> studentDisplayById = new LinkedHashMap<>();
        for (Student student : students) {
            if (student.getIdHocSinh() == null || student.getIdHocSinh().isBlank()) {
                continue;
            }
            String name = student.getHoTen() == null || student.getHoTen().isBlank()
                    ? "(không rõ)"
                    : student.getHoTen().trim();
            studentDisplayById.put(student.getIdHocSinh(), name + " (" + student.getIdHocSinh() + ")");
        }
        model.addAttribute("studentDisplayById", studentDisplayById);

        return "admin/student";
    }
}
