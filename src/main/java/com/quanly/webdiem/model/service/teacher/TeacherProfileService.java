package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.User;
import com.quanly.webdiem.model.form.TeacherProfileUpdateForm;
import com.quanly.webdiem.model.service.FileStorageService;
import com.quanly.webdiem.model.service.admin.TeacherInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class TeacherProfileService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UserDAO userDAO;
    private final TeacherDAO teacherDAO;
    private final TeacherInfoService teacherInfoService;
    private final FileStorageService fileStorageService;

    public TeacherProfileService(UserDAO userDAO,
                                 TeacherDAO teacherDAO,
                                 TeacherInfoService teacherInfoService,
                                 FileStorageService fileStorageService) {
        this.userDAO = userDAO;
        this.teacherDAO = teacherDAO;
        this.teacherInfoService = teacherInfoService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public TeacherProfilePageData getProfilePageData(String username) {
        String resolvedUsername = normalize(username);
        User user = resolvedUsername == null ? null : userDAO.findByTenDangNhap(resolvedUsername).orElse(null);
        Teacher teacher = resolveTeacher(user);

        TeacherInfoService.TeacherInfoView teacherInfo = teacher != null
                ? teacherInfoService.getTeacherInfo(teacher.getIdGiaoVien())
                : buildFallbackTeacherInfo(resolvedUsername, user);

        TeacherProfileUpdateForm form = new TeacherProfileUpdateForm();
        form.setEmail(firstNonBlank(user == null ? null : user.getEmail(), teacher == null ? null : teacher.getEmail()));
        form.setSoDienThoai(teacher == null ? null : teacher.getSoDienThoai());

        return new TeacherProfilePageData(
                resolvedUsername,
                displayAccountStatus(user == null ? null : user.getTrangThai()),
                user == null ? null : user.getIdTaiKhoan(),
                teacher == null ? null : teacher.getIdGiaoVien(),
                teacherInfo,
                form,
                user != null && teacher != null
        );
    }

    @Transactional
    public void updateProfile(String username, TeacherProfileUpdateForm form) {
        String resolvedUsername = normalize(username);
        if (resolvedUsername == null) {
            throw new RuntimeException("Không xác định được tài khoản đăng nhập.");
        }

        User user = userDAO.findByTenDangNhap(resolvedUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản giáo viên."));
        Teacher teacher = resolveTeacher(user);
        if (teacher == null) {
            throw new RuntimeException("Không tìm thấy hồ sơ giáo viên để cập nhật.");
        }

        String email = normalizeLower(form == null ? null : form.getEmail());
        String phone = normalize(form == null ? null : form.getSoDienThoai());
        Map<String, String> errors = validateForm(email, phone, user.getIdTaiKhoan(), teacher.getIdGiaoVien());
        if (!errors.isEmpty()) {
            throw new RuntimeException(errors.values().iterator().next());
        }

        teacher.setEmail(email);
        teacher.setSoDienThoai(phone);
        user.setEmail(email);

        if (form != null && form.getAvatar() != null && !form.getAvatar().isEmpty()) {
            String avatarPath = fileStorageService.saveTeacherAvatar(teacher.getIdGiaoVien(), form.getAvatar());
            teacher.setAnh(avatarPath);
        }

        teacherDAO.save(teacher);
        userDAO.save(user);
    }

    @Transactional(readOnly = true)
    public Map<String, String> validateForUpdate(String username, TeacherProfileUpdateForm form) {
        String resolvedUsername = normalize(username);
        User user = resolvedUsername == null ? null : userDAO.findByTenDangNhap(resolvedUsername).orElse(null);
        Teacher teacher = resolveTeacher(user);
        return validateForm(
                normalizeLower(form == null ? null : form.getEmail()),
                normalize(form == null ? null : form.getSoDienThoai()),
                user == null ? null : user.getIdTaiKhoan(),
                teacher == null ? null : teacher.getIdGiaoVien()
        );
    }

    private Map<String, String> validateForm(String email,
                                             String phone,
                                             Integer currentAccountId,
                                             String currentTeacherId) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (phone == null) {
            errors.put("soDienThoai", "Số điện thoại là bắt buộc.");
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.put("soDienThoai", "Số điện thoại Việt Nam không hợp lệ.");
        }

        if (email == null) {
            errors.put("email", "Email là bắt buộc.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "Email không đúng định dạng.");
        } else if (email.length() > 100) {
            errors.put("email", "Email không vượt quá 100 ký tự.");
        } else {
            Optional<User> existingUser = userDAO.findByEmailIgnoreCase(email);
            if (existingUser.isPresent()
                    && currentAccountId != null
                    && !currentAccountId.equals(existingUser.get().getIdTaiKhoan())) {
                errors.put("email", "Email đã tồn tại trong tài khoản khác.");
            }
            if (!errors.containsKey("email")
                    && currentTeacherId != null
                    && teacherDAO.countByEmailIgnoreCaseAndIdGiaoVienNot(email, currentTeacherId) > 0) {
                errors.put("email", "Email đã tồn tại trong hồ sơ giáo viên khác.");
            }
        }

        return errors;
    }

    private Teacher resolveTeacher(User user) {
        if (user == null || user.getIdTaiKhoan() == null) {
            return null;
        }
        List<Teacher> teachers = teacherDAO.findByIdTaiKhoan(user.getIdTaiKhoan());
        if (teachers.isEmpty()) {
            return null;
        }
        return teachers.get(0);
    }

    private TeacherInfoService.TeacherInfoView buildFallbackTeacherInfo(String username, User user) {
        String displayName = firstNonBlank(username, "Chưa xác định");
        String email = firstNonBlank(user == null ? null : user.getEmail(), "-");
        return new TeacherInfoService.TeacherInfoView(
                "-",
                displayName,
                null,
                "-",
                "-",
                email,
                "-",
                "-",
                "-",
                null,
                "-",
                "-",
                null,
                "-",
                "-",
                "-",
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private String displayAccountStatus(String status) {
        String normalizedStatus = normalize(status);
        if (normalizedStatus == null) {
            return "-";
        }
        if ("hoat_dong".equalsIgnoreCase(normalizedStatus)) {
            return "Hoạt động";
        }
        if ("khoa".equalsIgnoreCase(normalizedStatus)) {
            return "Khóa";
        }
        return normalizedStatus;
    }

    private String firstNonBlank(String first, String second) {
        String normalizedFirst = normalize(first);
        if (normalizedFirst != null) {
            return normalizedFirst;
        }
        return normalize(second);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeLower(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    public static class TeacherProfilePageData {
        private final String username;
        private final String accountStatus;
        private final Integer accountId;
        private final String teacherId;
        private final TeacherInfoService.TeacherInfoView teacherInfo;
        private final TeacherProfileUpdateForm form;
        private final boolean editable;

        public TeacherProfilePageData(String username,
                                      String accountStatus,
                                      Integer accountId,
                                      String teacherId,
                                      TeacherInfoService.TeacherInfoView teacherInfo,
                                      TeacherProfileUpdateForm form,
                                      boolean editable) {
            this.username = username;
            this.accountStatus = accountStatus;
            this.accountId = accountId;
            this.teacherId = teacherId;
            this.teacherInfo = teacherInfo;
            this.form = form;
            this.editable = editable;
        }

        public String getUsername() {
            return username;
        }

        public String getAccountStatus() {
            return accountStatus;
        }

        public Integer getAccountId() {
            return accountId;
        }

        public String getTeacherId() {
            return teacherId;
        }

        public TeacherInfoService.TeacherInfoView getTeacherInfo() {
            return teacherInfo;
        }

        public TeacherProfileUpdateForm getForm() {
            return form;
        }

        public boolean isEditable() {
            return editable;
        }
    }
}
