package com.quanly.webdiem.controller.teacher;

import com.quanly.webdiem.model.search.TeacherScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService;
import com.quanly.webdiem.model.service.teacher.TeacherScoreService.ScoreDashboardData;
import com.quanly.webdiem.model.service.teacher.TeacherStudentScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/score")
@PreAuthorize("hasAnyAuthority('ROLE_Giao_vien','ROLE_GVCN','ROLE_Admin')")
public class TeacherScoreController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherScoreController.class);
    private static final String PAGE_TITLE = "Quản lý điểm số lớp chủ nhiệm";
    private static final String PAGE_EDIT_TITLE = "Nhập/Sửa điểm môn học";

    private final TeacherStudentScopeService scopeService;
    private final TeacherPageModelHelper pageModelHelper;
    private final TeacherScoreService teacherScoreService;
    private final ScoreCreateService scoreCreateService;

    public TeacherScoreController(TeacherStudentScopeService scopeService,
                                  TeacherPageModelHelper pageModelHelper,
                                  TeacherScoreService teacherScoreService,
                                  ScoreCreateService scoreCreateService) {
        this.scopeService = scopeService;
        this.pageModelHelper = pageModelHelper;
        this.teacherScoreService = teacherScoreService;
        this.scoreCreateService = scoreCreateService;
    }

    @GetMapping
    public String scorePage(@ModelAttribute("search") TeacherScoreSearch search,
                            Authentication authentication,
                            Model model) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        pageModelHelper.applyBasePage(model, "score", PAGE_TITLE, scope);

        try {
            ScoreDashboardData dashboardData = teacherScoreService.loadDashboard(username, scope, search);
            model.addAttribute("scoreData", dashboardData);
            model.addAttribute("search", dashboardData.getSearch());

            if (dashboardData.getTeacherId() == null) {
                model.addAttribute("warningMessage", "Không xác định được giáo viên từ tài khoản đăng nhập.");
            } else if (dashboardData.getSchoolYear() == null || dashboardData.getSchoolYear().isBlank()) {
                model.addAttribute("warningMessage", "Không xác định được năm học hiện tại để hiển thị điểm.");
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Lỗi tải trang quản lý điểm cho GVCN", ex);
            model.addAttribute("flashType", "error");
            model.addAttribute("flashMessage", "Không thể tải dữ liệu điểm số của giáo viên chủ nhiệm.");
            model.addAttribute("scoreData", ScoreDashboardData.empty(search, scope == null ? null : scope.getSchoolYear()));
        }
        return "teacher/score";
    }

    @GetMapping("/edit")
    public String editPage(@RequestParam("studentId") String studentId,
                           @RequestParam("subjectId") String subjectId,
                           @RequestParam("namHoc") String namHoc,
                           @RequestParam("hocKy") String hocKy,
                           @RequestParam(value = "returnQ", required = false) String returnQ,
                           @RequestParam(value = "returnMon", required = false) String returnMon,
                           @RequestParam(value = "returnHocKy", required = false) String returnHocKy,
                           @RequestParam(value = "returnClassScope", required = false) String returnClassScope,
                           @RequestParam(value = "returnPage", required = false) String returnPage,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);
        pageModelHelper.applyBasePage(model, "score", PAGE_EDIT_TITLE, scope);

        try {
            teacherScoreService.assertCanEditScore(username, scope, studentId, subjectId, namHoc, hocKy);

            ScoreCreateService.ScoreCreateFilter filter = new ScoreCreateService.ScoreCreateFilter();
            filter.setStudentId(studentId);
            filter.setMon(subjectId);
            filter.setNamHoc(namHoc);
            filter.setHocKy(normalizeEditSemester(hocKy));
            filter.setApplyFilter("1");

            ScoreCreateService.ScoreCreatePageData editData = scoreCreateService.getCreatePageData(filter);
            if (editData == null || !editData.isReadyForInput()) {
                throw new RuntimeException("Không đủ dữ liệu để mở màn hình nhập/sửa điểm.");
            }

            model.addAttribute("editData", editData);
            model.addAttribute("returnQ", returnQ);
            model.addAttribute("returnMon", returnMon);
            model.addAttribute("returnHocKy", returnHocKy);
            model.addAttribute("returnClassScope", returnClassScope);
            model.addAttribute("returnPage", returnPage);
            return "teacher/score-edit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            appendReturnSearch(redirectAttributes, returnQ, returnMon, returnHocKy, returnClassScope, returnPage);
            return "redirect:/teacher/score";
        }
    }

    @PostMapping("/edit")
    public String editSubmit(@ModelAttribute ScoreCreateService.ScoreSaveRequest request,
                             @RequestParam(value = "returnQ", required = false) String returnQ,
                             @RequestParam(value = "returnMon", required = false) String returnMon,
                             @RequestParam(value = "returnHocKy", required = false) String returnHocKy,
                             @RequestParam(value = "returnClassScope", required = false) String returnClassScope,
                             @RequestParam(value = "returnPage", required = false) String returnPage,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String username = pageModelHelper.resolveUsername(authentication);
        TeacherHomeroomScope scope = scopeService.resolveScopeByUsername(username);

        String semester = normalizeEditSemester(request.getHocKy());
        request.setHocKy(semester);

        try {
            if ("0".equals(semester)) {
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), "1");
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), "2");
            } else {
                teacherScoreService.assertCanEditScore(username, scope, request.getStudentId(), request.getMon(), request.getNamHoc(), semester);
            }
            scoreCreateService.save(request);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Đã lưu điểm thành công.");
            appendReturnSearch(redirectAttributes, returnQ, returnMon, returnHocKy, returnClassScope, returnPage);
            return "redirect:/teacher/score";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            redirectAttributes.addAttribute("studentId", request.getStudentId());
            redirectAttributes.addAttribute("subjectId", request.getMon());
            redirectAttributes.addAttribute("namHoc", request.getNamHoc());
            redirectAttributes.addAttribute("hocKy", semester);
            addIfPresent(redirectAttributes, "returnQ", returnQ);
            addIfPresent(redirectAttributes, "returnMon", returnMon);
            addIfPresent(redirectAttributes, "returnHocKy", returnHocKy);
            addIfPresent(redirectAttributes, "returnClassScope", returnClassScope);
            addIfPresent(redirectAttributes, "returnPage", returnPage);
            return "redirect:/teacher/score/edit";
        }
    }

    private void appendReturnSearch(RedirectAttributes redirectAttributes,
                                    String returnQ,
                                    String returnMon,
                                    String returnHocKy,
                                    String returnClassScope,
                                    String returnPage) {
        addIfPresent(redirectAttributes, "q", returnQ);
        addIfPresent(redirectAttributes, "mon", returnMon);
        addIfPresent(redirectAttributes, "hocKy", returnHocKy);
        addIfPresent(redirectAttributes, "classScope", returnClassScope);
        addIfPresent(redirectAttributes, "page", returnPage);
    }

    private void addIfPresent(RedirectAttributes redirectAttributes, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        redirectAttributes.addAttribute(key, value.trim());
    }

    private String normalizeEditSemester(String hocKy) {
        if ("2".equals(hocKy) || "0".equals(hocKy)) {
            return hocKy;
        }
        return "1";
    }
}
