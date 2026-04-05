package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.search.ScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ScorePageSupport {

    public static final String PAGE_TITLE_SCORE = "Quản lý điểm số";
    public static final String PAGE_TITLE_SCORE_CREATE = "Thêm điểm số";
    public static final String PAGE_TITLE_SCORE_DETAIL = "Chi tiết điểm";
    public static final String PAGE_TITLE_SCORE_EDIT = "Chỉnh sửa điểm";
    public static final String PAGE_ERROR_MESSAGE = "Không thể tải danh sách điểm số.";
    public static final String PAGE_CREATE_ERROR_MESSAGE = "Không thể tải dữ liệu trang thêm điểm.";
    public static final String FLASH_UPDATE_SUCCESS = "Cập nhật điểm thành công.";
    public static final String FLASH_DELETE_SUCCESS = "Xóa nhóm điểm thành công.";
    public static final String FLASH_CREATE_SUCCESS = "Đã lưu điểm thành công.";
    public static final String SEMESTER_ALL = "0";
    public static final String SEMESTER_1 = "1";
    public static final String SEMESTER_2 = "2";

    private static final DateTimeFormatter EXPORT_FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ScoreCreateService scoreCreateService;

    public ScorePageSupport(ScoreCreateService scoreCreateService) {
        this.scoreCreateService = scoreCreateService;
    }

    public void applyListPage(Model model,
                              ScoreManagementService.ScorePageResult pageResult,
                              ScoreManagementService.ScoreStats stats,
                              List<String> grades,
                              List<ScoreManagementService.FilterOption> classes,
                              List<ScoreManagementService.FilterOption> subjects,
                              List<ScoreManagementService.FilterOption> courses) {
        model.addAttribute("activePage", "score");
        model.addAttribute("pageTitle", PAGE_TITLE_SCORE);
        model.addAttribute("scores", pageResult.getItems());
        model.addAttribute("pageData", pageResult);
        model.addAttribute("stats", stats);
        model.addAttribute("grades", grades);
        model.addAttribute("classOptions", classes);
        model.addAttribute("subjectOptions", subjects);
        model.addAttribute("courseOptions", courses);
    }

    public void applyCreatePage(Model model, ScoreCreateService.ScoreCreatePageData pageData) {
        model.addAttribute("activePage", "score");
        model.addAttribute("pageTitle", PAGE_TITLE_SCORE_CREATE);
        model.addAttribute("createData", pageData);
        model.addAttribute("filter", pageData == null ? new ScoreCreateService.ScoreCreateFilter() : pageData.getFilter());
    }

    public void applyDetailPage(Model model,
                                ScoreManagementService.ScoreGroupSummary summary,
                                ScoreCreateService.ScoreCreatePageData detailData,
                                String selectedSemester) {
        model.addAttribute("activePage", "score");
        model.addAttribute("pageTitle", PAGE_TITLE_SCORE_DETAIL);
        model.addAttribute("summary", summary);
        model.addAttribute("detailData", detailData);
        model.addAttribute("selectedHocKy", selectedSemester);
    }

    public void applyEditPage(Model model,
                              ScoreManagementService.ScoreGroupSummary summary,
                              ScoreCreateService.ScoreCreatePageData pageData) {
        model.addAttribute("activePage", "score");
        model.addAttribute("pageTitle", PAGE_TITLE_SCORE_EDIT);
        model.addAttribute("summary", summary);
        model.addAttribute("createData", pageData);
        model.addAttribute("filter", pageData.getFilter());
        model.addAttribute("formMode", "edit");
    }

    public ScoreCreateService.ScoreCreatePageData loadDetailData(String studentId,
                                                                 String subjectId,
                                                                 String namHoc,
                                                                 String hocKy) {
        ScoreCreateService.ScoreCreateFilter filter = new ScoreCreateService.ScoreCreateFilter();
        filter.setStudentId(studentId);
        filter.setMon(subjectId);
        filter.setNamHoc(namHoc);
        filter.setHocKy(hocKy);
        filter.setApplyFilter("1");
        ScoreCreateService.ScoreCreatePageData pageData = scoreCreateService.getCreatePageData(filter);
        if (pageData == null || !pageData.isReadyForInput()) {
            throw new RuntimeException("Không tìm thấy dữ liệu chi tiết điểm để hiển thị.");
        }
        return pageData;
    }

    public HttpHeaders downloadHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );
        return headers;
    }

    public String buildExportFileName(ScoreManagementService.ScoreGroupSummary summary,
                                      String hocKy,
                                      String extension) {
        String student = safeTrim(summary == null ? null : summary.getStudentId());
        String subject = safeTrim(summary == null ? null : summary.getSubjectId());
        String namHoc = safeTrim(summary == null ? null : summary.getNamHoc());
        return "chi-tiet-diem-"
                + (student == null ? "hs" : student)
                + "-"
                + (subject == null ? "mon" : subject)
                + "-"
                + (namHoc == null ? "nam-hoc" : namHoc)
                + "-"
                + semesterFileSuffix(hocKy)
                + "."
                + extension;
    }

    public String buildListExportFileName(String extension) {
        return "danh-sach-diem-" + EXPORT_FILE_DATE.format(LocalDate.now()) + "." + extension;
    }

    public void applySearchRedirectAttributes(RedirectAttributes redirectAttributes, ScoreSearch search) {
        if (redirectAttributes == null || search == null) {
            return;
        }
        if (safeTrim(search.getQ()) != null) {
            redirectAttributes.addAttribute("q", search.getQ());
        }
        if (safeTrim(search.getKhoi()) != null) {
            redirectAttributes.addAttribute("khoi", search.getKhoi());
        }
        if (safeTrim(search.getLop()) != null) {
            redirectAttributes.addAttribute("lop", search.getLop());
        }
        if (safeTrim(search.getMon()) != null) {
            redirectAttributes.addAttribute("mon", search.getMon());
        }
        if (safeTrim(search.getHocKy()) != null) {
            redirectAttributes.addAttribute("hocKy", search.getHocKy());
        }
        if (safeTrim(search.getKhoa()) != null) {
            redirectAttributes.addAttribute("khoa", search.getKhoa());
        }
    }

    public void validateExportEligibility(ScoreCreateService.ScoreCreatePageData detailData, String selectedSemester) {
        boolean hasHk1 = hasSemesterAverage(detailData == null ? null : detailData.getHk1Input());
        boolean hasHk2 = hasSemesterAverage(detailData == null ? null : detailData.getHk2Input());

        if (SEMESTER_1.equals(selectedSemester) && !hasHk1) {
            throw new RuntimeException("Không thể xuất file học kỳ I vì học sinh chưa có điểm học kỳ I.");
        }
        if (SEMESTER_2.equals(selectedSemester) && !hasHk2) {
            throw new RuntimeException("Không thể xuất file học kỳ II vì học sinh chưa có điểm học kỳ II.");
        }
        if (SEMESTER_ALL.equals(selectedSemester)) {
            if (!hasHk1 && !hasHk2) {
                throw new RuntimeException("Không thể xuất file cả năm vì học sinh chưa có điểm học kỳ I và học kỳ II.");
            }
            if (!hasHk1) {
                throw new RuntimeException("Không thể xuất file cả năm vì học sinh chưa có điểm học kỳ I.");
            }
            if (!hasHk2) {
                throw new RuntimeException("Không thể xuất file cả năm vì học sinh chưa có điểm học kỳ II.");
            }
        }
    }

    public String resolveSemester(String hocKy) {
        if (hocKy == null) {
            return SEMESTER_ALL;
        }
        String trimmed = hocKy.trim();
        if (SEMESTER_1.equals(trimmed) || SEMESTER_2.equals(trimmed) || SEMESTER_ALL.equals(trimmed)) {
            return trimmed;
        }
        return SEMESTER_ALL;
    }

    public String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String firstNonBlank(String first, String second) {
        String firstTrimmed = safeTrim(first);
        if (firstTrimmed != null) {
            return firstTrimmed;
        }
        return safeTrim(second);
    }

    public String resolveEditErrorMessage(RuntimeException ex) {
        String message = safeTrim(ex == null ? null : ex.getMessage());
        if (message != null) {
            return message;
        }
        return "Không thể lưu chỉnh sửa điểm. Vui lòng kiểm tra lại dữ liệu và thử lại.";
    }

    public String resolveDetailErrorMessage(RuntimeException ex) {
        String message = safeTrim(ex == null ? null : ex.getMessage());
        if (message != null) {
            return message;
        }
        return "Không thể tải trang chi tiết điểm. Vui lòng thử lại.";
    }

    public String resolveExportErrorMessage(RuntimeException ex) {
        String message = safeTrim(ex == null ? null : ex.getMessage());
        if (message != null) {
            return message;
        }
        return "Không thể xuất file điểm. Vui lòng kiểm tra dữ liệu và thử lại.";
    }

    private String semesterFileSuffix(String hocKy) {
        if (SEMESTER_1.equals(hocKy)) {
            return "hk1";
        }
        if (SEMESTER_2.equals(hocKy)) {
            return "hk2";
        }
        return "ca-nam";
    }

    private boolean hasSemesterAverage(ScoreCreateService.SemesterInput semesterInput) {
        return semesterInput != null && semesterInput.getAverage() != null;
    }
}
