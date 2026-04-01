package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.admin.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/student")
public class StudentListController {

    private final StudentService studentService;
    private final StudentPageModelHelper pageModelHelper;

    public StudentListController(StudentService studentService,
                                 StudentPageModelHelper pageModelHelper) {
        this.studentService = studentService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping
    public String studentPage(@ModelAttribute("search") StudentSearch search,
                              Model model) {

        pageModelHelper.applyBasePage(model, "Quản Lý Học Sinh");

        // Danh sách học sinh theo điều kiện tìm kiếm
        model.addAttribute("students", studentService.search(search));

        // Dữ liệu filter
        pageModelHelper.applyListFilters(model);

        // Nếu chọn lọc lịch sử chuyển thì hiển thêm cột
        boolean showHistoryColumn =
                search.getHistoryType() != null && !search.getHistoryType().isBlank();

        model.addAttribute("showHistoryColumn", showHistoryColumn);

        return "admin/student";
    }
}

