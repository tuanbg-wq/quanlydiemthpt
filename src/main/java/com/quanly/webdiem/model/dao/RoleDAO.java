package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleDAO extends JpaRepository<Role, Integer> {
    Optional<Role> findByTenVaiTro(String tenVaiTro);
}
