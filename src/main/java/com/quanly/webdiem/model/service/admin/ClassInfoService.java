package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.StudentClassHistoryDAO;
import com.quanly.webdiem.model.dao.StudentDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ClassInfoService {

    private static final String ERROR_CLASS_ID_INVALID = "Mã lớp học không hợp lệ.";
    private static final String ERROR_CLASS_NOT_FOUND = "Không tìm thấy lớp học.";
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClassDAO classDAO;
    private final StudentDAO studentDAO;
    private final StudentClassHistoryDAO studentClassHistoryDAO;

    public ClassInfoService(ClassDAO classDAO,
                            StudentDAO studentDAO,
                            StudentClassHistoryDAO studentClassHistoryDAO) {
        this.classDAO = classDAO;
        this.studentDAO = studentDAO;
        this.studentClassHistoryDAO = studentClassHistoryDAO;
    }

    @Transactional(readOnly = true)
    public ClassInfoView getClassInfo(String classId) {
        String normalizedClassId = normalizeUpper(classId);
        if (normalizedClassId == null) {
            throw new RuntimeException(ERROR_CLASS_ID_INVALID);
        }

        Object[] classRow = classDAO.findClassInfoById(normalizedClassId).stream()
                .findFirst()
                .map(this::normalizeRow)
                .orElseThrow(() -> new RuntimeException(ERROR_CLASS_NOT_FOUND));

        List<ClassStudentItem> students = studentDAO.findStudentsByClassId(normalizedClassId).stream()
                .map(this::mapStudentItem)
                .toList();

        List<ClassTransferHistoryItem> transferHistory = studentClassHistoryDAO.findClassTransferHistory(normalizedClassId).stream()
                .map(row -> mapTransferHistoryItem(row, normalizedClassId))
                .toList();

        int maleCount = 0;
        int femaleCount = 0;
        for (ClassStudentItem student : students) {
            GenderType genderType = toGenderType(student.getGioiTinhRaw());
            if (genderType == GenderType.MALE) {
                maleCount++;
            } else if (genderType == GenderType.FEMALE) {
                femaleCount++;
            }
        }

        String idLop = asString(classRow, 0, "-");
        String tenLop = asString(classRow, 1, idLop);
        Integer khoi = asInteger(classRow, 2, null);
        String namHoc = asString(classRow, 3, "-");
        int siSo = asInteger(classRow, 4, 0);
        String ghiChu = asString(classRow, 5, "");

        String idGvcn = asString(classRow, 6, "");
        String gvcnTen = asString(classRow, 7, "-");
        String gvcnEmail = asString(classRow, 8, "");
        String gvcnPhone = asString(classRow, 9, "");
        String gvcnAvatar = asString(classRow, 10, "");

        String idKhoa = asString(classRow, 11, "");
        String tenKhoa = asString(classRow, 12, idKhoa);

        return new ClassInfoView(
                idLop,
                tenLop,
                khoi,
                namHoc,
                siSo,
                ghiChu,
                idKhoa,
                tenKhoa,
                idGvcn,
                gvcnTen,
                gvcnEmail,
                gvcnPhone,
                gvcnAvatar,
                students,
                transferHistory,
                students.size(),
                maleCount,
                femaleCount
        );
    }

    private ClassStudentItem mapStudentItem(Object[] row) {
        Object[] normalizedRow = normalizeRow(row);
        String studentId = asString(normalizedRow, 0, "-");
        String studentName = asString(normalizedRow, 1, studentId);
        String gender = asString(normalizedRow, 2, "-");
        String email = asString(normalizedRow, 3, "-");
        String avatar = asString(normalizedRow, 4, "");
        LocalDate ngayNhapHoc = asLocalDate(normalizedRow, 5);
        String trangThai = asString(normalizedRow, 6, "-");

        return new ClassStudentItem(
                studentId,
                studentName,
                gender,
                email,
                avatar,
                ngayNhapHoc,
                trangThai
        );
    }

    private ClassTransferHistoryItem mapTransferHistoryItem(Object[] row, String currentClassId) {
        Object[] normalizedRow = normalizeRow(row);
        String studentId = asString(normalizedRow, 0, "-");
        String studentName = asString(normalizedRow, 1, studentId);
        String lopCu = asString(normalizedRow, 2, "-");
        String lopMoi = asString(normalizedRow, 3, "-");
        LocalDate ngayChuyen = asLocalDate(normalizedRow, 4);
        String loaiChuyen = asString(normalizedRow, 5, "-");
        String ghiChu = asString(normalizedRow, 6, "");

        TransferDirection direction = detectTransferDirection(currentClassId, lopCu, lopMoi, loaiChuyen);
        return new ClassTransferHistoryItem(
                studentId,
                studentName,
                lopCu,
                lopMoi,
                ngayChuyen,
                direction.code,
                direction.display,
                ghiChu
        );
    }

    private TransferDirection detectTransferDirection(String classId,
                                                      String lopCu,
                                                      String lopMoi,
                                                      String loaiChuyen) {
        boolean isFromClass = equalsIgnoreCase(classId, lopCu);
        boolean isToClass = equalsIgnoreCase(classId, lopMoi);
        String normalizedType = normalizeUpper(loaiChuyen);

        if (isToClass && !isFromClass) {
            return new TransferDirection("CHUYEN_DEN", "Chuyển đến");
        }
        if (isFromClass && !isToClass) {
            return new TransferDirection("CHUYEN_DI", "Chuyển đi");
        }
        if ("CHUYEN_TRUONG".equalsIgnoreCase(normalizedType)) {
            return new TransferDirection("CHUYEN_TRUONG", "Chuyển trường");
        }
        return new TransferDirection("KHAC", "Thay đổi lớp");
    }

    private GenderType toGenderType(String gioiTinhRaw) {
        String normalized = normalize(gioiTinhRaw);
        if (normalized == null) {
            return GenderType.UNKNOWN;
        }

        String folded = foldToAscii(normalized).toLowerCase(Locale.ROOT);
        if (folded.contains("nu")) {
            return GenderType.FEMALE;
        }
        if (folded.contains("nam")) {
            return GenderType.MALE;
        }
        return GenderType.UNKNOWN;
    }

    private String foldToAscii(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private String normalizeUpper(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equalsIgnoreCase(right);
    }

    private String asString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        String value = row[index].toString().trim();
        return value.isEmpty() ? fallback : value;
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

    private LocalDate asLocalDate(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }

        Object value = row[index];
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    private Object[] normalizeRow(Object[] row) {
        if (row == null) {
            return new Object[0];
        }
        if (row.length == 1 && row[0] instanceof Object[] nested) {
            return nested;
        }
        return row;
    }

    public static class ClassInfoView {
        private final String idLop;
        private final String tenLop;
        private final Integer khoi;
        private final String namHoc;
        private final int siSo;
        private final String ghiChu;
        private final String idKhoa;
        private final String tenKhoa;
        private final String idGvcn;
        private final String gvcnTen;
        private final String gvcnEmail;
        private final String gvcnPhone;
        private final String gvcnAvatar;
        private final List<ClassStudentItem> students;
        private final List<ClassTransferHistoryItem> transferHistory;
        private final int totalStudents;
        private final int maleStudents;
        private final int femaleStudents;

        public ClassInfoView(String idLop,
                             String tenLop,
                             Integer khoi,
                             String namHoc,
                             int siSo,
                             String ghiChu,
                             String idKhoa,
                             String tenKhoa,
                             String idGvcn,
                             String gvcnTen,
                             String gvcnEmail,
                             String gvcnPhone,
                             String gvcnAvatar,
                             List<ClassStudentItem> students,
                             List<ClassTransferHistoryItem> transferHistory,
                             int totalStudents,
                             int maleStudents,
                             int femaleStudents) {
            this.idLop = idLop;
            this.tenLop = tenLop;
            this.khoi = khoi;
            this.namHoc = namHoc;
            this.siSo = siSo;
            this.ghiChu = ghiChu;
            this.idKhoa = idKhoa;
            this.tenKhoa = tenKhoa;
            this.idGvcn = idGvcn;
            this.gvcnTen = gvcnTen;
            this.gvcnEmail = gvcnEmail;
            this.gvcnPhone = gvcnPhone;
            this.gvcnAvatar = gvcnAvatar;
            this.students = students;
            this.transferHistory = transferHistory;
            this.totalStudents = totalStudents;
            this.maleStudents = maleStudents;
            this.femaleStudents = femaleStudents;
        }

        public String getIdLop() {
            return idLop;
        }

        public String getMaLop() {
            return idLop;
        }

        public String getTenLop() {
            return tenLop;
        }

        public String getTenLopHienThi() {
            if (tenLop == null || tenLop.isBlank()) {
                return idLop;
            }
            return tenLop;
        }

        public String getMaVaTenLop() {
            String code = idLop == null ? "" : idLop.trim();
            String name = getTenLopHienThi();
            if (code.isEmpty()) {
                return name;
            }
            if (name == null || name.isBlank() || name.equalsIgnoreCase(code)) {
                return code;
            }
            return code + " - " + name;
        }

        public Integer getKhoi() {
            return khoi;
        }

        public String getNamHoc() {
            return namHoc;
        }

        public int getSiSo() {
            return siSo;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public String getIdKhoa() {
            return idKhoa;
        }

        public String getTenKhoa() {
            return tenKhoa;
        }

        public String getKhoaHocDisplay() {
            if (idKhoa == null || idKhoa.isBlank()) {
                return "-";
            }
            if (tenKhoa == null || tenKhoa.isBlank() || idKhoa.equalsIgnoreCase(tenKhoa)) {
                return idKhoa;
            }
            return idKhoa + " (" + tenKhoa + ")";
        }

        public String getIdGvcn() {
            return idGvcn;
        }

        public String getGvcnTen() {
            return gvcnTen;
        }

        public String getGvcnEmail() {
            return gvcnEmail;
        }

        public String getGvcnPhone() {
            return gvcnPhone;
        }

        public String getGvcnAvatar() {
            return gvcnAvatar;
        }

        public String getGvcnAvatarUrl() {
            if (gvcnAvatar == null || gvcnAvatar.isBlank()) {
                return "";
            }
            if (gvcnAvatar.startsWith("/")) {
                return gvcnAvatar;
            }
            return "/uploads/" + gvcnAvatar;
        }

        public String getGvcnInitials() {
            if (gvcnTen == null || gvcnTen.isBlank() || "-".equals(gvcnTen)) {
                return "--";
            }
            String[] words = gvcnTen.trim().split("\\s+");
            if (words.length == 1) {
                String firstWord = words[0];
                return firstWord.length() >= 2 ? firstWord.substring(0, 2).toUpperCase() : firstWord.toUpperCase();
            }
            String first = words[words.length - 2];
            String last = words[words.length - 1];
            return (first.substring(0, 1) + last.substring(0, 1)).toUpperCase();
        }

        public List<ClassStudentItem> getStudents() {
            return students;
        }

        public List<ClassTransferHistoryItem> getTransferHistory() {
            return transferHistory;
        }

        public int getTotalStudents() {
            return totalStudents;
        }

        public int getMaleStudents() {
            return maleStudents;
        }

        public int getFemaleStudents() {
            return femaleStudents;
        }
    }

    public static class ClassStudentItem {
        private final String idHocSinh;
        private final String hoTen;
        private final String gioiTinhRaw;
        private final String email;
        private final String avatar;
        private final LocalDate ngayNhapHoc;
        private final String trangThaiRaw;

        public ClassStudentItem(String idHocSinh,
                                String hoTen,
                                String gioiTinhRaw,
                                String email,
                                String avatar,
                                LocalDate ngayNhapHoc,
                                String trangThaiRaw) {
            this.idHocSinh = idHocSinh;
            this.hoTen = hoTen;
            this.gioiTinhRaw = gioiTinhRaw;
            this.email = email;
            this.avatar = avatar;
            this.ngayNhapHoc = ngayNhapHoc;
            this.trangThaiRaw = trangThaiRaw;
        }

        public String getIdHocSinh() {
            return idHocSinh;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getGioiTinhRaw() {
            return gioiTinhRaw;
        }

        public String getGioiTinhDisplay() {
            if (gioiTinhRaw == null || gioiTinhRaw.isBlank() || "-".equals(gioiTinhRaw)) {
                return "-";
            }
            return gioiTinhRaw;
        }

        public String getEmail() {
            return email;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getAvatarUrl() {
            if (avatar == null || avatar.isBlank()) {
                return "";
            }
            if (avatar.startsWith("/")) {
                return avatar;
            }
            return "/uploads/" + avatar;
        }

        public String getInitials() {
            if (hoTen == null || hoTen.isBlank() || "-".equals(hoTen)) {
                return "HS";
            }
            String[] words = hoTen.trim().split("\\s+");
            if (words.length == 1) {
                String firstWord = words[0];
                return firstWord.length() >= 2 ? firstWord.substring(0, 2).toUpperCase() : firstWord.toUpperCase();
            }
            String first = words[words.length - 2];
            String last = words[words.length - 1];
            return (first.substring(0, 1) + last.substring(0, 1)).toUpperCase();
        }

        public LocalDate getNgayNhapHoc() {
            return ngayNhapHoc;
        }

        public String getNgayNhapHocDisplay() {
            if (ngayNhapHoc == null) {
                return "-";
            }
            return ngayNhapHoc.format(DATE_DISPLAY_FORMAT);
        }

        public String getTrangThaiRaw() {
            return trangThaiRaw;
        }

        public String getTrangThaiDisplay() {
            if (trangThaiRaw == null || trangThaiRaw.isBlank() || "-".equals(trangThaiRaw)) {
                return "-";
            }
            if ("dang_hoc".equalsIgnoreCase(trangThaiRaw)) {
                return "Đang học";
            }
            if ("da_tot_nghiep".equalsIgnoreCase(trangThaiRaw)) {
                return "Đã tốt nghiệp";
            }
            if ("bo_hoc".equalsIgnoreCase(trangThaiRaw)) {
                return "Bỏ học";
            }
            if ("chuyen_truong".equalsIgnoreCase(trangThaiRaw)) {
                return "Chuyển trường";
            }
            return trangThaiRaw;
        }
    }

    public static class ClassTransferHistoryItem {
        private final String studentId;
        private final String studentName;
        private final String fromClass;
        private final String toClass;
        private final LocalDate transferDate;
        private final String transferTypeCode;
        private final String transferTypeDisplay;
        private final String note;

        public ClassTransferHistoryItem(String studentId,
                                        String studentName,
                                        String fromClass,
                                        String toClass,
                                        LocalDate transferDate,
                                        String transferTypeCode,
                                        String transferTypeDisplay,
                                        String note) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.fromClass = fromClass;
            this.toClass = toClass;
            this.transferDate = transferDate;
            this.transferTypeCode = transferTypeCode;
            this.transferTypeDisplay = transferTypeDisplay;
            this.note = note;
        }

        public String getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getFromClass() {
            return fromClass;
        }

        public String getToClass() {
            return toClass;
        }

        public LocalDate getTransferDate() {
            return transferDate;
        }

        public String getTransferDateDisplay() {
            if (transferDate == null) {
                return "-";
            }
            return transferDate.format(DATE_DISPLAY_FORMAT);
        }

        public String getTransferTypeCode() {
            return transferTypeCode;
        }

        public String getTransferTypeDisplay() {
            return transferTypeDisplay;
        }

        public String getTransferBadgeClass() {
            if ("CHUYEN_DEN".equalsIgnoreCase(transferTypeCode)) {
                return "badge-in";
            }
            if ("CHUYEN_DI".equalsIgnoreCase(transferTypeCode)) {
                return "badge-out";
            }
            if ("CHUYEN_TRUONG".equalsIgnoreCase(transferTypeCode)) {
                return "badge-school";
            }
            return "badge-neutral";
        }

        public String getNote() {
            return note;
        }
    }

    private enum GenderType {
        MALE,
        FEMALE,
        UNKNOWN
    }

    private static class TransferDirection {
        private final String code;
        private final String display;

        private TransferDirection(String code, String display) {
            this.code = code;
            this.display = display;
        }
    }
}
