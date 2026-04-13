package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.search.AdminReportSearch;
import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RewardDisciplineAdminReportHandler extends AbstractAdminReportTypeHandler {

    private final ConductManagementService conductManagementService;

    public RewardDisciplineAdminReportHandler(ConductManagementService conductManagementService) {
        this.conductManagementService = conductManagementService;
    }

    @Override
    public AdminReportType getType() {
        return AdminReportType.REWARD_DISCIPLINE;
    }

    @Override
    public AdminReportTypeResult buildResult(AdminReportSearch search) {
        ConductSearch conductSearch = mapSearch(search);
        List<ConductManagementService.ConductRow> rows = new ArrayList<>(conductManagementService.getRowsForExport(conductSearch));

        String schoolYearFilter = normalize(search == null ? null : search.getNamHoc());
        Integer semesterFilter = parseInteger(search == null ? null : search.getHocKy());
        rows = rows.stream()
                .filter(row -> schoolYearFilter == null || schoolYearFilter.equals(normalize(row.getNamHoc())))
                .filter(row -> semesterFilter == null || (row.getHocKy() != null && semesterFilter.equals(row.getHocKy())))
                .filter(this::isValidPreviewRow)
                .sorted(Comparator
                        .comparing((ConductManagementService.ConductRow row) -> extractGivenNameForSort(row.getTenHocSinh()))
                        .thenComparing(row -> extractNamePrefixForSort(row.getTenHocSinh()))
                        .thenComparing(row -> normalizeSortValue(row.getTenHocSinh()))
                        .thenComparing(row -> normalizeSortValue(row.getIdHocSinh()))
                )
                .toList();

        List<List<String>> previewRows = rows.stream()
                .map(this::toPreviewRow)
                .filter(this::hasMeaningfulCell)
                .toList();

        long totalReward = rows.stream().filter(ConductManagementService.ConductRow::isKhenThuong).count();
        long totalDiscipline = rows.size() - totalReward;

        AdminReportPreview preview = new AdminReportPreview(
                List.of(
                        new AdminReportPreview.MetricItem("Tổng sự kiện", String.valueOf(rows.size()), "neutral"),
                        new AdminReportPreview.MetricItem("Khen thưởng", String.valueOf(totalReward), "good"),
                        new AdminReportPreview.MetricItem("Kỷ luật", String.valueOf(totalDiscipline), "warn"),
                        new AdminReportPreview.MetricItem("Tỉ lệ tích cực", ratioDisplay(totalReward, rows.size()), "good")
                ),
                List.of("Mã HS", "Họ tên", "Mã lớp", "Lớp", "Khối", "Khóa học", "Loại", "Số quyết định", "Nội dung chi tiết", "Ngày ban hành"),
                previewRows,
                "Không có dữ liệu khen thưởng/kỷ luật phù hợp.",
                previewRows.size()
        );

        AdminReportFilterBundle filters = new AdminReportFilterBundle(
                buildSchoolYearOptions(rows),
                defaultSemesterOptions(),
                prependAll(conductManagementService.getGrades().stream()
                        .map(grade -> option(grade, "Khối " + grade))
                        .toList(), "Tất cả khối"),
                prependAll(conductManagementService.getClasses().stream()
                        .map(item -> option(item.getId(), item.getName()))
                        .toList(), "Tất cả lớp"),
                List.of(),
                prependAll(conductManagementService.getCourses().stream()
                        .map(item -> option(item.getId(), item.getName()))
                        .toList(), "Tất cả khóa học"),
                List.of(
                        option("", "Tất cả loại"),
                        option(ConductManagementService.LOAI_KHEN_THUONG, "Khen thưởng"),
                        option(ConductManagementService.LOAI_KY_LUAT, "Kỷ luật")
                ),
                List.of(),
                List.of(),
                List.of()
        );

        return new AdminReportTypeResult(filters, preview);
    }

    @Override
    public String buildFilterSummary(AdminReportSearch search) {
        List<String> parts = new ArrayList<>();
        if (search == null) {
            return "Không dùng bộ lọc";
        }

        addSummary(parts, "Năm học", search.getNamHoc());
        addSummary(parts, "Học kỳ", displaySemester(search.getHocKy()));
        addSummary(parts, "Khối", search.getKhoi());
        addSummary(parts, "Lớp", search.getLop());
        addSummary(parts, "Loại", displayType(search.getLoai()));
        addSummary(parts, "Khóa", search.getKhoa());
        addSummary(parts, "Từ khóa", search.getQ());

        return parts.isEmpty() ? "Không dùng bộ lọc" : String.join(" | ", parts);
    }

    private void addSummary(List<String> parts, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String sanitized = fallback(value);
        if ("-".equals(sanitized)) {
            return;
        }
        parts.add(label + ": " + sanitized);
    }

    private String displaySemester(String hocKy) {
        if (hocKy == null || hocKy.isBlank()) {
            return null;
        }
        return switch (hocKy.trim()) {
            case "0" -> "Cả năm";
            case "1" -> "Học kỳ 1";
            case "2" -> "Học kỳ 2";
            default -> hocKy;
        };
    }

    private String displayType(String loai) {
        if (loai == null || loai.isBlank()) {
            return null;
        }
        if (ConductManagementService.LOAI_KHEN_THUONG.equalsIgnoreCase(loai)) {
            return "Khen thưởng";
        }
        if (ConductManagementService.LOAI_KY_LUAT.equalsIgnoreCase(loai)) {
            return "Kỷ luật";
        }
        return loai;
    }

    private String ratioDisplay(long numerator, long denominator) {
        if (denominator <= 0) {
            return "0%";
        }
        double value = numerator * 100.0 / denominator;
        return String.format("%.1f%%", value);
    }

    private List<String> toPreviewRow(ConductManagementService.ConductRow row) {
        String classDisplay = fallback(row.getTenLop());
        String classCode = extractClassCode(classDisplay);
        String className = extractClassName(classDisplay);
        return sanitizeRow(List.of(
                row.getIdHocSinh(),
                row.getTenHocSinh(),
                classCode,
                className,
                row.getKhoi(),
                row.getKhoaHoc(),
                row.getLoaiDisplay(),
                row.getSoQuyetDinh(),
                row.getNoiDungChiTiet(),
                row.getNgayBanHanh()
        ));
    }

    private String extractClassCode(String classDisplay) {
        if (classDisplay == null || classDisplay.isBlank() || "-".equals(classDisplay)) {
            return "-";
        }
        int separator = classDisplay.indexOf('-');
        if (separator <= 0) {
            return classDisplay;
        }
        String code = classDisplay.substring(0, separator).trim();
        return code.isEmpty() ? classDisplay : code;
    }

    private String extractClassName(String classDisplay) {
        if (classDisplay == null || classDisplay.isBlank() || "-".equals(classDisplay)) {
            return "-";
        }
        int separator = classDisplay.indexOf('-');
        if (separator < 0 || separator + 1 >= classDisplay.length()) {
            return classDisplay;
        }
        String name = classDisplay.substring(separator + 1).trim();
        return name.isEmpty() ? classDisplay : name;
    }

    private ConductSearch mapSearch(AdminReportSearch search) {
        ConductSearch conductSearch = new ConductSearch();
        if (search == null) {
            return conductSearch;
        }
        conductSearch.setQ(search.getQ());
        conductSearch.setKhoi(search.getKhoi());
        conductSearch.setLop(search.getLop());
        conductSearch.setKhoa(search.getKhoa());
        conductSearch.setLoai(search.getLoai());
        return conductSearch;
    }

    private List<AdminReportFilterOption> buildSchoolYearOptions(List<ConductManagementService.ConductRow> rows) {
        Set<String> years = rows.stream()
                .map(ConductManagementService.ConductRow::getNamHoc)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> sortedYears = years.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        if (sortedYears.isEmpty()) {
            return prependAll(defaultSchoolYearOptions(), "Tất cả năm học");
        }

        return prependAll(sortedYears.stream().map(year -> option(year, year)).toList(), "Tất cả năm học");
    }

    private List<AdminReportFilterOption> prependAll(List<AdminReportFilterOption> options, String label) {
        List<AdminReportFilterOption> all = new ArrayList<>();
        all.add(option("", label));
        all.addAll(options);
        return all;
    }

    private boolean isValidPreviewRow(ConductManagementService.ConductRow row) {
        if (row == null) {
            return false;
        }
        return !containsHeaderNoise(
                row.getIdHocSinh(),
                row.getTenHocSinh(),
                row.getTenLop(),
                row.getKhoi(),
                row.getKhoaHoc(),
                row.getLoai(),
                row.getLoaiChiTiet(),
                row.getSoQuyetDinh(),
                row.getNoiDungChiTiet(),
                row.getNgayBanHanh(),
                row.getNamHoc(),
                row.getHocKyDisplay()
        );
    }
}
