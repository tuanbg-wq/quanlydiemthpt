package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassDAO extends JpaRepository<ClassEntity, String> {
}