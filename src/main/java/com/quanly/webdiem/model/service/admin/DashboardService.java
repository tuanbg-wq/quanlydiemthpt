package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.search.DashboardSearch;
import com.quanly.webdiem.model.search.ScoreSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final SubjectDAO subjectDAO;
    private final ClassDAO classDAO;
    private final ScoreManagementService scoreManagementService;
    private final ConductManagementService conductManagementService;
    private final ActivityLogService activityLogService;

    public DashboardService(StudentDAO studentDAO,
                            TeacherDAO teacherDAO,
                            SubjectDAO subjectDAO,
                            ClassDAO classDAO,
                            ScoreManagementService scoreManagementService,
                            ConductManagementService conductManagementService,
                            ActivityLogService activityLogService) {
        this.studentDAO = studentDAO;
        this.teacherDAO = teacherDAO;
        this.subjectDAO = subjectDAO;
        this.classDAO = classDAO;
        this.scoreManagementService = scoreManagementService;
        this.conductManagementService = conductManagementService;
        this.activityLogService = activityLogService;
    }

    public DashboardData getDashboardData(DashboardSearch search) {
        ScoreSearch scoreSearch = toScoreSearch(search);
        ConductSearch conductSearch = toConductSearch(search);

        long soHocSinh = 0L;
        long soGiaoVien = 0L;
        long soMonHoc = 0L;
        long soLop = 0L;
        ScoreManagementService.ScoreStats scoreStats = new ScoreManagementService.ScoreStats(0, 0, 0, 0, 0, 0, 0);
        ConductManagementService.ConductStats conductStats = new ConductManagementService.ConductStats(0, 0, 0, 0, 0);
        List<String> grades = List.of();
        List<ScoreManagementService.FilterOption> classOptions = List.of();
        List<ScoreManagementService.FilterOption> courseOptions = List.of();
        List<ActivityLogService.ConductActivityItem> activityItems = List.of();
        List<RecentStudentItem> recentStudents = List.of();

        try {
            soHocSinh = studentDAO.count();
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được tổng học sinh cho dashboard: {}", ex.getMessage());
        }
        try {
            soGiaoVien = teacherDAO.count();
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được tổng giáo viên cho dashboard: {}", ex.getMessage());
        }
        try {
            soMonHoc = subjectDAO.count();
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được tổng môn học cho dashboard: {}", ex.getMessage());
        }
        try {
            soLop = classDAO.count();
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được tổng lớp cho dashboard: {}", ex.getMessage());
        }

        try {
            scoreStats = scoreManagementService.getStats(scoreSearch);
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được thống kê điểm cho dashboard: {}", ex.getMessage());
        }
        try {
            grades = scoreManagementService.getGrades();
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được danh sách khối cho dashboard: {}", ex.getMessage());
        }
        try {
            classOptions = scoreManagementService.getClasses();
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được danh sách lớp cho dashboard: {}", ex.getMessage());
        }
        try {
            courseOptions = scoreManagementService.getCourses();
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được danh sách khóa cho dashboard: {}", ex.getMessage());
        }

        try {
            conductStats = conductManagementService.getStats(conductSearch);
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được thống kê khen thưởng/kỷ luật cho dashboard: {}", ex.getMessage());
        }
        try {
            activityItems = activityLogService.getRecentConductActivities(
                    search == null ? null : search.getQ(),
                    search == null ? null : search.getLoai(),
                    10
            );
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được hoạt động gần đây cho dashboard: {}", ex.getMessage());
        }
        try {
            recentStudents = studentDAO.findTop10ByOrderByNgayTaoDesc().stream()
                    .map(this::mapRecentStudent)
                    .toList();
        } catch (Exception ex) {
            LOGGER.warn("Không lấy được danh sách học sinh mới cho dashboard: {}", ex.getMessage());
        }

        return new DashboardData(
                soHocSinh,
                soGiaoVien,
                soMonHoc,
                soLop,
                scoreStats,
                conductStats,
                grades,
                classOptions,
                courseOptions,
                activityItems,
                recentStudents
        );
    }

    private ScoreSearch toScoreSearch(DashboardSearch search) {
        ScoreSearch scoreSearch = new ScoreSearch();
        if (search == null) {
            return scoreSearch;
        }
        scoreSearch.setQ(search.getQ());
        scoreSearch.setKhoi(search.getKhoi());
        scoreSearch.setLop(search.getLop());
        scoreSearch.setKhoa(search.getKhoa());
        return scoreSearch;
    }

    private ConductSearch toConductSearch(DashboardSearch search) {
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

    private RecentStudentItem mapRecentStudent(Student student) {
        if (student == null) {
            return new RecentStudentItem("-", "-", "-", "");
        }

        String id = firstNonBlank(student.getIdHocSinh(), "-");
        String hoTen = firstNonBlank(student.getHoTen(), "-");
        String tenLop = resolveClassDisplay(student.getLop());
        String thoiGianTao = firstNonBlank(student.getNgayTaoHienThi(), "");
        return new RecentStudentItem(id, hoTen, tenLop, thoiGianTao);
    }

    private String resolveClassDisplay(ClassEntity classEntity) {
        if (classEntity == null) {
            return "-";
        }

        String idLop = firstNonBlank(classEntity.getIdLop(), "");
        String tenLop = firstNonBlank(classEntity.getTenLop(), "");
        if (idLop.isBlank() && tenLop.isBlank()) {
            return "-";
        }
        if (idLop.isBlank()) {
            return tenLop;
        }
        if (tenLop.isBlank() || tenLop.equalsIgnoreCase(idLop)) {
            return idLop;
        }
        return idLop + " - " + tenLop;
    }

    private String firstNonBlank(String value, String fallback) {
        String trimmed = safeTrim(value);
        if (trimmed != null) {
            return trimmed;
        }
        return fallback;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static class DashboardData {
        private final long soHocSinh;
        private final long soGiaoVien;
        private final long soMonHoc;
        private final long soLop;
        private final ScoreManagementService.ScoreStats scoreStats;
        private final ConductManagementService.ConductStats conductStats;
        private final List<String> grades;
        private final List<ScoreManagementService.FilterOption> classOptions;
        private final List<ScoreManagementService.FilterOption> courseOptions;
        private final List<ActivityLogService.ConductActivityItem> activityItems;
        private final List<RecentStudentItem> recentStudents;

        public DashboardData(long soHocSinh,
                             long soGiaoVien,
                             long soMonHoc,
                             long soLop,
                             ScoreManagementService.ScoreStats scoreStats,
                             ConductManagementService.ConductStats conductStats,
                             List<String> grades,
                             List<ScoreManagementService.FilterOption> classOptions,
                             List<ScoreManagementService.FilterOption> courseOptions,
                             List<ActivityLogService.ConductActivityItem> activityItems,
                             List<RecentStudentItem> recentStudents) {
            this.soHocSinh = soHocSinh;
            this.soGiaoVien = soGiaoVien;
            this.soMonHoc = soMonHoc;
            this.soLop = soLop;
            this.scoreStats = scoreStats;
            this.conductStats = conductStats;
            this.grades = grades;
            this.classOptions = classOptions;
            this.courseOptions = courseOptions;
            this.activityItems = activityItems;
            this.recentStudents = recentStudents;
        }

        public long getSoHocSinh() {
            return soHocSinh;
        }

        public long getSoGiaoVien() {
            return soGiaoVien;
        }

        public long getSoMonHoc() {
            return soMonHoc;
        }

        public long getSoLop() {
            return soLop;
        }

        public ScoreManagementService.ScoreStats getScoreStats() {
            return scoreStats;
        }

        public ConductManagementService.ConductStats getConductStats() {
            return conductStats;
        }

        public List<String> getGrades() {
            return grades;
        }

        public List<ScoreManagementService.FilterOption> getClassOptions() {
            return classOptions;
        }

        public List<ScoreManagementService.FilterOption> getCourseOptions() {
            return courseOptions;
        }

        public List<ActivityLogService.ConductActivityItem> getActivityItems() {
            return activityItems;
        }

        public List<RecentStudentItem> getRecentStudents() {
            return recentStudents;
        }
    }

    public static class RecentStudentItem {
        private final String idHocSinh;
        private final String hoTen;
        private final String tenLop;
        private final String thoiGianTao;

        public RecentStudentItem(String idHocSinh, String hoTen, String tenLop, String thoiGianTao) {
            this.idHocSinh = idHocSinh;
            this.hoTen = hoTen;
            this.tenLop = tenLop;
            this.thoiGianTao = thoiGianTao;
        }

        public String getIdHocSinh() {
            return idHocSinh;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getTenLop() {
            return tenLop;
        }

        public String getThoiGianTao() {
            return thoiGianTao;
        }
    }
}
