package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.entity.StudentClassHistory;
import com.quanly.webdiem.model.search.AdminReportSearch;
import com.quanly.webdiem.model.service.admin.StudentClassHistoryService;
import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.admin.StudentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StudentListAdminReportHandler extends AbstractAdminReportTypeHandler {

    private final StudentService studentService;
    private final StudentClassHistoryService studentClassHistoryService;
    private final CourseDAO courseDAO;
    private final ClassDAO classDAO;

    public StudentListAdminReportHandler(StudentService studentService,
                                         StudentClassHistoryService studentClassHistoryService,
                                         CourseDAO courseDAO,
                                         ClassDAO classDAO) {
        this.studentService = studentService;
        this.studentClassHistoryService = studentClassHistoryService;
        this.courseDAO = courseDAO;
        this.classDAO = classDAO;
    }

    @Override
    public AdminReportType getType() {
        return AdminReportType.STUDENT_LIST;
    }

    @Override
    public AdminReportTypeResult buildResult(AdminReportSearch search) {
        StudentSearch studentSearch = mapSearch(search);
        String historyType = search == null ? null : search.getLichSuChuyen();
        boolean showTransferColumns = historyType != null && !historyType.isBlank();

        List<Student> students = new ArrayList<>(studentService.search(studentSearch));
        students = students.stream()
                .filter(this::isValidRow)
                .sorted(Comparator
                        .comparing((Student student) -> extractGivenNameForSort(student.getHoTen()))
                        .thenComparing(student -> extractNamePrefixForSort(student.getHoTen()))
                        .thenComparing(student -> normalizeSortValue(student.getHoTen()))
                        .thenComparing(student -> normalizeSortValue(student.getIdHocSinh()))
                )
                .toList();

        List<List<String>> previewRows = students.stream()
                .map(student -> toPreviewRow(student, historyType, showTransferColumns))
                .filter(this::hasMeaningfulCell)
                .toList();

        long withEmail = students.stream()
                .filter(item -> item.getEmail() != null && !item.getEmail().isBlank())
                .count();
        long withConduct = students.stream()
                .filter(item -> item.getHanhKiemTongHienThi() != null && !item.getHanhKiemTongHienThi().isBlank())
                .count();
        long classChangeCount = students.stream()
                .filter(item -> item.getHistoryDetail() != null && !item.getHistoryDetail().isBlank())
                .count();

        List<String> headers = new ArrayList<>(List.of(
                "Mã HS",
                "Họ và tên",
                "Mã lớp - Tên lớp",
                "Hạnh kiểm",
                "Địa chỉ",
                "Email",
                "Ngày nhập học"
        ));
        if (showTransferColumns) {
            headers.add("Chuyển từ -> đến");
            headers.add("Thời gian chuyển");
        }

        AdminReportPreview preview = new AdminReportPreview(
                List.of(
                        new AdminReportPreview.MetricItem("Tổng học sinh", String.valueOf(previewRows.size()), "neutral"),
                        new AdminReportPreview.MetricItem("Có email", String.valueOf(withEmail), "good"),
                        new AdminReportPreview.MetricItem("Có hạnh kiểm", String.valueOf(withConduct), "good"),
                        new AdminReportPreview.MetricItem("Có lịch sử chuyển", String.valueOf(classChangeCount), "warn")
                ),
                headers,
                previewRows,
                "Không có dữ liệu học sinh phù hợp.",
                previewRows.size()
        );

        AdminReportFilterBundle filters = new AdminReportFilterBundle(
                List.of(),
                List.of(),
                buildGradeOptions(),
                buildClassOptions(),
                List.of(),
                buildCourseOptions(),
                List.of(
                        option("", "-- Hạnh kiểm --"),
                        option("tot", "Tốt"),
                        option("kha", "Khá"),
                        option("trung_binh", "Trung bình"),
                        option("yeu", "Yếu"),
                        option("chua_co", "Chưa có")
                ),
                List.of(),
                List.of(
                        option("", "-- Lịch sử chuyển --"),
                        option("CHUYEN_LOP", "Chuyển lớp"),
                        option("CHUYEN_TRUONG", "Chuyển trường")
                ),
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
        addSummary(parts, "Từ khóa", search.getQ());
        addSummary(parts, "Khóa học", search.getKhoa());
        addSummary(parts, "Khối", search.getKhoi());
        addSummary(parts, "Lớp", search.getLop());
        addSummary(parts, "Hạnh kiểm", search.getHanhKiem());
        addSummary(parts, "Lịch sử chuyển", search.getLichSuChuyen());
        return parts.isEmpty() ? "Không dùng bộ lọc" : String.join(" | ", parts);
    }

    private void addSummary(List<String> parts, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String sanitizedValue = fallback(value);
        if ("-".equals(sanitizedValue)) {
            return;
        }
        parts.add(label + ": " + sanitizedValue);
    }

    private StudentSearch mapSearch(AdminReportSearch search) {
        StudentSearch studentSearch = new StudentSearch();
        if (search == null) {
            return studentSearch;
        }
        studentSearch.setQ(search.getQ());
        studentSearch.setCourseId(search.getKhoa());
        studentSearch.setKhoi(search.getKhoi());
        studentSearch.setClassId(search.getLop());
        studentSearch.setHanhKiem(search.getHanhKiem());
        studentSearch.setHistoryType(search.getLichSuChuyen());
        return studentSearch;
    }

    private List<AdminReportFilterOption> buildCourseOptions() {
        List<AdminReportFilterOption> options = new ArrayList<>();
        options.add(option("", "-- Chọn khóa học --"));
        List<Course> courses = new ArrayList<>(courseDAO.findAll());
        courses.sort(Comparator.comparing(Course::getIdKhoa, Comparator.nullsLast(String::compareToIgnoreCase)));
        for (Course course : courses) {
            if (course == null || course.getIdKhoa() == null || course.getIdKhoa().isBlank()) {
                continue;
            }
            String label = course.getIdKhoa();
            if (course.getTenKhoa() != null && !course.getTenKhoa().isBlank()) {
                label = course.getIdKhoa() + " - " + course.getTenKhoa().trim();
            }
            options.add(option(course.getIdKhoa(), label));
        }
        return options;
    }

    private List<AdminReportFilterOption> buildGradeOptions() {
        List<AdminReportFilterOption> options = new ArrayList<>();
        options.add(option("", "-- Chọn khối --"));
        List<Integer> grades = classDAO.findAll().stream()
                .map(ClassEntity::getKhoi)
                .filter(item -> item != null)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        for (Integer grade : grades) {
            options.add(option(String.valueOf(grade), "Khối " + grade));
        }
        return options;
    }

    private List<AdminReportFilterOption> buildClassOptions() {
        List<AdminReportFilterOption> options = new ArrayList<>();
        options.add(option("", "-- Chọn lớp --"));
        List<ClassEntity> classes = classDAO.findAll().stream()
                .sorted(Comparator.comparing(ClassEntity::getIdLop, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        for (ClassEntity classEntity : classes) {
            if (classEntity == null || classEntity.getIdLop() == null || classEntity.getIdLop().isBlank()) {
                continue;
            }
            options.add(option(classEntity.getIdLop(), classEntity.getMaVaTenLop()));
        }
        return options;
    }

    private List<String> toPreviewRow(Student student, String historyType, boolean showTransferColumns) {
        String classDisplay = student.getLop() == null ? "-" : student.getLop().getMaVaTenLop();
        String conduct = student.getHanhKiemTongHienThi();
        if (conduct == null || conduct.isBlank()) {
            conduct = "Chưa có";
        }

        List<String> row = new ArrayList<>(List.of(
                student.getIdHocSinh(),
                student.getHoTen(),
                classDisplay,
                conduct,
                student.getDiaChi(),
                student.getEmail(),
                student.getNgayNhapHocHienThi()
        ));

        if (showTransferColumns) {
            StudentClassHistory latestHistory = studentClassHistoryService.getLatestHistoryByType(
                    student.getIdHocSinh(),
                    historyType
            );
            row.add(buildTransferDetail(latestHistory));
            row.add(latestHistory == null ? "-" : fallback(latestHistory.getNgayChuyenHienThi()));
        }

        return sanitizeRow(row);
    }

    private String buildTransferDetail(StudentClassHistory history) {
        if (history == null) {
            return "-";
        }
        String type = normalize(history.getLoaiChuyen());
        if ("chuyen_truong".equals(type)) {
            return fallback(history.getTruongCu()) + " -> " + fallback(history.getTruongMoi());
        }
        return fallback(history.getLopCu()) + " -> " + fallback(history.getLopMoi());
    }

    private boolean isValidRow(Student student) {
        if (student == null) {
            return false;
        }
        String classDisplay = student.getLop() == null ? null : student.getLop().getMaVaTenLop();
        return !containsHeaderNoise(
                student.getAnh(),
                student.getIdHocSinh(),
                student.getHoTen(),
                classDisplay,
                student.getDiaChi(),
                student.getEmail(),
                student.getNgayNhapHocHienThi(),
                student.getHanhKiemTongHienThi()
        );
    }
}
