package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ScoreDAO;
import com.quanly.webdiem.model.entity.ScoreSearch;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class ScoreQueryService {

    private static final int PAGE_SIZE = 10;

    private final ScoreDAO scoreDAO;

    public ScoreQueryService(ScoreDAO scoreDAO) {
        this.scoreDAO = scoreDAO;
    }

    public ScoreManagementService.ScorePageResult search(ScoreSearch search) {
        String q = normalize(search == null ? null : search.getQ());
        Integer khoi = parseInteger(search == null ? null : search.getKhoi());
        String lop = normalize(search == null ? null : search.getLop());
        String mon = normalize(search == null ? null : search.getMon());
        Integer hocKy = parseInteger(search == null ? null : search.getHocKy());
        String khoa = normalize(search == null ? null : search.getKhoa());

        List<ScoreManagementService.ScoreRow> rows = scoreDAO.searchForManagement(q, khoi, lop, mon, hocKy, khoa)
                .stream()
                .map(this::mapRow)
                .toList();

        int requestedPage = normalizePage(search == null ? null : search.getPage());
        return paginate(rows, requestedPage);
    }

    public ScoreManagementService.ScoreStats getStats() {
        long totalStudentsWithScores = scoreDAO.countDistinctStudentsWithScores();
        double schoolAverage = defaultNumber(scoreDAO.calculateSchoolAverage());
        long totalGroups = scoreDAO.countScoreGroups();
        long goodGroups = scoreDAO.countGoodScoreGroups();
        double goodRate = totalGroups == 0 ? 0.0 : (goodGroups * 100.0 / totalGroups);

        return new ScoreManagementService.ScoreStats(
                totalStudentsWithScores,
                roundOneDecimal(schoolAverage),
                roundOneDecimal(goodRate)
        );
    }

    public List<String> getGrades() {
        return scoreDAO.findDistinctGrades().stream()
                .map(String::valueOf)
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
                asString(row, 5, "-")
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
                asString(row, 8, "-"),
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

    private ScoreManagementService.FilterOption mapClassOption(Object[] row) {
        String id = asString(row, 0, "");
        String tenLop = asString(row, 1, id);
        String khoi = asString(row, 2, "");
        String label = khoi.isBlank()
                ? tenLop
                : tenLop + " (Kh\u1ed1i " + khoi + ")";
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
}
