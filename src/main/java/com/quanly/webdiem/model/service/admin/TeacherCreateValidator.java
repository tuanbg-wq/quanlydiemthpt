package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.TeacherCreateForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TeacherCreateValidator implements Validator {

    private static final int MIN_AGE = 22;
    private static final long MAX_AVATAR_SIZE = 3L * 1024 * 1024;

    private static final Pattern TEACHER_ID_PATTERN = Pattern.compile("^GV\\d{3,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern ONLY_DIGITS_PATTERN = Pattern.compile("^\\d+$");

    private static final Set<String> ALLOWED_GENDERS = Set.of("Nam", "Nu", "Khac");
    private static final Set<String> ALLOWED_STATUSES = Set.of("dang_lam", "nghi_viec");
    private static final Set<String> ALLOWED_DEGREES = Set.of("CU_NHAN", "THAC_SI", "TIEN_SI", "KHAC");
    private static final Set<String> ALLOWED_ROLES = Set.of("GVCN", "GVBM");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final TeacherDAO teacherDAO;
    private final SubjectDAO subjectDAO;

    public TeacherCreateValidator(TeacherDAO teacherDAO, SubjectDAO subjectDAO) {
        this.teacherDAO = teacherDAO;
        this.subjectDAO = subjectDAO;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return TeacherCreateForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateForCreate((TeacherCreateForm) target, errors);
    }

    public void validateForCreate(TeacherCreateForm form, Errors errors) {
        validateInternal(form, errors, null, true);
    }

    public void validateForUpdate(String teacherId, TeacherCreateForm form, Errors errors) {
        validateInternal(form, errors, teacherId, false);
    }

    private void validateInternal(TeacherCreateForm form,
                                  Errors errors,
                                  String currentTeacherId,
                                  boolean enforceActiveStatusForRole) {
        LocalDate today = LocalDate.now();

        validateTeacherId(form, errors, currentTeacherId);
        validateFullName(form, errors);
        validateBirthDate(form, errors, today);
        validateGender(form, errors);
        validatePhone(form, errors);
        validateEmail(form, errors, currentTeacherId);
        validateAddress(form, errors);
        validateSubject(form, errors);
        validateDegree(form, errors);
        validateStartDate(form, errors, today);
        validateAvatar(form, errors);
        validateNote(form, errors);
        validateStatus(form, errors);
        validateSchoolYear(form, errors);
        validateRoles(form, errors, enforceActiveStatusForRole);
    }

    private void validateTeacherId(TeacherCreateForm form, Errors errors, String currentTeacherId) {
        String teacherId = normalize(form.getIdGiaoVien());
        if (teacherId == null) {
            errors.rejectValue("idGiaoVien", "teacher.id.required", "M\u00e3 gi\u00e1o vi\u00ean l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        String upperId = teacherId.toUpperCase(Locale.ROOT);
        if (!TEACHER_ID_PATTERN.matcher(upperId).matches()) {
            errors.rejectValue("idGiaoVien", "teacher.id.format", "M\u00e3 gi\u00e1o vi\u00ean ph\u1ea3i c\u00f3 d\u1ea1ng GV001, GV002...");
            return;
        }

        String expectedTeacherId = normalize(currentTeacherId);
        if (expectedTeacherId == null) {
            if (teacherDAO.existsById(upperId)) {
                errors.rejectValue("idGiaoVien", "teacher.id.duplicate", "M\u00e3 gi\u00e1o vi\u00ean \u0111\u00e3 t\u1ed3n t\u1ea1i.");
                return;
            }
            if (subjectDAO.existsById(upperId)) {
                errors.rejectValue(
                        "idGiaoVien",
                        "teacher.id.conflictSubject",
                        "M\u00e3 gi\u00e1o vi\u00ean kh\u00f4ng \u0111\u01b0\u1ee3c tr\u00f9ng m\u00e3 m\u00f4n h\u1ecdc."
                );
            }
            return;
        }

        String expectedUpperId = expectedTeacherId.toUpperCase(Locale.ROOT);
        if (!upperId.equals(expectedUpperId)) {
            errors.rejectValue("idGiaoVien", "teacher.id.immutable", "Kh\u00f4ng \u0111\u01b0\u1ee3c thay \u0111\u1ed5i m\u00e3 gi\u00e1o vi\u00ean.");
            return;
        }

        if (!teacherDAO.existsById(expectedUpperId)) {
            errors.rejectValue("idGiaoVien", "teacher.id.notFound", "Kh\u00f4ng t\u00ecm th\u1ea5y gi\u00e1o vi\u00ean \u0111\u1ec3 c\u1eadp nh\u1eadt.");
        }
    }

    private void validateFullName(TeacherCreateForm form, Errors errors) {
        String fullName = normalize(form.getHoTen());
        if (fullName == null) {
            errors.rejectValue("hoTen", "teacher.name.required", "H\u1ecd v\u00e0 t\u00ean l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (fullName.length() < 2) {
            errors.rejectValue("hoTen", "teacher.name.length", "H\u1ecd v\u00e0 t\u00ean ph\u1ea3i c\u00f3 \u00edt nh\u1ea5t 2 k\u00fd t\u1ef1.");
            return;
        }

        if (ONLY_DIGITS_PATTERN.matcher(fullName.replace(" ", "")).matches()) {
            errors.rejectValue("hoTen", "teacher.name.invalid", "H\u1ecd v\u00e0 t\u00ean kh\u00f4ng \u0111\u01b0\u1ee3c ch\u1ec9 ch\u1ee9a s\u1ed1.");
        }
    }

    private void validateBirthDate(TeacherCreateForm form, Errors errors, LocalDate today) {
        LocalDate birthDate = form.getNgaySinh();
        if (birthDate == null) {
            errors.rejectValue("ngaySinh", "teacher.birth.required", "Ng\u00e0y sinh l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (birthDate.isAfter(today)) {
            errors.rejectValue("ngaySinh", "teacher.birth.invalid", "Ng\u00e0y sinh kh\u00f4ng h\u1ee3p l\u1ec7.");
            return;
        }

        if (Period.between(birthDate, today).getYears() < MIN_AGE) {
            errors.rejectValue("ngaySinh", "teacher.birth.age", "Gi\u00e1o vi\u00ean ph\u1ea3i t\u1eeb 22 tu\u1ed5i tr\u1edf l\u00ean.");
        }
    }

    private void validateGender(TeacherCreateForm form, Errors errors) {
        String gender = normalize(form.getGioiTinh());
        if (gender == null) {
            errors.rejectValue("gioiTinh", "teacher.gender.required", "Gi\u1edbi t\u00ednh l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (!ALLOWED_GENDERS.contains(gender)) {
            errors.rejectValue("gioiTinh", "teacher.gender.invalid", "Gi\u1edbi t\u00ednh kh\u00f4ng h\u1ee3p l\u1ec7.");
        }
    }

    private void validatePhone(TeacherCreateForm form, Errors errors) {
        String phone = normalize(form.getSoDienThoai());
        if (phone == null) {
            errors.rejectValue("soDienThoai", "teacher.phone.required", "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.rejectValue("soDienThoai", "teacher.phone.invalid", "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i Vi\u1ec7t Nam kh\u00f4ng h\u1ee3p l\u1ec7.");
        }
    }

    private void validateEmail(TeacherCreateForm form, Errors errors, String currentTeacherId) {
        String email = normalize(form.getEmail());
        if (email == null) {
            errors.rejectValue("email", "teacher.email.required", "Email l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.rejectValue("email", "teacher.email.format", "Email kh\u00f4ng \u0111\u00fang \u0111\u1ecbnh d\u1ea1ng.");
            return;
        }

        String normalizedTeacherId = normalize(currentTeacherId);
        if (normalizedTeacherId == null) {
            if (teacherDAO.countByEmailIgnoreCase(email) > 0) {
                errors.rejectValue("email", "teacher.email.duplicate", "Email \u0111\u00e3 t\u1ed3n t\u1ea1i trong h\u1ec7 th\u1ed1ng.");
            }
            return;
        }

        if (teacherDAO.countByEmailIgnoreCaseAndIdGiaoVienNot(email, normalizedTeacherId) > 0) {
            errors.rejectValue("email", "teacher.email.duplicate", "Email \u0111\u00e3 t\u1ed3n t\u1ea1i trong h\u1ec7 th\u1ed1ng.");
        }
    }

    private void validateAddress(TeacherCreateForm form, Errors errors) {
        String address = normalize(form.getDiaChi());
        if (address == null) {
            errors.rejectValue("diaChi", "teacher.address.required", "\u0110\u1ecba ch\u1ec9 l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (address.length() > 255) {
            errors.rejectValue("diaChi", "teacher.address.length", "\u0110\u1ecba ch\u1ec9 kh\u00f4ng v\u01b0\u1ee3t qu\u00e1 255 k\u00fd t\u1ef1.");
        }
    }

    private void validateSubject(TeacherCreateForm form, Errors errors) {
        String subjectId = normalize(form.getMonHocId());
        if (subjectId == null) {
            errors.rejectValue("monHocId", "teacher.subject.required", "M\u00f4n d\u1ea1y l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (!subjectDAO.existsById(subjectId)) {
            errors.rejectValue("monHocId", "teacher.subject.invalid", "M\u00f4n d\u1ea1y kh\u00f4ng t\u1ed3n t\u1ea1i.");
        }
    }

    private void validateDegree(TeacherCreateForm form, Errors errors) {
        String degree = normalize(form.getTrinhDo());
        if (degree == null) {
            errors.rejectValue("trinhDo", "teacher.degree.required", "Tr\u00ecnh \u0111\u1ed9 h\u1ecdc v\u1ea5n l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (!ALLOWED_DEGREES.contains(degree)) {
            errors.rejectValue("trinhDo", "teacher.degree.invalid", "Tr\u00ecnh \u0111\u1ed9 h\u1ecdc v\u1ea5n kh\u00f4ng h\u1ee3p l\u1ec7.");
        }
    }

    private void validateStartDate(TeacherCreateForm form, Errors errors, LocalDate today) {
        LocalDate startDate = form.getNgayBatDauCongTac();
        if (startDate == null) {
            errors.rejectValue("ngayBatDauCongTac", "teacher.start.required", "Ng\u00e0y b\u1eaft \u0111\u1ea7u c\u00f4ng t\u00e1c l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (startDate.isAfter(today)) {
            errors.rejectValue("ngayBatDauCongTac", "teacher.start.future", "Ng\u00e0y b\u1eaft \u0111\u1ea7u c\u00f4ng t\u00e1c kh\u00f4ng \u0111\u01b0\u1ee3c l\u1edbn h\u01a1n hi\u1ec7n t\u1ea1i.");
        }

        LocalDate birthDate = form.getNgaySinh();
        if (birthDate != null && startDate.isBefore(birthDate.plusYears(MIN_AGE))) {
            errors.rejectValue(
                    "ngayBatDauCongTac",
                    "teacher.start.age",
                    "Ng\u00e0y b\u1eaft \u0111\u1ea7u c\u00f4ng t\u00e1c kh\u00f4ng h\u1ee3p l\u1ec7 theo \u0111\u1ed9 tu\u1ed5i t\u1ed1i thi\u1ec3u."
            );
        }
    }

    private void validateAvatar(TeacherCreateForm form, Errors errors) {
        MultipartFile avatar = form.getAvatar();
        if (avatar == null || avatar.isEmpty()) {
            return;
        }

        String fileName = avatar.getOriginalFilename();
        String extension = extension(fileName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            errors.rejectValue("avatar", "teacher.avatar.ext", "\u1ea2nh \u0111\u1ea1i di\u1ec7n ch\u1ec9 h\u1ed7 tr\u1ee3 jpg, jpeg, png.");
            return;
        }

        if (avatar.getSize() > MAX_AVATAR_SIZE) {
            errors.rejectValue("avatar", "teacher.avatar.size", "\u1ea2nh \u0111\u1ea1i di\u1ec7n kh\u00f4ng v\u01b0\u1ee3t qu\u00e1 3MB.");
        }
    }

    private void validateNote(TeacherCreateForm form, Errors errors) {
        String note = normalize(form.getGhiChu());
        if (note != null && note.length() > 1000) {
            errors.rejectValue("ghiChu", "teacher.note.length", "Ghi ch\u00fa kh\u00f4ng v\u01b0\u1ee3t qu\u00e1 1000 k\u00fd t\u1ef1.");
        }
    }

    private void validateStatus(TeacherCreateForm form, Errors errors) {
        String status = normalize(form.getTrangThai());
        if (status == null) {
            errors.rejectValue("trangThai", "teacher.status.required", "Tr\u1ea1ng th\u00e1i ho\u1ea1t \u0111\u1ed9ng l\u00e0 b\u1eaft bu\u1ed9c.");
            return;
        }

        if (!ALLOWED_STATUSES.contains(status)) {
            errors.rejectValue("trangThai", "teacher.status.invalid", "Tr\u1ea1ng th\u00e1i ho\u1ea1t \u0111\u1ed9ng kh\u00f4ng h\u1ee3p l\u1ec7.");
        }
    }

    private void validateSchoolYear(TeacherCreateForm form, Errors errors) {
        String schoolYear = normalize(form.getNamHoc());
        if (schoolYear == null) {
            errors.rejectValue("namHoc", "teacher.namHoc.required", "N\u0103m h\u1ecdc \u00e1p d\u1ee5ng vai tr\u00f2 l\u00e0 b\u1eaft bu\u1ed9c.");
        }
    }

    private void validateRoles(TeacherCreateForm form, Errors errors, boolean enforceActiveStatusForRole) {
        String status = normalize(form.getTrangThai());
        if (!enforceActiveStatusForRole && status != null && !"dang_lam".equals(status)) {
            return;
        }

        List<String> roles = form.getVaiTroMa();
        if (roles == null || roles.isEmpty()) {
            errors.rejectValue("vaiTroMa", "teacher.roles.required", "Vui l\u00f2ng ch\u1ecdn 1 vai tr\u00f2 gi\u00e1o vi\u00ean.");
            return;
        }

        List<String> normalizedRoles = roles.stream()
                .map(this::normalize)
                .filter(Objects::nonNull)
                .map(role -> role.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();

        if (normalizedRoles.isEmpty()) {
            errors.rejectValue("vaiTroMa", "teacher.roles.required", "Vui l\u00f2ng ch\u1ecdn 1 vai tr\u00f2 gi\u00e1o vi\u00ean.");
            return;
        }

        if (normalizedRoles.size() > 1) {
            errors.rejectValue("vaiTroMa", "teacher.roles.single", "Ch\u1ec9 \u0111\u01b0\u1ee3c ch\u1ecdn 1 vai tr\u00f2 gi\u00e1o vi\u00ean.");
            return;
        }

        String role = normalizedRoles.get(0);
        if (!ALLOWED_ROLES.contains(role)) {
            errors.rejectValue("vaiTroMa", "teacher.roles.invalid", "Vai tr\u00f2 gi\u00e1o vi\u00ean kh\u00f4ng h\u1ee3p l\u1ec7.");
            return;
        }

        if (enforceActiveStatusForRole) {
            if (status != null && !"dang_lam".equals(status)) {
                errors.rejectValue(
                        "vaiTroMa",
                        "teacher.roles.status",
                        "Ch\u1ec9 gi\u00e1o vi\u00ean \u0111ang c\u00f4ng t\u00e1c m\u1edbi \u0111\u01b0\u1ee3c g\u00e1n vai tr\u00f2 nghi\u1ec7p v\u1ee5."
                );
            }
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

        return trimmed;
    }

    private String extension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }

        return fileName.substring(dot + 1);
    }
}
