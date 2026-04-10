package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.dto.TeacherListItem;
import com.quanly.webdiem.model.search.AdminReportSearch;
import com.quanly.webdiem.model.search.TeacherSearch;
import com.quanly.webdiem.model.service.admin.TeacherService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TeacherProfileAdminReportHandler extends AbstractAdminReportTypeHandler {

    private final TeacherService teacherService;

    public TeacherProfileAdminReportHandler(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @Override
    public AdminReportType getType() {
        return AdminReportType.TEACHER_PROFILE;
    }

    @Override
    public AdminReportTypeResult buildResult(AdminReportSearch search) {
        TeacherSearch teacherSearch = mapSearch(search);
        List<TeacherListItem> rows = fetchAllRows(teacherSearch);

        String roleFilter = normalizeAscii(search == null ? null : search.getVaiTro());
        if (roleFilter != null) {
            rows = rows.stream()
                    .filter(item -> roleFilter.equals(resolveRoleCode(item.getVaiTro())))
                    .toList();
        }

        long activeCount = rows.stream()
                .filter(item -> normalize(item.getTrangThai()) != null && normalize(item.getTrangThai()).contains("dang"))
                .count();
        long homeroomCount = rows.stream()
                .filter(item -> "gvcn".equals(resolveRoleCode(item.getVaiTro())))
                .count();

        AdminReportPreview preview = new AdminReportPreview(
                List.of(
                        new AdminReportPreview.MetricItem("Tổng giáo viên", String.valueOf(rows.size()), "neutral"),
                        new AdminReportPreview.MetricItem("Đang công tác", String.valueOf(activeCount), "good"),
                        new AdminReportPreview.MetricItem("Giáo viên chủ nhiệm", String.valueOf(homeroomCount), "good"),
                        new AdminReportPreview.MetricItem("Tỷ lệ chủ nhiệm", ratioDisplay(homeroomCount, rows.size()), "neutral")
                ),
                List.of("Giáo viên", "Bộ môn", "Vai trò", "Trạng thái", "Email", "Điện thoại"),
                rows.stream().limit(8).map(this::toPreviewRow).toList(),
                "Không có dữ liệu hồ sơ giáo viên phù hợp.",
                rows.size()
        );

        AdminReportFilterBundle filters = new AdminReportFilterBundle(
                List.of(),
                List.of(),
                prependAll(teacherService.getGrades().stream().map(grade -> option(grade, "Khối " + grade)).toList(), "Tất cả khối"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                prependAll(teacherService.getSubjects().stream().map(subject -> option(subject, subject)).toList(), "Tất cả bộ môn"),
                prependAll(teacherService.getStatuses().stream().map(status -> option(status, displayStatus(status))).toList(), "Tất cả trạng thái"),
                buildRoleOptions(rows)
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
        addSummary(parts, "Vai trò", displayRoleFromCode(search.getVaiTro()));
        addSummary(parts, "Từ khóa", search.getQ());

        return parts.isEmpty() ? "Không dùng bộ lọc" : String.join(" | ", parts);
    }

    private void addSummary(List<String> parts, String label, String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return;
        }
        parts.add(label + ": " + value);
    }

    private String ratioDisplay(long numerator, long denominator) {
        if (denominator <= 0) {
            return "0%";
        }
        double value = numerator * 100.0 / denominator;
        return String.format(Locale.US, "%.1f%%", value);
    }

    private List<String> toPreviewRow(TeacherListItem item) {
        return List.of(
                fallback(item.getHoTen()),
                fallback(item.getMonDay()),
                fallback(item.getVaiTro()),
                displayStatus(item.getTrangThai()),
                fallback(item.getEmail()),
                fallback(item.getSoDienThoai())
        );
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

    private List<AdminReportFilterOption> buildRoleOptions(List<TeacherListItem> items) {
        Set<String> roleCodes = new LinkedHashSet<>();
        roleCodes.add("gvcn");
        roleCodes.add("gvbm");
        roleCodes.add("admin");

        items.stream()
                .map(TeacherListItem::getVaiTro)
                .map(this::resolveRoleCode)
                .filter(code -> code != null && !code.isBlank())
                .forEach(roleCodes::add);

        List<AdminReportFilterOption> options = roleCodes.stream()
                .map(code -> option(code, displayRoleFromCode(code)))
                .toList();

        return prependAll(options, "Tất cả vai trò");
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
        return normalizedRole.replace(' ', '_');
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
        return rawStatus;
    }

    private String displayRoleFromCode(String code) {
        String normalizedCode = normalize(code);
        if (normalizedCode == null) {
            return "-";
        }
        if (normalizedCode.equals("gvcn")) {
            return "Giáo viên chủ nhiệm";
        }
        if (normalizedCode.equals("gvbm") || normalizedCode.equals("giao vien")) {
            return "Giáo viên bộ môn";
        }
        if (normalizedCode.equals("admin")) {
            return "Admin";
        }
        return code;
    }
}
