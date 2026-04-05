package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ScoreDAO;
import com.quanly.webdiem.model.search.ScoreSearch;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class ScoreQueryService {

    private static final int PAGE_SIZE = 10;

    private final ScoreDAO scoreDAO;

    public ScoreQueryService(ScoreDAO scoreDAO) {
        this.scoreDAO = scoreDAO;
    }

    public ScoreManagementService.ScorePageResult search(ScoreSearch search) {
        List<ScoreManagementService.ScoreRow> rows = findRowsBySearch(search);

        int requestedPage = normalizePage(search == null ? null : search.getPage());
        return paginate(rows, requestedPage);
    }

    public ScoreManagementService.ScoreStats getStats(ScoreSearch search) {
        List<ScoreManagementService.ScoreRow> rows = findRowsBySearch(search);
        long totalStudentsWithScores = rows.stream()
                .map(ScoreManagementService.ScoreRow::getIdHocSinh)
                .filter(Objects::nonNull)
                .map(id -> id.toLowerCase(Locale.ROOT))
                .distinct()
                .count();
        List<Double> totalScores = rows.stream()
                .map(ScoreManagementService.ScoreRow::getTongKet)
                .filter(Objects::nonNull)
                .toList();
        long totalGroups = totalScores.size();
        long excellentGroups = countByRange(totalScores, 8.0, null);
        long goodGroups = countByRange(totalScores, 6.5, 8.0);
        long averageGroups = countByRange(totalScores, 5.0, 6.5);
        long weakGroups = countByRange(totalScores, null, 5.0);
        double schoolAverage = totalScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double excellentRate = totalGroups == 0 ? 0.0 : (excellentGroups * 100.0 / totalGroups);
        double goodOnlyRate = totalGroups == 0 ? 0.0 : (goodGroups * 100.0 / totalGroups);
        double averageRate = totalGroups == 0 ? 0.0 : (averageGroups * 100.0 / totalGroups);
        double weakRate = totalGroups == 0 ? 0.0 : (weakGroups * 100.0 / totalGroups);
        double goodRate = excellentRate + goodOnlyRate;

        return new ScoreManagementService.ScoreStats(
                totalStudentsWithScores,
                roundOneDecimal(schoolAverage),
                roundOneDecimal(goodRate),
                roundOneDecimal(excellentRate),
                roundOneDecimal(goodOnlyRate),
                roundOneDecimal(averageRate),
                roundOneDecimal(weakRate)
        );
    }

    public ScoreManagementService.ScoreStats getStats() {
        return getStats(null);
    }

    public List<ScoreManagementService.ScoreRow> findRowsForExport(ScoreSearch search) {
        List<ScoreManagementService.ScoreRow> rows = new ArrayList<>(findRowsBySearch(search));
        if (isAnnualSearch(search)) {
            rows.removeIf(row -> !hasCompleteAnnualScores(row));
        }
        rows.sort(buildExportComparator());
        return rows;
    }

    private boolean isAnnualSearch(ScoreSearch search) {
        return search != null && "0".equals(trimOrNull(search.getHocKy()));
    }

    private boolean hasCompleteAnnualScores(ScoreManagementService.ScoreRow row) {
        if (row == null) {
            return false;
        }
        return row.getTongKetHocKy1() != null
                && row.getTongKetHocKy2() != null
                && row.getTongKetCaNam() != null;
    }

    public List<String> getGrades() {
        List<String> grades = new ArrayList<>(scoreDAO.findDistinctGrades().stream()
                .map(String::valueOf)
                .toList());
        if (!grades.contains("10")) {
            grades.add("10");
        }
        if (!grades.contains("11")) {
            grades.add("11");
        }
        if (!grades.contains("12")) {
            grades.add("12");
        }
        return grades.stream()
                .distinct()
                .sorted((left, right) -> {
                    Integer leftValue = parseInteger(left);
                    Integer rightValue = parseInteger(right);
                    if (leftValue != null && rightValue != null) {
                        return Integer.compare(leftValue, rightValue);
                    }
                    if (leftValue != null) {
                        return -1;
                    }
                    if (rightValue != null) {
                        return 1;
                    }
                    return left.compareTo(right);
                })
                .toList();
    }

    public List<ScoreManagementService.FilterOption> getClasses() {
        return scoreDAO.findDistinctClassesForFilter().stream()
                .map(this::mapClassOption)
                .toList();
    }

    public List<ScoreManagementService.FilterOption> getSubjects() {
        return scoreDAO.findDistinctSubjectsForFilter().stream()
                .map(this::mapDefaultOption)
                .toList();
    }

    public List<ScoreManagementService.FilterOption> getCourses() {
        return scoreDAO.findDistinctCoursesForFilter().stream()
                .map(this::mapCourseOption)
                .toList();
    }

    public ScoreManagementService.ScoreGroupSummary getScoreGroupSummary(String studentId,
                                                                         String subjectId,
                                                                         String namHoc) {
        List<Object[]> rows = scoreDAO.findScoreGroupSummary(
                normalize(studentId),
                normalize(subjectId),
                trimOrNull(namHoc)
        );
        if (rows.isEmpty()) {
            throw new RuntimeException("Không tìm thấy nhóm điểm cần thao tác.");
        }

        Object[] row = rows.get(0);
        return new ScoreManagementService.ScoreGroupSummary(
                asString(row, 0, "-"),
                asString(row, 1, "-"),
                asString(row, 2, "-"),
                asString(row, 3, "-"),
                asString(row, 4, "-"),
                asString(row, 5, "-"),
                asString(row, 6, "-"),
                asString(row, 7, "-")
        );
    }

    public List<ScoreManagementService.ScoreEntry> getScoreEntries(String studentId,
                                                                   String subjectId,
                                                                   String namHoc) {
        return scoreDAO.findScoreEntriesByGroup(
                        normalize(studentId),
                        normalize(subjectId),
                        trimOrNull(namHoc)
                ).stream()
                .map(this::mapScoreEntry)
                .toList();
    }

    private ScoreManagementService.ScorePageResult paginate(List<ScoreManagementService.ScoreRow> rows, int requestedPage) {
        int totalItems = rows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        int fromIndex = Math.max(0, (page - 1) * PAGE_SIZE);
        int toIndex = Math.min(totalItems, fromIndex + PAGE_SIZE);

        List<ScoreManagementService.ScoreRow> items = totalItems == 0
                ? Collections.emptyList()
                : rows.subList(fromIndex, toIndex);

        int fromRecord = totalItems == 0 ? 0 : fromIndex + 1;
        int toRecord = totalItems == 0 ? 0 : toIndex;

        return new ScoreManagementService.ScorePageResult(
                items,
                page,
                totalPages,
                totalItems,
                fromRecord,
                toRecord
        );
    }

    private ScoreManagementService.ScoreRow mapRow(Object[] row) {
        return new ScoreManagementService.ScoreRow(
                asString(row, 0, "-"),
                asString(row, 1, "-"),
                asString(row, 2, "-"),
                asString(row, 3, "-"),
                asString(row, 4, "-"),
                asDouble(row, 5),
                asDouble(row, 6),
                asDouble(row, 7),
                null,
                null,
                null,
                "-",
                asInteger(row, 8, null),
                asString(row, 9, "-")
        );
    }

    private ScoreManagementService.ScoreEntry mapScoreEntry(Object[] row) {
        return new ScoreManagementService.ScoreEntry(
                asInteger(row, 0, null),
                asInteger(row, 1, null),
                asInteger(row, 2, null),
                asString(row, 3, "-"),
                asDouble(row, 4),
                asString(row, 5, ""),
                asString(row, 6, "")
        );
    }

    private List<ScoreManagementService.ScoreRow> findRowsBySearch(ScoreSearch search) {
        String q = normalize(search == null ? null : search.getQ());
        Integer khoi = parseInteger(search == null ? null : search.getKhoi());
        String lop = normalize(search == null ? null : search.getLop());
        String mon = normalize(search == null ? null : search.getMon());
        Integer hocKy = parseInteger(search == null ? null : search.getHocKy());
        String khoa = normalize(search == null ? null : search.getKhoa());

        List<ScoreManagementService.ScoreRow> rows = scoreDAO.searchForManagement(q, khoi, lop, mon, hocKy, khoa).stream()
                .map(this::mapRow)
                .toList();

        if (hocKy != null && hocKy == 0) {
            return mergeAnnualRows(rows);
        }
        return rows;
    }

    private Comparator<ScoreManagementService.ScoreRow> buildExportComparator() {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        collator.setStrength(Collator.PRIMARY);

        return (left, right) -> {
            int byName = collator.compare(
                    safeText(left == null ? null : left.getTenHocSinh()),
                    safeText(right == null ? null : right.getTenHocSinh())
            );
            if (byName != 0) {
                return byName;
            }

            int byId = collator.compare(
                    safeText(left == null ? null : left.getIdHocSinh()),
                    safeText(right == null ? null : right.getIdHocSinh())
            );
            if (byId != 0) {
                return byId;
            }

            int byClass = collator.compare(
                    safeText(left == null ? null : left.getTenLop()),
                    safeText(right == null ? null : right.getTenLop())
            );
            if (byClass != 0) {
                return byClass;
            }

            int bySubject = collator.compare(
                    safeText(left == null ? null : left.getTenMon()),
                    safeText(right == null ? null : right.getTenMon())
            );
            if (bySubject != 0) {
                return bySubject;
            }

            Integer leftSemester = left == null ? null : left.getHocKy();
            Integer rightSemester = right == null ? null : right.getHocKy();
            if (leftSemester != null && rightSemester != null) {
                return Integer.compare(leftSemester, rightSemester);
            }
            if (leftSemester == null && rightSemester != null) {
                return 1;
            }
            if (leftSemester != null) {
                return -1;
            }

            return collator.compare(
                    safeText(left == null ? null : left.getNamHoc()),
                    safeText(right == null ? null : right.getNamHoc())
            );
        };
    }

    private List<ScoreManagementService.ScoreRow> mergeAnnualRows(List<ScoreManagementService.ScoreRow> rows) {
        Map<String, AnnualScoreAccumulator> grouped = new LinkedHashMap<>();
        for (ScoreManagementService.ScoreRow row : rows) {
            String key = buildAnnualKey(row);
            AnnualScoreAccumulator accumulator = grouped.computeIfAbsent(key, ignored -> new AnnualScoreAccumulator(row));
            accumulator.accept(row);
        }
        return grouped.values().stream()
                .map(this::toAnnualRow)
                .toList();
    }

    private ScoreManagementService.ScoreRow toAnnualRow(AnnualScoreAccumulator accumulator) {
        Double hk1 = accumulator.tongKetHocKy1;
        Double hk2 = accumulator.tongKetHocKy2;
        Double caNam = accumulator.tongKetCaNamFromData;
        if (caNam == null && hk1 != null && hk2 != null) {
            caNam = roundOneDecimal((hk1 + 2 * hk2) / 3.0);
        }

        return new ScoreManagementService.ScoreRow(
                accumulator.idHocSinh,
                accumulator.tenHocSinh,
                accumulator.tenLop,
                accumulator.idMon,
                accumulator.tenMon,
                null,
                null,
                caNam,
                hk1,
                hk2,
                caNam,
                accumulator.resolveConduct(),
                0,
                accumulator.namHoc
        );
    }

    private String buildAnnualKey(ScoreManagementService.ScoreRow row) {
        return safeKey(row.getIdHocSinh())
                + "|"
                + safeKey(row.getIdMon())
                + "|"
                + safeKey(row.getNamHoc());
    }

    private String safeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String normalizeConduct(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "-".equals(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private long countByRange(List<Double> values, Double minInclusive, Double maxExclusive) {
        return values.stream()
                .filter(Objects::nonNull)
                .filter(value -> minInclusive == null || value >= minInclusive)
                .filter(value -> maxExclusive == null || value < maxExclusive)
                .count();
    }

    private ScoreManagementService.FilterOption mapClassOption(Object[] row) {
        String id = asString(row, 0, "");
        String tenLop = asString(row, 1, id);
        String khoi = asString(row, 2, "");
        String label = khoi.isBlank()
                ? tenLop
                : tenLop + " (Khối " + khoi + ")";
        return new ScoreManagementService.FilterOption(id, label);
    }

    private ScoreManagementService.FilterOption mapDefaultOption(Object[] row) {
        String id = asString(row, 0, "");
        String name = asString(row, 1, id);
        return new ScoreManagementService.FilterOption(id, name);
    }

    private ScoreManagementService.FilterOption mapCourseOption(Object[] row) {
        String id = asString(row, 0, "");
        String name = asString(row, 1, id);
        return new ScoreManagementService.FilterOption(id, id + " (" + name + ")");
    }

    private String asString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }

        String value = row[index].toString().trim();
        if (value.isEmpty()) {
            return fallback;
        }
        return value;
    }

    private Integer asInteger(Object[] row, int index, Integer fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }

        Object value = row[index];
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private Double asDouble(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }

        Object value = row[index];
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private long asLong(Object[] row, int index, long fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }

        Object value = row[index];
        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private double defaultNumber(Double value) {
        if (value == null) {
            return 0.0;
        }
        return value;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private final class AnnualScoreAccumulator {
        private final String idHocSinh;
        private final String tenHocSinh;
        private final String tenLop;
        private final String idMon;
        private final String tenMon;
        private final String namHoc;
        private Double tongKetHocKy1;
        private Double tongKetHocKy2;
        private Double tongKetCaNamFromData;
        private String hanhKiemHocKy1;
        private String hanhKiemHocKy2;
        private String hanhKiemCaNam;

        private AnnualScoreAccumulator(ScoreManagementService.ScoreRow baseRow) {
            this.idHocSinh = baseRow.getIdHocSinh();
            this.tenHocSinh = baseRow.getTenHocSinh();
            this.tenLop = baseRow.getTenLop();
            this.idMon = baseRow.getIdMon();
            this.tenMon = baseRow.getTenMon();
            this.namHoc = baseRow.getNamHoc();
        }

        private void accept(ScoreManagementService.ScoreRow row) {
            Integer semester = row.getHocKy();
            if (semester == null) {
                return;
            }

            String conduct = normalizeConduct(row.getHanhKiem());
            if (semester == 1) {
                if (tongKetHocKy1 == null) {
                    tongKetHocKy1 = row.getTongKet();
                }
                if (conduct != null) {
                    hanhKiemHocKy1 = conduct;
                }
                return;
            }
            if (semester == 2) {
                if (tongKetHocKy2 == null) {
                    tongKetHocKy2 = row.getTongKet();
                }
                if (conduct != null) {
                    hanhKiemHocKy2 = conduct;
                }
                return;
            }
            if (semester == 0) {
                if (tongKetCaNamFromData == null) {
                    tongKetCaNamFromData = row.getTongKet();
                }
                if (conduct != null) {
                    hanhKiemCaNam = conduct;
                }
            }
        }

        private String resolveConduct() {
            if (hanhKiemCaNam != null) {
                return hanhKiemCaNam;
            }
            if (hanhKiemHocKy2 != null) {
                return hanhKiemHocKy2;
            }
            if (hanhKiemHocKy1 != null) {
                return hanhKiemHocKy1;
            }
            return "-";
        }
    }
}
