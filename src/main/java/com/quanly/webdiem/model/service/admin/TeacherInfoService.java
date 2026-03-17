package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.Teacher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TeacherInfoService {

    private final TeacherDAO teacherDAO;

    public TeacherInfoService(TeacherDAO teacherDAO) {
        this.teacherDAO = teacherDAO;
    }

    @Transactional(readOnly = true)
    public TeacherInfoView getTeacherInfo(String teacherId) {
        String normalizedTeacherId = normalizeTeacherId(teacherId);
        Teacher teacher = teacherDAO.findById(normalizedTeacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên."));

        List<RoleHistoryItem> roleHistory = teacherDAO.findRoleHistoryByTeacherId(normalizedTeacherId).stream()
                .map(this::mapRoleHistory)
                .toList();

        List<YearClassHistoryItem> homeroomHistory = teacherDAO.findHomeroomHistoryByTeacherId(normalizedTeacherId).stream()
                .map(this::mapYearClassHistory)
                .toList();

        List<SubjectAssignmentHistoryItem> subjectAssignmentHistory = teacherDAO
                .findSubjectAssignmentHistoryByTeacherId(normalizedTeacherId).stream()
                .map(this::mapSubjectAssignmentHistory)
                .toList();
        List<WorkHistoryItem> workHistory = buildWorkHistory(roleHistory, homeroomHistory, subjectAssignmentHistory);

        String currentRole = roleHistory.isEmpty() ? "-" : roleHistory.get(0).getRoleName();
        String roleSchoolYear = roleHistory.isEmpty() ? "-" : roleHistory.get(0).getSchoolYear();
        String currentSubjectClasses = subjectAssignmentHistory.isEmpty() ? "-" : subjectAssignmentHistory.get(0).getClassNames();

        return new TeacherInfoView(
                teacher.getIdGiaoVien(),
                orDash(teacher.getHoTen()),
                teacher.getNgaySinh(),
                displayGender(teacher.getGioiTinh()),
                orDash(teacher.getSoDienThoai()),
                orDash(teacher.getEmail()),
                orDash(teacher.getDiaChi()),
                orDash(teacher.getChuyenMon()),
                orDash(teacher.getTrinhDo()),
                teacher.getNgayVaoLam(),
                displayStatus(teacher.getTrangThai()),
                orDash(teacher.getGhiChu()),
                normalizeAvatarPath(teacher.getAnh()),
                currentRole,
                roleSchoolYear,
                currentSubjectClasses,
                workHistory,
                roleHistory,
                homeroomHistory,
                subjectAssignmentHistory
        );
    }

    private String normalizeTeacherId(String teacherId) {
        if (teacherId == null || teacherId.trim().isEmpty()) {
            throw new RuntimeException("Mã giáo viên không hợp lệ.");
        }
        return teacherId.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeAvatarPath(String avatarPath) {
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            return null;
        }
        String normalized = avatarPath.trim();
        if (normalized.startsWith("/")) {
            return normalized;
        }
        return "/uploads/" + normalized;
    }

    private String displayGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return "-";
        }

        if ("nu".equalsIgnoreCase(gender)) {
            return "Nữ";
        }

        if ("nam".equalsIgnoreCase(gender)) {
            return "Nam";
        }

        if ("khac".equalsIgnoreCase(gender)) {
            return "Khác";
        }

        return gender;
    }

    private String displayStatus(String status) {
        if (status == null || status.isBlank()) {
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

    private String orDash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }

    private RoleHistoryItem mapRoleHistory(Object[] row) {
        return new RoleHistoryItem(
                orDash(asString(row, 0)),
                orDash(asString(row, 1))
        );
    }

    private YearClassHistoryItem mapYearClassHistory(Object[] row) {
        return new YearClassHistoryItem(
                orDash(asString(row, 0)),
                orDash(asString(row, 1))
        );
    }

    private SubjectAssignmentHistoryItem mapSubjectAssignmentHistory(Object[] row) {
        return new SubjectAssignmentHistoryItem(
                orDash(asString(row, 0)),
                orDash(asString(row, 1)),
                orDash(asString(row, 2))
        );
    }

    private List<WorkHistoryItem> buildWorkHistory(List<RoleHistoryItem> roleHistory,
                                                   List<YearClassHistoryItem> homeroomHistory,
                                                   List<SubjectAssignmentHistoryItem> subjectAssignmentHistory) {
        Map<String, WorkHistoryBuilder> rows = new LinkedHashMap<>();

        for (RoleHistoryItem roleItem : roleHistory) {
            if (roleItem == null) {
                continue;
            }
            WorkHistoryBuilder row = rows.computeIfAbsent(roleItem.getSchoolYear(), WorkHistoryBuilder::new);
            row.addRole(roleItem.getRoleName());
        }

        for (YearClassHistoryItem homeroomItem : homeroomHistory) {
            if (homeroomItem == null) {
                continue;
            }
            WorkHistoryBuilder row = rows.computeIfAbsent(homeroomItem.getSchoolYear(), WorkHistoryBuilder::new);
            row.setHomeroomClasses(homeroomItem.getClassNames());
        }

        for (SubjectAssignmentHistoryItem subjectItem : subjectAssignmentHistory) {
            if (subjectItem == null) {
                continue;
            }
            WorkHistoryBuilder row = rows.computeIfAbsent(subjectItem.getSchoolYear(), WorkHistoryBuilder::new);
            row.setSubjectNames(subjectItem.getSubjectNames());
            row.setSubjectClassNames(subjectItem.getClassNames());
        }

        return rows.values().stream()
                .map(WorkHistoryBuilder::build)
                .sorted((a, b) -> compareSchoolYearDesc(a.getSchoolYear(), b.getSchoolYear()))
                .toList();
    }

    private int compareSchoolYearDesc(String schoolYearA, String schoolYearB) {
        int aStart = extractStartYear(schoolYearA);
        int bStart = extractStartYear(schoolYearB);
        if (aStart != bStart) {
            return Integer.compare(bStart, aStart);
        }
        return schoolYearB.compareToIgnoreCase(schoolYearA);
    }

    private int extractStartYear(String schoolYear) {
        if (schoolYear == null || schoolYear.length() < 4) {
            return 0;
        }
        try {
            return Integer.parseInt(schoolYear.substring(0, 4));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String asString(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }
        return row[index].toString();
    }

    public static class TeacherInfoView {
        private final String idGiaoVien;
        private final String hoTen;
        private final LocalDate ngaySinh;
        private final String gioiTinh;
        private final String soDienThoai;
        private final String email;
        private final String diaChi;
        private final String chuyenMon;
        private final String trinhDo;
        private final LocalDate ngayVaoLam;
        private final String trangThai;
        private final String ghiChu;
        private final String avatar;
        private final String currentRole;
        private final String currentRoleSchoolYear;
        private final String currentSubjectClasses;
        private final List<WorkHistoryItem> workHistory;
        private final List<RoleHistoryItem> roleHistory;
        private final List<YearClassHistoryItem> homeroomHistory;
        private final List<SubjectAssignmentHistoryItem> subjectAssignmentHistory;

        public TeacherInfoView(String idGiaoVien,
                               String hoTen,
                               LocalDate ngaySinh,
                               String gioiTinh,
                               String soDienThoai,
                               String email,
                               String diaChi,
                               String chuyenMon,
                               String trinhDo,
                               LocalDate ngayVaoLam,
                               String trangThai,
                               String ghiChu,
                               String avatar,
                               String currentRole,
                               String currentRoleSchoolYear,
                               String currentSubjectClasses,
                               List<WorkHistoryItem> workHistory,
                               List<RoleHistoryItem> roleHistory,
                               List<YearClassHistoryItem> homeroomHistory,
                               List<SubjectAssignmentHistoryItem> subjectAssignmentHistory) {
            this.idGiaoVien = idGiaoVien;
            this.hoTen = hoTen;
            this.ngaySinh = ngaySinh;
            this.gioiTinh = gioiTinh;
            this.soDienThoai = soDienThoai;
            this.email = email;
            this.diaChi = diaChi;
            this.chuyenMon = chuyenMon;
            this.trinhDo = trinhDo;
            this.ngayVaoLam = ngayVaoLam;
            this.trangThai = trangThai;
            this.ghiChu = ghiChu;
            this.avatar = avatar;
            this.currentRole = currentRole;
            this.currentRoleSchoolYear = currentRoleSchoolYear;
            this.currentSubjectClasses = currentSubjectClasses;
            this.workHistory = workHistory;
            this.roleHistory = roleHistory;
            this.homeroomHistory = homeroomHistory;
            this.subjectAssignmentHistory = subjectAssignmentHistory;
        }

        public String getIdGiaoVien() {
            return idGiaoVien;
        }

        public String getHoTen() {
            return hoTen;
        }

        public LocalDate getNgaySinh() {
            return ngaySinh;
        }

        public String getGioiTinh() {
            return gioiTinh;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public String getEmail() {
            return email;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public String getChuyenMon() {
            return chuyenMon;
        }

        public String getTrinhDo() {
            return trinhDo;
        }

        public LocalDate getNgayVaoLam() {
            return ngayVaoLam;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getCurrentRole() {
            return currentRole;
        }

        public String getCurrentRoleSchoolYear() {
            return currentRoleSchoolYear;
        }

        public String getCurrentSubjectClasses() {
            return currentSubjectClasses;
        }

        public List<WorkHistoryItem> getWorkHistory() {
            return workHistory;
        }

        public List<RoleHistoryItem> getRoleHistory() {
            return roleHistory;
        }

        public List<YearClassHistoryItem> getHomeroomHistory() {
            return homeroomHistory;
        }

        public List<SubjectAssignmentHistoryItem> getSubjectAssignmentHistory() {
            return subjectAssignmentHistory;
        }
    }

    public static class YearClassHistoryItem {
        private final String schoolYear;
        private final String classNames;

        public YearClassHistoryItem(String schoolYear, String classNames) {
            this.schoolYear = schoolYear;
            this.classNames = classNames;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public String getClassNames() {
            return classNames;
        }
    }

    public static class WorkHistoryItem {
        private final String schoolYear;
        private final String roleName;
        private final String homeroomClasses;
        private final String subjectClassNames;
        private final String subjectNames;

        public WorkHistoryItem(String schoolYear,
                               String roleName,
                               String homeroomClasses,
                               String subjectClassNames,
                               String subjectNames) {
            this.schoolYear = schoolYear;
            this.roleName = roleName;
            this.homeroomClasses = homeroomClasses;
            this.subjectClassNames = subjectClassNames;
            this.subjectNames = subjectNames;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public String getRoleName() {
            return roleName;
        }

        public String getHomeroomClasses() {
            return homeroomClasses;
        }

        public String getSubjectClassNames() {
            return subjectClassNames;
        }

        public String getSubjectNames() {
            return subjectNames;
        }
    }

    public static class SubjectAssignmentHistoryItem {
        private final String schoolYear;
        private final String subjectNames;
        private final String classNames;

        public SubjectAssignmentHistoryItem(String schoolYear, String subjectNames, String classNames) {
            this.schoolYear = schoolYear;
            this.subjectNames = subjectNames;
            this.classNames = classNames;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public String getSubjectNames() {
            return subjectNames;
        }

        public String getClassNames() {
            return classNames;
        }
    }

    public static class RoleHistoryItem {
        private final String schoolYear;
        private final String roleName;

        public RoleHistoryItem(String schoolYear, String roleName) {
            this.schoolYear = schoolYear;
            this.roleName = roleName;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public String getRoleName() {
            return roleName;
        }
    }

    private static class WorkHistoryBuilder {
        private final String schoolYear;
        private final List<String> roles = new ArrayList<>();
        private String homeroomClasses = "-";
        private String subjectClassNames = "-";
        private String subjectNames = "-";

        private WorkHistoryBuilder(String schoolYear) {
            this.schoolYear = schoolYear;
        }

        private void addRole(String roleName) {
            if (roleName == null || roleName.isBlank() || "-".equals(roleName)) {
                return;
            }
            if (!roles.contains(roleName)) {
                roles.add(roleName);
            }
        }

        private void setHomeroomClasses(String homeroomClasses) {
            if (homeroomClasses != null && !homeroomClasses.isBlank() && !"-".equals(homeroomClasses)) {
                this.homeroomClasses = homeroomClasses;
            }
        }

        private void setSubjectClassNames(String subjectClassNames) {
            if (subjectClassNames != null && !subjectClassNames.isBlank() && !"-".equals(subjectClassNames)) {
                this.subjectClassNames = subjectClassNames;
            }
        }

        private void setSubjectNames(String subjectNames) {
            if (subjectNames != null && !subjectNames.isBlank() && !"-".equals(subjectNames)) {
                this.subjectNames = subjectNames;
            }
        }

        private WorkHistoryItem build() {
            String mergedRole = roles.isEmpty() ? "-" : String.join(", ", roles);
            return new WorkHistoryItem(
                    schoolYear == null || schoolYear.isBlank() ? "-" : schoolYear,
                    mergedRole,
                    homeroomClasses,
                    subjectClassNames,
                    subjectNames
            );
        }
    }
}
