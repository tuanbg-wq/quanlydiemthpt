package com.quanly.webdiem.model.service;

import com.quanly.webdiem.model.dao.RoleDAO;
import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.Role;
import com.quanly.webdiem.model.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserDAO userDAO, RoleDAO roleDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerTeacher(RegisterRequest req) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String password = req.getPassword() == null ? "" : req.getPassword().trim();

        if (username.isEmpty() || password.isEmpty()) {
            throw new RuntimeException("Tên đăng nhập và mật khẩu không được để trống.");
        }
        if (userDAO.existsByTenDangNhap(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }

        // role trong DB bạn đang có: 'Giao_vien'
        Role roleGV = roleDAO.findByTenVaiTro("Giao_vien")
                .orElseThrow(() -> new RuntimeException("Thiếu role 'Giao_vien' trong bảng roles."));

        User u = new User();
        u.setTenDangNhap(username);
        u.setMatKhau(passwordEncoder.encode(password));
        u.setVaiTro(roleGV);
        u.setEmail(req.getEmail() == null || req.getEmail().isBlank() ? null : req.getEmail().trim());
        u.setTrangThai("hoat_dong");

        userDAO.save(u);
    }
}
