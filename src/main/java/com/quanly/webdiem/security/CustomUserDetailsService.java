package com.quanly.webdiem.security;

import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDAO userDAO;

    public CustomUserDetailsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userDAO.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));

        // Role trong DB: Admin / Giao_vien / Hoc_sinh
        String authority = "ROLE_" + u.getVaiTro().getTenVaiTro();

        boolean enabled = !"khoa".equalsIgnoreCase(u.getTrangThai());

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getTenDangNhap())
                .password(u.getMatKhau())
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .disabled(!enabled)
                .build();
    }
}
