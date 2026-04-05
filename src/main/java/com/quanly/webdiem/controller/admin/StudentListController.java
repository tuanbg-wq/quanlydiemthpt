package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.entity.ActivityLog;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.admin.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/admin/student")
public class StudentListController {

    private final StudentService studentService;
    private final ActivityLogService activityLogService;
    private final StudentPageModelHelper pageModelHelper;

    public StudentListController(StudentService studentService,
                                 ActivityLogService activityLogService,
                                 StudentPageModelHelper pageModelHelper) {
        this.studentService = studentService;
        this.activityLogService = activityLogService;
        this.pageModelHelper = pageModelHelper;
    }

    @GetMapping
    public String studentPage(@ModelAttribute("search") StudentSearch search,
                              @RequestParam(value = "historyMode", required = false) String historyMode,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              Model model) {
        pageModelHelper.applyBasePage(model, "Quản Lý Học Sinh");

        boolean showAllHistory = "all".equalsIgnoreCase(historyMode);
        model.addAttribute("showAllHistory", showAllHistory);

        List<Student> students = new ArrayList<>(studentService.search(search));
        sortStudentsByName(students);
        int pageSize = 6;
        int totalStudents = students.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalStudents / (double) pageSize));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = totalStudents == 0 ? 0 : (currentPage - 1) * pageSize;
        int toIndex = totalStudents == 0 ? 0 : Math.min(fromIndex + pageSize, totalStudents);
        List<Student> pagedStudents = totalStudents == 0
                ? List.of()
                : students.subList(fromIndex, toIndex);
        model.addAttribute("students", pagedStudents);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("hasPrevPage", currentPage > 1);
        model.addAttribute("hasNextPage", currentPage < totalPages);

        pageModelHelper.applyListFilters(model);

        boolean showHistoryColumn =
                search.getHistoryType() != null && !search.getHistoryType().isBlank();
        model.addAttribute("showHistoryColumn", showHistoryColumn);

        List<String> studentIds = pagedStudents.stream()
                .map(Student::getIdHocSinh)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        int historyLimit = showAllHistory ? 120 : 6;
        List<ActivityLog> studentHistoryLogs = mergeStudentLogs(
                activityLogService.getStudentLogsByStudentIds(studentIds, historyLimit),
                activityLogService.getRecentStudentDeleteLogs(historyLimit),
                historyLimit
        );
        boolean hasMoreHistory = !showAllHistory && studentHistoryLogs.size() > 5;
        if (hasMoreHistory) {
            studentHistoryLogs = studentHistoryLogs.subList(0, 5);
        }
        model.addAttribute("studentHistoryLogs", studentHistoryLogs);
        model.addAttribute("hasMoreHistory", hasMoreHistory);

        Map<String, String> studentDisplayById = new LinkedHashMap<>();
        for (Student student : pagedStudents) {
            if (student.getIdHocSinh() == null || student.getIdHocSinh().isBlank()) {
                continue;
            }
            String name = student.getHoTen() == null || student.getHoTen().isBlank()
                    ? "(không rõ)"
                    : student.getHoTen().trim();
            studentDisplayById.put(student.getIdHocSinh(), name + " (" + student.getIdHocSinh() + ")");
        }
        model.addAttribute("studentDisplayById", studentDisplayById);

        return "admin/student";
    }

    private void sortStudentsByName(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return;
        }

        Collator viCollator = Collator.getInstance(new Locale("vi", "VN"));
        viCollator.setStrength(Collator.PRIMARY);

        students.sort(
                Comparator.comparing(
                                (Student student) -> extractGivenName(student == null ? null : student.getHoTen()),
                                viCollator
                        )
                        .thenComparing(
                                (Student student) -> extractNamePrefix(student == null ? null : student.getHoTen()),
                                viCollator
                        )
                        .thenComparing(
                                (Student student) -> normalizeForSort(student == null ? null : student.getHoTen()),
                                viCollator
                        )
                        .thenComparing((Student student) -> normalizeForSort(student == null ? null : student.getIdHocSinh()))
        );
    }

    private String normalizeForSort(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private String extractGivenName(String fullName) {
        String normalized = normalizeForSort(fullName).replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }
        int lastSpace = normalized.lastIndexOf(' ');
        return lastSpace < 0 ? normalized : normalized.substring(lastSpace + 1);
    }

    private String extractNamePrefix(String fullName) {
        String normalized = normalizeForSort(fullName).replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }
        int lastSpace = normalized.lastIndexOf(' ');
        return lastSpace < 0 ? "" : normalized.substring(0, lastSpace);
    }

    private List<ActivityLog> mergeStudentLogs(List<ActivityLog> primaryLogs,
                                               List<ActivityLog> deleteLogs,
                                               int limit) {
        int resolvedLimit = Math.max(1, limit);
        List<ActivityLog> merged = new ArrayList<>();
        Set<Integer> seenIds = new HashSet<>();

        addLogs(merged, seenIds, primaryLogs);
        addLogs(merged, seenIds, deleteLogs);

        merged.sort(
                Comparator
                        .comparing(ActivityLog::getThoiGian, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ActivityLog::getIdNhatKy, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        if (merged.size() <= resolvedLimit) {
            return merged;
        }
        return merged.subList(0, resolvedLimit);
    }

    private void addLogs(List<ActivityLog> target, Set<Integer> seenIds, List<ActivityLog> source) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (ActivityLog log : source) {
            if (log == null) {
                continue;
            }
            Integer logId = log.getIdNhatKy();
            if (logId != null && !seenIds.add(logId)) {
                continue;
            }
            target.add(log);
        }
    }
}
