package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.entity.Subject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SubjectInfoService {

    private static final String META_TX_KEY = "So cot diem thuong xuyen";
    private static final String META_NOTE_KEY = "Ghi chu";
    private static final int DEFAULT_TX_COUNT = 3;

    private final SubjectDAO subjectDAO;
    private final CourseDAO courseDAO;

    public SubjectInfoService(SubjectDAO subjectDAO,
                              CourseDAO courseDAO) {
        this.subjectDAO = subjectDAO;
        this.courseDAO = courseDAO;
    }

    @Transactional(readOnly = true)
    public SubjectInfoView getSubjectInfo(String subjectId) {
        String normalizedSubjectId = normalizeSubjectId(subjectId);
        Subject subject = subjectDAO.findById(normalizedSubjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học."));

        Map<String, String> metadata = parseMetadata(subject.getMoTa());
        List<TeacherResponsibleItem> teachers = subjectDAO.findTeachersBySubjectId(normalizedSubjectId).stream()
                .map(this::mapTeacherItem)
                .toList();

        return new SubjectInfoView(
                orDash(subject.getIdMonHoc()),
                orDash(subject.getTenMonHoc()),
                buildCourseDisplay(subject.getIdKhoa()),
                orDash(subject.getNamHocApDung()),
                displayHocKy(subject.getHocKyApDung()),
                orDash(subject.getKhoiApDung()),
                orDash(subject.getToBoMon()),
                String.valueOf(resolveFrequentScoreCount(metadata)),
                orDash(subject.getIdGiaoVienPhuTrach()),
                orDash(resolveDescription(subject.getMoTa(), metadata)),
                subject.getNgayTao(),
                teachers
        );
    }

    private String normalizeSubjectId(String subjectId) {
        if (subjectId == null || subjectId.trim().isEmpty()) {
            throw new RuntimeException("Mã môn học không hợp lệ.");
        }
        return subjectId.trim().toUpperCase(Locale.ROOT);
    }

    private TeacherResponsibleItem mapTeacherItem(Object[] row) {
        String teacherId = asString(row, 0, "-");
        String teacherName = asString(row, 1, "-");
        String teacherEmail = asString(row, 2, "-");
        String teacherPhone = asString(row, 3, "-");
        String teacherStatus = displayTeacherStatus(asString(row, 4, "-"));

        return new TeacherResponsibleItem(
                teacherId,
                teacherName,
                teacherEmail,
                teacherPhone,
                teacherStatus
        );
    }

    private String buildCourseDisplay(String courseId) {
        String normalizedCourseId = normalize(courseId);
        if (normalizedCourseId == null) {
            return "-";
        }

        Course course = courseDAO.findById(normalizedCourseId).orElse(null);
        if (course == null || normalize(course.getTenKhoa()) == null) {
            return normalizedCourseId;
        }

        return normalizedCourseId + " - " + course.getTenKhoa();
    }

    private String displayHocKy(String hocKyRaw) {
        String hocKy = normalize(hocKyRaw);
        if (hocKy == null) {
            return "-";
        }

        if ("CA_NAM".equalsIgnoreCase(hocKy)) {
            return "Cả năm";
        }
        if ("HK1".equalsIgnoreCase(hocKy)) {
            return "Học kỳ 1";
        }
        if ("HK2".equalsIgnoreCase(hocKy)) {
            return "Học kỳ 2";
        }
        return hocKy;
    }

    private String displayTeacherStatus(String statusRaw) {
        String status = normalize(statusRaw);
        if (status == null) {
            return "-";
        }

        if ("dang_lam".equalsIgnoreCase(status)) {
            return "Đang công tác";
        }
        if ("nghi_viec".equalsIgnoreCase(status)) {
            return "Đã nghỉ";
        }
        return status;
    }

    private String resolveDescription(String currentDescription, Map<String, String> metadata) {
        String note = normalize(metadata.get(META_NOTE_KEY));
        if (note != null) {
            return note;
        }
        if (metadata.containsKey(META_TX_KEY)) {
            return null;
        }
        return normalize(currentDescription);
    }

    private int resolveFrequentScoreCount(Map<String, String> metadata) {
        String value = normalize(metadata.get(META_TX_KEY));
        if (value == null) {
            return DEFAULT_TX_COUNT;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= 2 && parsed <= 4) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // fall back to default.
        }
        return DEFAULT_TX_COUNT;
    }

    private Map<String, String> parseMetadata(String description) {
        Map<String, String> metadata = new LinkedHashMap<>();
        String normalizedDescription = normalize(description);
        if (normalizedDescription == null) {
            return metadata;
        }
        String[] lines = normalizedDescription.split("\\R");
        for (String line : lines) {
            if (line == null || !line.contains(":")) {
                continue;
            }
            String[] pair = line.split(":", 2);
            String key = normalize(pair[0]);
            String value = pair.length > 1 ? normalize(pair[1]) : null;
            if (key != null && value != null) {
                metadata.put(key, value);
            }
        }
        return metadata;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String orDash(String value) {
        String normalized = normalize(value);
        return normalized == null ? "-" : normalized;
    }

    private String asString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }

        String value = row[index].toString();
        String normalized = normalize(value);
        return normalized == null ? fallback : normalized;
    }

    public static class SubjectInfoView {
        private final String idMonHoc;
        private final String tenMonHoc;
        private final String khoaHoc;
        private final String namHoc;
        private final String hocKy;
        private final String khoiApDung;
        private final String toBoMon;
        private final String soDiemThuongXuyen;
        private final String giaoVienPhuTrachChinh;
        private final String moTa;
        private final LocalDateTime ngayTao;
        private final List<TeacherResponsibleItem> teacherList;

        public SubjectInfoView(String idMonHoc,
                               String tenMonHoc,
                               String khoaHoc,
                               String namHoc,
                               String hocKy,
                               String khoiApDung,
                               String toBoMon,
                               String soDiemThuongXuyen,
                               String giaoVienPhuTrachChinh,
                               String moTa,
                               LocalDateTime ngayTao,
                               List<TeacherResponsibleItem> teacherList) {
            this.idMonHoc = idMonHoc;
            this.tenMonHoc = tenMonHoc;
            this.khoaHoc = khoaHoc;
            this.namHoc = namHoc;
            this.hocKy = hocKy;
            this.khoiApDung = khoiApDung;
            this.toBoMon = toBoMon;
            this.soDiemThuongXuyen = soDiemThuongXuyen;
            this.giaoVienPhuTrachChinh = giaoVienPhuTrachChinh;
            this.moTa = moTa;
            this.ngayTao = ngayTao;
            this.teacherList = teacherList;
        }

        public String getIdMonHoc() {
            return idMonHoc;
        }

        public String getTenMonHoc() {
            return tenMonHoc;
        }

        public String getKhoaHoc() {
            return khoaHoc;
        }

        public String getNamHoc() {
            return namHoc;
        }

        public String getHocKy() {
            return hocKy;
        }

        public String getKhoiApDung() {
            return khoiApDung;
        }

        public String getToBoMon() {
            return toBoMon;
        }

        public String getSoDiemThuongXuyen() {
            return soDiemThuongXuyen;
        }

        public String getGiaoVienPhuTrachChinh() {
            return giaoVienPhuTrachChinh;
        }

        public String getMoTa() {
            return moTa;
        }

        public LocalDateTime getNgayTao() {
            return ngayTao;
        }

        public List<TeacherResponsibleItem> getTeacherList() {
            return teacherList;
        }
    }

    public static class TeacherResponsibleItem {
        private final String idGiaoVien;
        private final String hoTen;
        private final String email;
        private final String soDienThoai;
        private final String trangThai;

        public TeacherResponsibleItem(String idGiaoVien,
                                      String hoTen,
                                      String email,
                                      String soDienThoai,
                                      String trangThai) {
            this.idGiaoVien = idGiaoVien;
            this.hoTen = hoTen;
            this.email = email;
            this.soDienThoai = soDienThoai;
            this.trangThai = trangThai;
        }

        public String getIdGiaoVien() {
            return idGiaoVien;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getEmail() {
            return email;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public String getTrangThai() {
            return trangThai;
        }
    }
}
