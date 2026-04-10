package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.search.AdminReportSearch;
import com.quanly.webdiem.model.search.ScoreSearch;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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

        List<ScoreManagementService.ScoreRow> allRows = new ArrayList<>(scoreManagementService.getRowsForExport(scoreSearch));
        String namHocFilter = normalize(search == null ? null : search.getNamHoc());
        if (namHocFilter != null) {
            allRows = allRows.stream()
                    .filter(row -> namHocFilter.equals(normalize(row.getNamHoc())))
                    .toList();
        }

        ScoreManagementService.ScoreStats stats = scoreManagementService.getStats(scoreSearch);
        AdminReportPreview preview = new AdminReportPreview(
                List.of(
                        new AdminReportPreview.MetricItem("Tổng học sinh có điểm", String.valueOf(stats.getTotalStudentsWithScores()), "neutral"),
                        new AdminReportPreview.MetricItem("Điểm trung bình toàn trường", stats.getSchoolAverageDisplay(), "good"),
                        new AdminReportPreview.MetricItem("Tỷ lệ giỏi + khá", stats.getGoodRateDisplay(), "good"),
                        new AdminReportPreview.MetricItem("Tỷ lệ cần hỗ trợ", stats.getWeakRateDisplay(), "warn")
                ),
                List.of("Họ và tên", "Lớp", "Môn học", "Điểm TB", "Hạnh kiểm", "Trạng thái"),
                allRows.stream().limit(8).map(this::toPreviewRow).toList(),
                "Không có dữ liệu điểm phù hợp với bộ lọc.",
                allRows.size()
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

        if (parts.isEmpty()) {
            return "Không dùng bộ lọc";
        }
        return String.join(" | ", parts);
    }

    private void addSummary(List<String> parts, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        parts.add(label + ": " + value);
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

    private List<String> toPreviewRow(ScoreManagementService.ScoreRow row) {
        double average = resolveAverage(row);
        String status = resolveStatus(average, row.getHanhKiem());
        return List.of(
                fallback(row.getTenHocSinh()),
                fallback(row.getTenLop()),
                fallback(row.getTenMon()),
                formatAverage(average),
                fallback(row.getHanhKiem()),
                status
        );
    }

    private double resolveAverage(ScoreManagementService.ScoreRow row) {
        if (row == null) {
            return 0.0;
        }

        if (row.getTongKetCaNam() != null) {
            return row.getTongKetCaNam();
        }
        if (row.getTongKet() != null) {
            return row.getTongKet();
        }
        if (row.getTongKetHocKy2() != null) {
            return row.getTongKetHocKy2();
        }
        if (row.getTongKetHocKy1() != null) {
            return row.getTongKetHocKy1();
        }
        return 0.0;
    }

    private String formatAverage(double value) {
        if (value <= 0) {
            return "-";
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private String resolveStatus(double average, String conduct) {
        String normalizedConduct = normalizeAscii(conduct);
        if (normalizedConduct == null) {
            normalizedConduct = "";
        }
        boolean weakConduct = normalizedConduct.contains("yeu") || normalizedConduct.contains("kem");
        boolean goodConduct = normalizedConduct.contains("tot") || normalizedConduct.contains("gioi") || normalizedConduct.contains("kha");

        if (average < 5.0 || weakConduct) {
            return "Cần lưu ý";
        }
        if (average >= 8.0 && goodConduct) {
            return "Khen thưởng";
        }
        return "Ổn định";
    }
}
