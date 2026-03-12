package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.StudentClassHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentClassHistoryDAO extends JpaRepository<StudentClassHistory, Integer> {

    // Lấy toàn bộ lịch sử của 1 học sinh, mới nhất trước
    List<StudentClassHistory> findByStudentIdOrderByNgayChuyenDesc(String studentId);

    // Kiểm tra học sinh có lịch sử theo loại không
    boolean existsByStudentIdAndLoaiChuyen(String studentId, String loaiChuyen);

    // Lấy lịch sử mới nhất theo loại
    Optional<StudentClassHistory> findFirstByStudentIdAndLoaiChuyenOrderByNgayChuyenDesc(
            String studentId,
            String loaiChuyen
    );

    @Modifying
    @Query(
            value = """
            UPDATE student_class_history
            SET id_hoc_sinh = :newId
            WHERE id_hoc_sinh = :oldId
            """,
            nativeQuery = true
    )
    int updateStudentId(@Param("oldId") String oldId, @Param("newId") String newId);
}
