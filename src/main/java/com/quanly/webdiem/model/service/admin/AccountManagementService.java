package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.RoleDAO;
import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.Role;
import com.quanly.webdiem.model.entity.TeacherAccountCreateForm;
import com.quanly.webdiem.model.entity.User;
import com.quanly.webdiem.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AccountManagementService {

    private static final String TEACHER_ROLE = "Giao_vien";
    private static final String STATUS_ACTIVE = "hoat_dong";
    private static final String STATUS_LOCKED = "khoa";

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final PasswordHasher passwordHasher;

    public AccountManagementService(UserDAO userDAO, RoleDAO roleDAO, PasswordHasher passwordHasher) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.passwordHasher = passwordHasher;
    }

    public List<AccountRow> getAllAccounts() {
        return userDAO.findAll().stream()
                .sorted(Comparator.comparing(User::getTenDangNhap, String.CASE_INSENSITIVE_ORDER))
                .map(this::mapAccountRow)
                .toList();
    }

    @Transactional
    public void createTeacherAccount(TeacherAccountCreateForm form) {
        String username = normalize(form.getTenDangNhap());
        String password = normalize(form.getMatKhau());
        String email = normalize(form.getEmail());
        String status = normalize(form.getTrangThai());

        if (username == null || password == null) {
            throw new RuntimeException("Ten dang nhap va mat khau khong duoc de trong.");
        }

        if (userDAO.existsByTenDangNhap(username)) {
            throw new RuntimeException("Ten dang nhap da ton tai.");
        }

        if (email != null && userDAO.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email da ton tai.");
        }

        Role teacherRole = roleDAO.findByTenVaiTro(TEACHER_ROLE)
                .orElseThrow(() -> new RuntimeException("Thieu role 'Giao_vien' trong bang roles."));

        User user = new User();
        user.setTenDangNhap(username);
        user.setMatKhau(passwordHasher.encode(password));
        user.setEmail(email);
        user.setVaiTro(teacherRole);
        user.setTrangThai(normalizeStatus(status));

        userDAO.save(user);
    }

    private AccountRow mapAccountRow(User user) {
        String roleName = user.getVaiTro() == null ? null : normalize(user.getVaiTro().getTenVaiTro());
        return new AccountRow(
                normalize(user.getTenDangNhap()),
                normalize(user.getEmail()),
                roleName,
                normalizeStatus(user.getTrangThai())
        );
    }

    private String normalizeStatus(String status) {
        String normalized = normalize(status);
        if (normalized == null) {
            return STATUS_ACTIVE;
        }

        if (STATUS_LOCKED.equalsIgnoreCase(normalized)) {
            return STATUS_LOCKED;
        }
        return STATUS_ACTIVE;
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

    public static class AccountRow {
        private final String tenDangNhap;
        private final String email;
        private final String vaiTro;
        private final String trangThai;

        public AccountRow(String tenDangNhap, String email, String vaiTro, String trangThai) {
            this.tenDangNhap = tenDangNhap;
            this.email = email;
            this.vaiTro = vaiTro;
            this.trangThai = trangThai;
        }

        public String getTenDangNhap() {
            return tenDangNhap;
        }

        public String getEmail() {
            return email;
        }

        public String getVaiTro() {
            return vaiTro;
        }

        public String getTrangThai() {
            return trangThai;
        }
    }
}
