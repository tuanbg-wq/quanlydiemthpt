package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.TeacherCreateForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    private final ClassDAO classDAO;

    public TeacherCreateValidator(TeacherDAO teacherDAO, SubjectDAO subjectDAO, ClassDAO classDAO) {
        this.teacherDAO = teacherDAO;
        this.subjectDAO = subjectDAO;
        this.classDAO = classDAO;
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
        validateClassAssignments(form, errors, currentTeacherId);
    }

    private void validateTeacherId(TeacherCreateForm form, Errors errors, String currentTeacherId) {
        String teacherId = normalize(form.getIdGiaoVien());
        if (teacherId == null) {
            errors.rejectValue("idGiaoVien", "teacher.id.required", "Mã giáo viên là bắt buộc.");
            return;
        }

        String upperId = teacherId.toUpperCase(Locale.ROOT);
        if (!TEACHER_ID_PATTERN.matcher(upperId).matches()) {
            errors.rejectValue("idGiaoVien", "teacher.id.format", "Mã giáo viên phải có dạng GV001, GV002...");
            return;
        }

        String expectedTeacherId = normalize(currentTeacherId);
        if (expectedTeacherId == null) {
            if (teacherDAO.existsById(upperId)) {
                errors.rejectValue("idGiaoVien", "teacher.id.duplicate", "Mã giáo viên đã tồn tại.");
                return;
            }
            if (subjectDAO.existsById(upperId)) {
                errors.rejectValue(
                        "idGiaoVien",
                        "teacher.id.conflictSubject",
                        "Mã giáo viên không được trùng mã môn học."
                );
            }
            return;
        }

        String expectedUpperId = expectedTeacherId.toUpperCase(Locale.ROOT);
        if (!teacherDAO.existsById(expectedUpperId)) {
            errors.rejectValue("idGiaoVien", "teacher.id.notFound", "Không tìm thấy giáo viên để cập nhật.");
            return;
        }

        if (upperId.equals(expectedUpperId)) {
            return;
        }

        if (teacherDAO.existsById(upperId)) {
            errors.rejectValue("idGiaoVien", "teacher.id.duplicate", "Mã giáo viên đã tồn tại.");
            return;
        }

        if (subjectDAO.existsById(upperId)) {
            errors.rejectValue(
                    "idGiaoVien",
                    "teacher.id.conflictSubject",
                    "Mã giáo viên không được trùng mã môn học."
            );
        }
    }

    private void validateFullName(TeacherCreateForm form, Errors errors) {
        String fullName = normalize(form.getHoTen());
        if (fullName == null) {
            errors.rejectValue("hoTen", "teacher.name.required", "Họ và tên là bắt buộc.");
            return;
        }

        if (fullName.length() < 2) {
            errors.rejectValue("hoTen", "teacher.name.length", "Họ và tên phải có ít nhất 2 ký tự.");
            return;
        }

        if (ONLY_DIGITS_PATTERN.matcher(fullName.replace(" ", "")).matches()) {
            errors.rejectValue("hoTen", "teacher.name.invalid", "Họ và tên không được chỉ chứa số.");
        }
    }

    private void validateBirthDate(TeacherCreateForm form, Errors errors, LocalDate today) {
        LocalDate birthDate = form.getNgaySinh();
        if (birthDate == null) {
            errors.rejectValue("ngaySinh", "teacher.birth.required", "Ngày sinh là bắt buộc.");
            return;
        }

        if (birthDate.isAfter(today)) {
            errors.rejectValue("ngaySinh", "teacher.birth.invalid", "Ngày sinh không hợp lệ.");
            return;
        }

        if (Period.between(birthDate, today).getYears() < MIN_AGE) {
            errors.rejectValue("ngaySinh", "teacher.birth.age", "Giáo viên phải từ 22 tuổi trở lên.");
        }
    }

    private void validateGender(TeacherCreateForm form, Errors errors) {
        String gender = normalize(form.getGioiTinh());
        if (gender == null) {
            errors.rejectValue("gioiTinh", "teacher.gender.required", "Giới tính là bắt buộc.");
            return;
        }

        if (!ALLOWED_GENDERS.contains(gender)) {
            errors.rejectValue("gioiTinh", "teacher.gender.invalid", "Giới tính không hợp lệ.");
        }
    }

    private void validatePhone(TeacherCreateForm form, Errors errors) {
        String phone = normalize(form.getSoDienThoai());
        if (phone == null) {
            errors.rejectValue("soDienThoai", "teacher.phone.required", "Số điện thoại là bắt buộc.");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.rejectValue("soDienThoai", "teacher.phone.invalid", "Số điện thoại Việt Nam không hợp lệ.");
        }
    }

    private void validateEmail(TeacherCreateForm form, Errors errors, String currentTeacherId) {
        String email = normalize(form.getEmail());
        if (email == null) {
            errors.rejectValue("email", "teacher.email.required", "Email là bắt buộc.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.rejectValue("email", "teacher.email.format", "Email không đúng định dạng.");
            return;
        }

        String normalizedTeacherId = normalize(currentTeacherId);
        if (normalizedTeacherId == null) {
            if (teacherDAO.countByEmailIgnoreCase(email) > 0) {
                errors.rejectValue("email", "teacher.email.duplicate", "Email đã tồn tại trong hệ thống.");
            }
            return;
        }

        if (teacherDAO.countByEmailIgnoreCaseAndIdGiaoVienNot(email, normalizedTeacherId) > 0) {
            errors.rejectValue("email", "teacher.email.duplicate", "Email đã tồn tại trong hệ thống.");
        }
    }

    private void validateAddress(TeacherCreateForm form, Errors errors) {
        String address = normalize(form.getDiaChi());
        if (address == null) {
            errors.rejectValue("diaChi", "teacher.address.required", "Địa chỉ là bắt buộc.");
            return;
        }

        if (address.length() > 255) {
            errors.rejectValue("diaChi", "teacher.address.length", "Địa chỉ không vượt quá 255 ký tự.");
        }
    }

    private void validateSubject(TeacherCreateForm form, Errors errors) {
        String subjectId = normalize(form.getMonHocId());
        if (subjectId == null) {
            errors.rejectValue("monHocId", "teacher.subject.required", "Môn dạy là bắt buộc.");
            return;
        }

        if (!subjectDAO.existsById(subjectId)) {
            errors.rejectValue("monHocId", "teacher.subject.invalid", "Môn dạy không tồn tại.");
        }
    }

    private void validateDegree(TeacherCreateForm form, Errors errors) {
        String degree = normalize(form.getTrinhDo());
        if (degree == null) {
            errors.rejectValue("trinhDo", "teacher.degree.required", "Trình độ học vấn là bắt buộc.");
            return;
        }

        if (!ALLOWED_DEGREES.contains(degree)) {
            errors.rejectValue("trinhDo", "teacher.degree.invalid", "Trình độ học vấn không hợp lệ.");
        }
    }

    private void validateStartDate(TeacherCreateForm form, Errors errors, LocalDate today) {
        LocalDate startDate = form.getNgayBatDauCongTac();
        if (startDate == null) {
            errors.rejectValue("ngayBatDauCongTac", "teacher.start.required", "Ngày bắt đầu công tác là bắt buộc.");
            return;
        }

        if (startDate.isAfter(today)) {
            errors.rejectValue("ngayBatDauCongTac", "teacher.start.future", "Ngày bắt đầu công tác không được lớn hơn hiện tại.");
        }

        LocalDate birthDate = form.getNgaySinh();
        if (birthDate != null && startDate.isBefore(birthDate.plusYears(MIN_AGE))) {
            errors.rejectValue(
                    "ngayBatDauCongTac",
                    "teacher.start.age",
                    "Ngày bắt đầu công tác không hợp lệ theo độ tuổi tối thiểu."
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
            errors.rejectValue("avatar", "teacher.avatar.ext", "Ảnh đại diện chỉ hỗ trợ jpg, jpeg, png.");
            return;
        }

        if (avatar.getSize() > MAX_AVATAR_SIZE) {
            errors.rejectValue("avatar", "teacher.avatar.size", "Ảnh đại diện không vượt quá 3MB.");
        }
    }

    private void validateNote(TeacherCreateForm form, Errors errors) {
        String note = normalize(form.getGhiChu());
        if (note != null && note.length() > 1000) {
            errors.rejectValue("ghiChu", "teacher.note.length", "Ghi chú không vượt quá 1000 ký tự.");
        }
    }

    private void validateStatus(TeacherCreateForm form, Errors errors) {
        String status = normalize(form.getTrangThai());
        if (status == null) {
            errors.rejectValue("trangThai", "teacher.status.required", "Trạng thái hoạt động là bắt buộc.");
            return;
        }

        if (!ALLOWED_STATUSES.contains(status)) {
            errors.rejectValue("trangThai", "teacher.status.invalid", "Trạng thái hoạt động không hợp lệ.");
        }
    }

    private void validateSchoolYear(TeacherCreateForm form, Errors errors) {
        String schoolYear = normalize(form.getNamHoc());
        if (schoolYear == null) {
            errors.rejectValue("namHoc", "teacher.namHoc.required", "Năm học áp dụng vai trò là bắt buộc.");
        }
    }

    private void validateRoles(TeacherCreateForm form, Errors errors, boolean enforceActiveStatusForRole) {
        String status = normalize(form.getTrangThai());
        if (!enforceActiveStatusForRole && status != null && !"dang_lam".equals(status)) {
            return;
        }

        List<String> roles = form.getVaiTroMa();
        if (roles == null || roles.isEmpty()) {
            errors.rejectValue("vaiTroMa", "teacher.roles.required", "Vui lòng chọn 1 vai trò giáo viên.");
            return;
        }

        List<String> normalizedRoles = roles.stream()
                .map(this::normalize)
                .filter(Objects::nonNull)
                .map(role -> role.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();

        if (normalizedRoles.isEmpty()) {
            errors.rejectValue("vaiTroMa", "teacher.roles.required", "Vui lòng chọn 1 vai trò giáo viên.");
            return;
        }

        if (normalizedRoles.size() > 1) {
            errors.rejectValue("vaiTroMa", "teacher.roles.single", "Chỉ được chọn 1 vai trò giáo viên.");
            return;
        }

        String role = normalizedRoles.get(0);
        if (!ALLOWED_ROLES.contains(role)) {
            errors.rejectValue("vaiTroMa", "teacher.roles.invalid", "Vai trò giáo viên không hợp lệ.");
            return;
        }

        if (enforceActiveStatusForRole && status != null && !"dang_lam".equals(status)) {
            errors.rejectValue(
                    "vaiTroMa",
                    "teacher.roles.status",
                    "Chỉ giáo viên đang công tác mới được gán vai trò nghiệp vụ."
            );
        }
    }

    private void validateClassAssignments(TeacherCreateForm form, Errors errors, String currentTeacherId) {
        String status = normalize(form.getTrangThai());
        if (!"dang_lam".equalsIgnoreCase(status)) {
            return;
        }

        String role = resolveSelectedRoleCode(form.getVaiTroMa());
        if (role == null) {
            return;
        }

        List<String> subjectClassIds = parseClassIds(form.getLopBoMon());
        if (subjectClassIds.isEmpty()) {
            errors.rejectValue(
                    "lopBoMon",
                    "teacher.subjectClass.required",
                    "Lớp bộ môn là bắt buộc. Có thể nhập nhiều lớp, ví dụ: 10A1, 10A2."
            );
            return;
        }

        List<String> invalidSubjectClasses = new ArrayList<>();
        for (String classId : subjectClassIds) {
            if (classDAO.findById(classId).isEmpty()) {
                invalidSubjectClasses.add(classId);
            }
        }
        if (!invalidSubjectClasses.isEmpty()) {
            errors.rejectValue(
                    "lopBoMon",
                    "teacher.subjectClass.invalid",
                    "Lớp bộ môn không tồn tại: " + String.join(", ", invalidSubjectClasses) + "."
            );
            return;
        }

        if ("GVCN".equalsIgnoreCase(role)) {
            String homeroomClassId = normalize(form.getLopChuNhiem());
            if (homeroomClassId == null) {
                errors.rejectValue(
                        "lopChuNhiem",
                        "teacher.homeroomClass.required",
                        "Lớp chủ nhiệm là bắt buộc khi chọn vai trò giáo viên chủ nhiệm."
                );
                return;
            }
            String normalizedClassId = homeroomClassId.toUpperCase(Locale.ROOT);
            ClassEntity homeroomClass = classDAO.findById(normalizedClassId).orElse(null);
            if (homeroomClass == null) {
                errors.rejectValue("lopChuNhiem", "teacher.homeroomClass.invalid", "Lop chu nhiem khong ton tai.");
                return;
            }

            String selectedTeacherId = normalize(form.getIdGiaoVien());
            if (selectedTeacherId == null) {
                selectedTeacherId = normalize(currentTeacherId);
            }
            String assignedHomeroomTeacherId = normalize(homeroomClass.getIdGvcn());
            if (assignedHomeroomTeacherId != null
                    && (selectedTeacherId == null || !assignedHomeroomTeacherId.equalsIgnoreCase(selectedTeacherId))) {
                errors.rejectValue(
                        "lopChuNhiem",
                        "teacher.homeroomClass.occupied",
                        "Lop nay da co giao vien chu nhiem, vui long chon lop khac."
                );
            }
        }
    }

    private String resolveSelectedRoleCode(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
                .map(this::normalize)
                .filter(Objects::nonNull)
                .map(role -> role.toUpperCase(Locale.ROOT))
                .findFirst()
                .orElse(null);
    }

    private List<String> parseClassIds(String raw) {
        String normalizedRaw = normalize(raw);
        if (normalizedRaw == null) {
            return List.of();
        }

        String[] tokens = normalizedRaw.split("[,;\\n]+");
        LinkedHashSet<String> uniqueClassIds = new LinkedHashSet<>();
        for (String token : tokens) {
            String normalizedToken = normalize(token);
            if (normalizedToken == null) {
                continue;
            }
            uniqueClassIds.add(normalizedToken.toUpperCase(Locale.ROOT));
        }
        return new ArrayList<>(uniqueClassIds);
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
