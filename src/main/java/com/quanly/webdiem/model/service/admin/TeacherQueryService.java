package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.TeacherListItem;
import com.quanly.webdiem.model.entity.TeacherSearch;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
public class TeacherQueryService {

    private static final int PAGE_SIZE = 6;

    private final TeacherDAO teacherDAO;

    public TeacherQueryService(TeacherDAO teacherDAO) {
        this.teacherDAO = teacherDAO;
    }

    public TeacherService.TeacherPageResult search(TeacherSearch search) {
        String q = normalize(search == null ? null : search.getQ());
        String boMon = normalize(search == null ? null : search.getBoMon());
        String khoi = normalize(search == null ? null : search.getKhoi());
        String trangThai = normalize(search == null ? null : search.getTrangThai());

        List<TeacherListItem> rows = teacherDAO.searchForManagement(q, boMon, khoi, trangThai).stream()
                .map(this::mapRow)
                .toList();

        int requestedPage = normalizePage(search == null ? null : search.getPage());
        return paginate(rows, requestedPage);
    }

    public List<String> getSubjects() {
        return teacherDAO.findDistinctSubjects();
    }

    public List<String> getGrades() {
        List<String> grades = teacherDAO.findDistinctGrades().stream()
                .map(String::valueOf)
                .toList();
        if (!grades.isEmpty()) {
            return grades;
        }
        return List.of("10", "11", "12");
    }

    public List<String> getStatuses() {
        LinkedHashSet<String> statuses = new LinkedHashSet<>();
        statuses.add("dang_lam");
        statuses.add("nghi_viec");

        teacherDAO.findDistinctStatuses().stream()
                .map(this::normalize)
                .filter(value -> value != null)
                .forEach(statuses::add);

        return statuses.stream().toList();
    }

    private TeacherService.TeacherPageResult paginate(List<TeacherListItem> rows, int requestedPage) {
        int totalItems = rows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        int fromIndex = Math.max(0, (page - 1) * PAGE_SIZE);
        int toIndex = Math.min(totalItems, fromIndex + PAGE_SIZE);

        List<TeacherListItem> items = totalItems == 0
                ? Collections.emptyList()
                : rows.subList(fromIndex, toIndex);

        int fromRecord = totalItems == 0 ? 0 : fromIndex + 1;
        int toRecord = totalItems == 0 ? 0 : toIndex;

        return new TeacherService.TeacherPageResult(
                items,
                page,
                totalPages,
                totalItems,
                fromRecord,
                toRecord
        );
    }

    private TeacherListItem mapRow(Object[] row) {
        return new TeacherListItem(
                asString(row, 0, "-"),
                asString(row, 1, "-"),
                asString(row, 2, "-"),
                displayGender(asString(row, 3, "-")),
                asString(row, 4, "-"),
                asString(row, 5, "-"),
                asString(row, 6, "-"),
                asString(row, 7, "-"),
                asString(row, 8, "-"),
                displayRole(asString(row, 9, "-")),
                asString(row, 10, "-"),
                "",
                asString(row, 11, "")
        );
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

    private String displayGender(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return "-";
        }

        if ("nu".equalsIgnoreCase(value)) {
            return "Nữ";
        }

        if ("nam".equalsIgnoreCase(value)) {
            return "Nam";
        }

        return value;
    }

    private String displayRole(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return "-";
        }

        if ("admin".equalsIgnoreCase(value)) {
            return "Admin";
        }

        if ("gvcn".equalsIgnoreCase(value)) {
            return "Giáo viên chủ nhiệm";
        }

        if ("gvbm".equalsIgnoreCase(value)) {
            return "Giáo viên bộ môn";
        }

        if ("giao_vien".equalsIgnoreCase(value)) {
            return "Giáo viên";
        }

        if ("hoc_sinh".equalsIgnoreCase(value)) {
            return "Học sinh";
        }

        return value.replace('_', ' ');
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

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }
}
