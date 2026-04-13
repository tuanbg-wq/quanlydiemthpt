package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.search.AdminReportSearch;
import com.quanly.webdiem.model.search.ScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScoreAdminReportHandler extends AbstractAdminReportTypeHandler {

    private final ScoreManagementService scoreManagementService;

    public ScoreAdminReportHandler(ScoreManagementService scoreManagementService) {
        this.scoreManagementService = scoreManagementService;
    }

    @Override
    public AdminReportType getType() {
        return AdminReportType.SCORE;
    }

    @Override
    public AdminReportTypeResult buildResult(AdminReportSearch search) {
        ScoreSearch scoreSearch = mapSearch(search);
        boolean annualView = "0".equals(search == null ? null : search.getHocKy());

        List<ScoreManagementService.ScoreRow> allRows = new ArrayList<>(scoreManagementService.getRowsForExport(scoreSearch));
        String schoolYearFilter = normalize(search == null ? null : search.getNamHoc());
        if (schoolYearFilter != null) {
            allRows = allRows.stream()
                    .filter(row -> schoolYearFilter.equals(normalize(row.getNamHoc())))
                    .toList();
        }
        allRows = allRows.stream()
                .filter(this::isValidPreviewRow)
                .sorted(Comparator
                        .comparing((ScoreManagementService.ScoreRow row) -> extractGivenNameForSort(row.getTenHocSinh()))
                        .thenComparing(row -> extractNamePrefixForSort(row.getTenHocSinh()))
                        .thenComparing(row -> normalizeSortValue(row.getTenHocSinh()))
                        .thenComparing(row -> normalizeSortValue(row.getIdHocSinh()))
                )
                .toList();

        List<List<String>> previewRows = allRows.stream()
                .map(row -> toPreviewRow(row, annualView))
                .filter(this::hasMeaningfulCell)
                .toList();

        ScoreManagementService.ScoreStats stats = scoreManagementService.getStats(scoreSearch);
        AdminReportPreview preview = new AdminReportPreview(
                List.of(
                        new AdminReportPreview.MetricItem("Tổng học sinh có điểm", String.valueOf(stats.getTotalStudentsWithScores()), "neutral"),
                        new AdminReportPreview.MetricItem("Điểm trung bình toàn trường", stats.getSchoolAverageDisplay(), "good"),
                        new AdminReportPreview.MetricItem("Tỉ lệ giỏi + khá", stats.getGoodRateDisplay(), "good"),
                        new AdminReportPreview.MetricItem("Tỉ lệ cần hỗ trợ", stats.getWeakRateDisplay(), "warn")
                ),
                annualView
                        ? List.of("Mã học sinh", "Tên học sinh", "Lớp", "Môn", "Tổng kết kỳ 1", "Tổng kết kỳ 2", "Cả năm", "Học kỳ", "Năm học")
                        : List.of("Mã học sinh", "Tên học sinh", "Lớp", "Môn", "Giữa kỳ", "Cuối kỳ", "Tổng kết", "Học kỳ", "Năm học"),
                previewRows,
                "Không có dữ liệu điểm phù hợp với bộ lọc.",
                previewRows.size()
        );

        AdminReportFilterBundle filters = new AdminReportFilterBundle(
                buildSchoolYearOptions(allRows),
                defaultSemesterOptions(),
                prependAll(scoreManagementService.getGrades().stream()
                        .map(grade -> option(grade, "Khối " + grade))
                        .toList(), "Tất cả khối"),
                prependAll(scoreManagementService.getClasses().stream()
                        .map(item -> option(item.getId(), item.getName()))
                        .toList(), "Tất cả lớp"),
                prependAll(scoreManagementService.getSubjects().stream()
                        .map(item -> option(item.getId(), item.getName()))
                        .toList(), "Tất cả môn"),
                prependAll(scoreManagementService.getCourses().stream()
                        .map(item -> option(item.getId(), item.getName()))
                        .toList(), "Tất cả khóa học"),
                List.of(),
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
        addSummary(parts, "Môn", search.getMon());
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

    private List<AdminReportFilterOption> buildSchoolYearOptions(List<ScoreManagementService.ScoreRow> rows) {
        Set<String> years = rows.stream()
                .map(ScoreManagementService.ScoreRow::getNamHoc)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> sortedYears = years.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        if (sortedYears.isEmpty()) {
            return prependAll(defaultSchoolYearOptions(), "Tất cả năm học");
        }

        List<AdminReportFilterOption> options = sortedYears.stream()
                .map(year -> option(year, year))
                .toList();
        return prependAll(options, "Tất cả năm học");
    }

    private List<AdminReportFilterOption> prependAll(List<AdminReportFilterOption> options, String label) {
        List<AdminReportFilterOption> all = new ArrayList<>();
        all.add(option("", label));
        all.addAll(options);
        return all;
    }

    private ScoreSearch mapSearch(AdminReportSearch search) {
        ScoreSearch scoreSearch = new ScoreSearch();
        if (search == null) {
            return scoreSearch;
        }

        scoreSearch.setQ(search.getQ());
        scoreSearch.setKhoi(search.getKhoi());
        scoreSearch.setLop(search.getLop());
        scoreSearch.setMon(search.getMon());
        scoreSearch.setHocKy(search.getHocKy());
        scoreSearch.setKhoa(search.getKhoa());
        return scoreSearch;
    }

    private List<String> toPreviewRow(ScoreManagementService.ScoreRow row, boolean annualView) {
        if (row == null) {
            return List.of();
        }
        if (annualView) {
            return sanitizeRow(List.of(
                    row.getIdHocSinh(),
                    row.getTenHocSinh(),
                    row.getTenLop(),
                    row.getTenMon(),
                    row.getTongKetHocKy1Display(),
                    row.getTongKetHocKy2Display(),
                    row.getTongKetCaNamDisplay(),
                    row.getHocKyDisplay(),
                    row.getNamHocDisplay()
            ));
        }
        return sanitizeRow(List.of(
                row.getIdHocSinh(),
                row.getTenHocSinh(),
                row.getTenLop(),
                row.getTenMon(),
                row.getDiemGiuaKyDisplay(),
                row.getDiemCuoiKyDisplay(),
                row.getTongKetDisplay(),
                row.getHocKyDisplay(),
                row.getNamHocDisplay()
        ));
    }

    private boolean isValidPreviewRow(ScoreManagementService.ScoreRow row) {
        if (row == null) {
            return false;
        }
        return !containsHeaderNoise(
                row.getIdHocSinh(),
                row.getTenHocSinh(),
                row.getTenLop(),
                row.getTenMon(),
                row.getHanhKiem(),
                row.getNamHocDisplay(),
                row.getHocKyDisplay()
        );
    }
}
