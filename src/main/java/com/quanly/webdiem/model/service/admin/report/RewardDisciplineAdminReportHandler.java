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

        String namHocFilter = normalize(search == null ? null : search.getNamHoc());
        Integer hocKyFilter = parseInteger(search == null ? null : search.getHocKy());
        rows = rows.stream()
                .filter(row -> namHocFilter == null || namHocFilter.equals(normalize(row.getNamHoc())))
                .filter(row -> hocKyFilter == null || (row.getHocKy() != null && hocKyFilter.equals(row.getHocKy())))
                .toList();

        long totalReward = rows.stream().filter(ConductManagementService.ConductRow::isKhenThuong).count();
        long totalDiscipline = rows.size() - totalReward;

        AdminReportPreview preview = new AdminReportPreview(
                List.of(
                        new AdminReportPreview.MetricItem("Tổng sự kiện", String.valueOf(rows.size()), "neutral"),
                        new AdminReportPreview.MetricItem("Khen thưởng", String.valueOf(totalReward), "good"),
                        new AdminReportPreview.MetricItem("Kỷ luật", String.valueOf(totalDiscipline), "warn"),
                        new AdminReportPreview.MetricItem("Tỷ lệ tích cực", ratioDisplay(totalReward, rows.size()), "good")
                ),
                List.of("Học sinh", "Lớp", "Loại", "Số quyết định", "Ngày ban hành", "Nội dung"),
                rows.stream().limit(8).map(this::toPreviewRow).toList(),
                "Không có dữ liệu khen thưởng/kỷ luật phù hợp.",
                rows.size()
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

    private void addSummary(List<String> parts, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        parts.add(label + ": " + value);
    }

    private String ratioDisplay(long numerator, long denominator) {
        if (denominator <= 0) {
            return "0%";
        }
        double value = numerator * 100.0 / denominator;
        return String.format("%.1f%%", value);
    }

    private List<String> toPreviewRow(ConductManagementService.ConductRow row) {
        return List.of(
                fallback(row.getTenHocSinh()),
                fallback(row.getTenLop()),
                fallback(row.getLoaiDisplay()),
                fallback(row.getSoQuyetDinh()),
                fallback(row.getNgayBanHanh()),
                fallback(row.getNoiDungChiTiet())
        );
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
}
