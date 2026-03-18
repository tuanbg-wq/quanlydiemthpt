package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.entity.ClassSearch;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class ClassManagementQueryService {

    private static final int PAGE_SIZE = 10;

    private final ClassDAO classDAO;
    private final CourseDAO courseDAO;

    public ClassManagementQueryService(ClassDAO classDAO,
                                       CourseDAO courseDAO) {
        this.classDAO = classDAO;
        this.courseDAO = courseDAO;
    }

    public ClassManagementService.ClassPageResult search(ClassSearch search) {
        String q = normalize(search == null ? null : search.getQ());
        String khoi = normalize(search == null ? null : search.getKhoi());
        String khoa = normalize(search == null ? null : search.getKhoa());

        List<ClassManagementService.ClassRow> rows = classDAO.searchForManagement(q, khoi, khoa).stream()
                .map(this::mapRow)
                .toList();

        int requestedPage = normalizePage(search == null ? null : search.getPage());
        return paginate(rows, requestedPage);
    }

    public ClassManagementService.ClassStats getStats() {
        return new ClassManagementService.ClassStats(
                classDAO.countAllClasses(),
                classDAO.sumAllClassSizes(),
                classDAO.countDistinctHomeroomTeachers()
        );
    }

    public List<String> getGrades() {
        return List.of("10", "11", "12");
    }

    public List<ClassManagementService.CourseOption> getCourses() {
        return courseDAO.findAll(Sort.by(Sort.Direction.ASC, "idKhoa")).stream()
                .map(course -> new ClassManagementService.CourseOption(
                        safeText(course.getIdKhoa(), ""),
                        safeText(course.getTenKhoa(), safeText(course.getIdKhoa(), ""))
                ))
                .toList();
    }

    private ClassManagementService.ClassPageResult paginate(List<ClassManagementService.ClassRow> rows, int requestedPage) {
        int totalItems = rows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        int fromIndex = Math.max(0, (page - 1) * PAGE_SIZE);
        int toIndex = Math.min(totalItems, fromIndex + PAGE_SIZE);

        List<ClassManagementService.ClassRow> items = totalItems == 0
                ? Collections.emptyList()
                : rows.subList(fromIndex, toIndex);

        int fromRecord = totalItems == 0 ? 0 : fromIndex + 1;
        int toRecord = totalItems == 0 ? 0 : toIndex;

        return new ClassManagementService.ClassPageResult(
                items,
                page,
                totalPages,
                totalItems,
                fromRecord,
                toRecord
        );
    }

    private ClassManagementService.ClassRow mapRow(Object[] row) {
        return new ClassManagementService.ClassRow(
                asString(row, 0, "-"),
                asString(row, 1, "-"),
                asString(row, 2, "-"),
                asString(row, 3, "-"),
                asString(row, 4, "-"),
                asString(row, 5, "-"),
                asString(row, 6, ""),
                asInteger(row, 7, 0),
                asString(row, 8, "-")
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

    private int asInteger(Object[] row, int index, int fallback) {
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

    private String safeText(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
