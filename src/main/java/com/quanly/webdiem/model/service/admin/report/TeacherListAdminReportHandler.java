package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.dto.TeacherListItem;
import com.quanly.webdiem.model.search.AdminReportSearch;
import com.quanly.webdiem.model.search.TeacherSearch;
import com.quanly.webdiem.model.service.admin.TeacherService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TeacherListAdminReportHandler extends AbstractAdminReportTypeHandler {

    private final TeacherService teacherService;

    public TeacherListAdminReportHandler(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @Override
    public AdminReportType getType() {
        return AdminReportType.TEACHER_LIST;
    }

    @Override
    public AdminReportTypeResult buildResult(AdminReportSearch search) {
        TeacherSearch teacherSearch = mapSearch(search);
        List<TeacherListItem> rows = fetchAllRows(teacherSearch).stream()
                .filter(this::isValidPreviewRow)
                .toList();

        String roleFilter = normalizeAscii(search == null ? null : search.getVaiTro());
        if (roleFilter != null) {
            rows = rows.stream()
                    .filter(item -> roleFilter.equals(resolveRoleCode(item.getVaiTro())))
                    .toList();
        }

        rows = rows.stream()
                .sorted(Comparator
                        .comparing((TeacherListItem item) -> extractGivenNameForSort(item.getHoTen()))
                        .thenComparing(item -> extractNamePrefixForSort(item.getHoTen()))
                        .thenComparing(item -> normalizeSortValue(item.getHoTen()))
                        .thenComparing(item -> normalizeSortValue(item.getIdGiaoVien()))
                )
                .toList();

        long homeroomCount = rows.stream().filter(item -> "gvcn".equals(resolveRoleCode(item.getVaiTro()))).count();
        long subjectTeacherCount = rows.stream().filter(item -> "gvbm".equals(resolveRoleCode(item.getVaiTro()))).count();
        long inactiveCount = rows.stream()
                .filter(item -> {
                    String normalizedStatus = normalize(item.getTrangThai());
                    return normalizedStatus != null && normalizedStatus.contains("nghi");
                })
                .count();

        List<List<String>> previewRows = rows.stream()
                .map(this::toPreviewRow)
                .filter(this::hasMeaningfulCell)
                .toList();

        AdminReportPreview preview = new AdminReportPreview(
                List.of(
                        new AdminReportPreview.MetricItem("Tổng giáo viên", String.valueOf(rows.size()), "neutral"),
                        new AdminReportPreview.MetricItem("Giáo viên bộ môn", String.valueOf(subjectTeacherCount), "good"),
                        new AdminReportPreview.MetricItem("Giáo viên chủ nhiệm", String.valueOf(homeroomCount), "good"),
                        new AdminReportPreview.MetricItem("Đang nghỉ/không hoạt động", String.valueOf(inactiveCount), "warn")
                ),
                List.of("Mã giáo viên", "Họ và tên", "Giới tính", "Số điện thoại", "Môn dạy", "Chủ nhiệm lớp", "Lớp bộ môn", "Vai trò"),
                previewRows,
                "Không có dữ liệu danh sách giáo viên phù hợp.",
                previewRows.size()
        );

        AdminReportFilterBundle filters = new AdminReportFilterBundle(
                List.of(),
                List.of(),
                prependAll(teacherService.getGrades().stream()
                        .map(grade -> option(grade, "Khối " + grade))
                        .toList(), "Tất cả khối"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                prependAll(teacherService.getSubjects().stream()
                        .map(subject -> option(subject, subject))
                        .toList(), "Tất cả bộ môn"),
                prependAll(teacherService.getStatuses().stream()
                        .map(status -> option(status, displayStatus(status)))
                        .toList(), "Tất cả trạng thái"),
                List.of(
                        option("", "Tất cả vai trò"),
                        option("gvcn", "Giáo viên chủ nhiệm"),
                        option("gvbm", "Giáo viên bộ môn"),
                        option("admin", "Admin")
                )
        );

        return new AdminReportTypeResult(filters, preview);
    }

    @Override
    public String buildFilterSummary(AdminReportSearch search) {
        List<String> parts = new ArrayList<>();
        if (search == null) {
            return "Không dùng bộ lọc";
        }

        addSummary(parts, "Bộ môn", search.getBoMon());
        addSummary(parts, "Khối", search.getKhoi());
        addSummary(parts, "Trạng thái", displayStatus(search.getTrangThai()));
        addSummary(parts, "Vai trò", displayRole(search.getVaiTro()));
        addSummary(parts, "Từ khóa", search.getQ());

        return parts.isEmpty() ? "Không dùng bộ lọc" : String.join(" | ", parts);
    }

    private void addSummary(List<String> parts, String label, String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return;
        }
        String sanitizedValue = fallback(value);
        if ("-".equals(sanitizedValue)) {
            return;
        }
        parts.add(label + ": " + sanitizedValue);
    }

    private List<String> toPreviewRow(TeacherListItem item) {
        return sanitizeRow(List.of(
                item.getIdGiaoVien(),
                item.getHoTen(),
                item.getGioiTinh(),
                item.getSoDienThoai(),
                item.getMonDay(),
                item.getChuNhiemLop(),
                item.getLopBoMon(),
                displayRoleFromRaw(item.getVaiTro())
        ));
    }

    private TeacherSearch mapSearch(AdminReportSearch search) {
        TeacherSearch teacherSearch = new TeacherSearch();
        if (search == null) {
            return teacherSearch;
        }

        teacherSearch.setQ(search.getQ());
        teacherSearch.setBoMon(search.getBoMon());
        teacherSearch.setKhoi(search.getKhoi());
        teacherSearch.setTrangThai(search.getTrangThai());
        return teacherSearch;
    }

    private List<TeacherListItem> fetchAllRows(TeacherSearch search) {
        List<TeacherListItem> items = new ArrayList<>();
        int currentPage = 1;
        int totalPages;

        do {
            search.setPage(currentPage);
            TeacherService.TeacherPageResult pageResult = teacherService.search(search);
            items.addAll(pageResult.getItems());
            totalPages = pageResult.getTotalPages();
            currentPage++;
        } while (currentPage <= totalPages);

        return items;
    }

    private List<AdminReportFilterOption> prependAll(List<AdminReportFilterOption> options, String label) {
        List<AdminReportFilterOption> all = new ArrayList<>();
        all.add(option("", label));
        all.addAll(options);
        return all;
    }

    private String resolveRoleCode(String rawRole) {
        String normalizedRole = normalizeAscii(rawRole);
        if (normalizedRole == null) {
            return null;
        }
        if (normalizedRole.contains("chu nhiem")) {
            return "gvcn";
        }
        if (normalizedRole.contains("bo mon") || normalizedRole.equals("giao vien")) {
            return "gvbm";
        }
        if (normalizedRole.contains("admin")) {
            return "admin";
        }
        return normalizedRole;
    }

    private String displayRoleFromRaw(String rawRole) {
        return displayRole(resolveRoleCode(rawRole));
    }

    private String displayRole(String code) {
        String normalizedCode = normalize(code);
        if (normalizedCode == null) {
            return "-";
        }
        if (normalizedCode.equals("gvcn")) {
            return "Giáo viên chủ nhiệm";
        }
        if (normalizedCode.equals("gvbm")) {
            return "Giáo viên bộ môn";
        }
        if (normalizedCode.equals("admin")) {
            return "Admin";
        }
        return fallback(code);
    }

    private String displayStatus(String rawStatus) {
        String normalizedStatus = normalize(rawStatus);
        if (normalizedStatus == null) {
            return "-";
        }
        if (normalizedStatus.equals("dang_lam") || normalizedStatus.equals("dang lam")) {
            return "Đang làm";
        }
        if (normalizedStatus.equals("nghi_huu") || normalizedStatus.equals("nghi huu")) {
            return "Nghỉ hưu";
        }
        if (normalizedStatus.equals("nghi_viec") || normalizedStatus.equals("nghi viec")) {
            return "Nghỉ việc";
        }
        return fallback(rawStatus);
    }

    private boolean isValidPreviewRow(TeacherListItem item) {
        if (item == null) {
            return false;
        }
        return !containsHeaderNoise(
                item.getAvatar(),
                item.getIdGiaoVien(),
                item.getHoTen(),
                item.getGioiTinh(),
                item.getSoDienThoai(),
                item.getMonDay(),
                item.getChuNhiemLop(),
                item.getLopBoMon(),
                item.getVaiTro(),
                item.getTrangThai(),
                item.getEmail()
        );
    }
}
