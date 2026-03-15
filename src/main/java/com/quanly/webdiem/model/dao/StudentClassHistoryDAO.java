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

    @Query(value = """
            SELECT
                h.id_hoc_sinh AS studentId,
                COALESCE(NULLIF(TRIM(s.ho_ten), ''), h.id_hoc_sinh) AS studentName,
                COALESCE(NULLIF(TRIM(h.lop_cu), ''), '-') AS lopCu,
                COALESCE(NULLIF(TRIM(h.lop_moi), ''), '-') AS lopMoi,
                h.ngay_chuyen AS ngayChuyen,
                COALESCE(NULLIF(TRIM(h.loai_chuyen), ''), '-') AS loaiChuyen,
                COALESCE(NULLIF(TRIM(h.ghi_chu), ''), '') AS ghiChu
            FROM student_class_history h
            LEFT JOIN students s ON LOWER(s.id_hoc_sinh) = LOWER(h.id_hoc_sinh)
            WHERE LOWER(COALESCE(h.lop_cu, '')) = LOWER(:classId)
               OR LOWER(COALESCE(h.lop_moi, '')) = LOWER(:classId)
            ORDER BY h.ngay_chuyen DESC, h.id DESC
            LIMIT 80
            """, nativeQuery = true)
    List<Object[]> findClassTransferHistory(@Param("classId") String classId);
}
