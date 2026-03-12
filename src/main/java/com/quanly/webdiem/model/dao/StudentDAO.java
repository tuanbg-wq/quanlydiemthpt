package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentDAO extends JpaRepository<Student, String> {

    /**
     * Search giống UI:
     * - q: tìm theo mã, họ tên, email, tên lớp
     * - courseId: lọc theo khóa
     * - khoi: lọc theo khối
     * - classId: lọc theo lớp
     */
    @Query("""
    SELECT s
    FROM Student s
    LEFT JOIN s.lop c
    LEFT JOIN c.khoaHoc k
    WHERE
        (
            :q IS NULL OR :q = '' OR
            LOWER(s.idHocSinh) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(s.hoTen) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(s.email,'')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.tenLop,'')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        AND ( :classId IS NULL OR :classId = '' OR c.idLop = :classId )
        AND ( :courseId IS NULL OR :courseId = '' OR k.idKhoa = :courseId )
        AND ( :khoi IS NULL OR :khoi = '' OR c.khoi = CAST(:khoi AS integer) )
    ORDER BY s.ngayTao DESC
""")
    List<Student> search(
            @Param("q") String q,
            @Param("courseId") String courseId,
            @Param("khoi") String khoi,
            @Param("classId") String classId
    );

    @Modifying
    @Query(
            value = """
            UPDATE students
            SET id_hoc_sinh = :newId
            WHERE id_hoc_sinh = :oldId
            """,
            nativeQuery = true
    )
    int updateStudentId(@Param("oldId") String oldId, @Param("newId") String newId);

    List<Student> findTop10ByOrderByNgayTaoDesc();
}
