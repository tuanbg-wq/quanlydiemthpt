package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseDAO extends JpaRepository<Course, String> {
}