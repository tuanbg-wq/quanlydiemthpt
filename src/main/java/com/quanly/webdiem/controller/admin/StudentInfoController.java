package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.StudentClassHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/student")
public class StudentInfoController {

    private final StudentDAO studentDAO;
    private final StudentClassHistoryService historyService;
    private final ActivityLogService activityLogService;
    private final StudentPageModelHelper pageModelHelper;

    public StudentInfoController(StudentDAO studentDAO,
                                 StudentClassHistoryService historyService,
                                 ActivityLogService activityLogService,
                                 StudentPageModelHelper pageModelHelper) {
        this.studentDAO = studentDAO;
        this.historyService = historyService;
        this.activityLogService = activityLogService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping("/{id}/info")
    public String showInfo(@PathVariable String id, Model model) {
        Student student = studentDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh"));

        pageModelHelper.applyBasePage(model, "Thông tin học sinh");
        model.addAttribute("student", student);
        model.addAttribute("classHistories", historyService.getHistoryByStudent(id));
        model.addAttribute("editLogs", activityLogService.getStudentEditLogs(id));

        return "admin/student-info";
    }
}
