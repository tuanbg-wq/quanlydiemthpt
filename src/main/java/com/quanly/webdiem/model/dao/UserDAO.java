package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDAO extends JpaRepository<User, Integer> {
    Optional<User> findByTenDangNhap(String tenDangNhap);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmailIgnoreCase(String email);

    @Query(value = """
            SELECT
                u.id_tai_khoan AS idTaiKhoan,
                u.ten_dang_nhap AS tenDangNhap,
                COALESCE(t.ho_ten, '-') AS hoTen,
                CASE
                    WHEN LOWER(COALESCE(r.ten_vai_tro, '')) = 'admin' THEN 'Admin'
                    WHEN LOWER(COALESCE(r.ten_vai_tro, '')) = 'gvcn' THEN 'GVCN'
                    WHEN LOWER(COALESCE(r.ten_vai_tro, '')) = 'gvbm' THEN 'Giao vien bo mon'
                    WHEN LOWER(COALESCE(r.ten_vai_tro, '')) = 'giao_vien' THEN 'Giao vien bo mon'
                    ELSE COALESCE(r.ten_vai_tro, '-')
                END AS vaiTro,
                COALESCE(u.trang_thai, 'hoat_dong') AS trangThai,
                COALESCE(u.email, '') AS email,
                COALESCE(grade.khoi_lop, '-') AS khoiLop
            FROM users u
            LEFT JOIN roles r ON r.id_vai_tro = u.id_vai_tro
            LEFT JOIN teachers t ON t.id_tai_khoan = u.id_tai_khoan
            LEFT JOIN (
                SELECT
                    source.id_tai_khoan,
                    GROUP_CONCAT(DISTINCT source.khoi ORDER BY source.khoi SEPARATOR ', ') AS khoi_lop
                FROM (
                    SELECT t1.id_tai_khoan, c1.khoi
                    FROM teachers t1
                    JOIN classes c1 ON LOWER(c1.id_gvcn) = LOWER(t1.id_giao_vien)
                    WHERE t1.id_tai_khoan IS NOT NULL

                    UNION

                    SELECT t2.id_tai_khoan, c2.khoi
                    FROM teachers t2
                    JOIN teaching_assignments ta ON LOWER(ta.id_giao_vien) = LOWER(t2.id_giao_vien)
                    JOIN classes c2 ON c2.id_lop = ta.id_lop
                    WHERE t2.id_tai_khoan IS NOT NULL
                ) source
                GROUP BY source.id_tai_khoan
            ) grade ON grade.id_tai_khoan = u.id_tai_khoan
            WHERE
                (
                    :q IS NULL OR :q = '' OR
                    LOWER(u.ten_dang_nhap) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(t.ho_ten, '')) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(u.email, '')) LIKE CONCAT('%', LOWER(:q), '%')
                )
                AND (
                    :vaiTro IS NULL OR :vaiTro = '' OR
                    (
                        LOWER(:vaiTro) = 'admin'
                        AND LOWER(COALESCE(r.ten_vai_tro, '')) = 'admin'
                    )
                    OR (
                        LOWER(:vaiTro) = 'gvcn'
                        AND LOWER(COALESCE(r.ten_vai_tro, '')) = 'gvcn'
                    )
                    OR (
                        LOWER(:vaiTro) = 'gvbm'
                        AND LOWER(COALESCE(r.ten_vai_tro, '')) IN ('gvbm', 'giao_vien')
                    )
                )
                AND (
                    :trangThai IS NULL OR :trangThai = '' OR
                    LOWER(COALESCE(u.trang_thai, 'hoat_dong')) = LOWER(:trangThai)
                )
                AND (
                    :khoi IS NULL OR :khoi = '' OR
                    EXISTS (
                        SELECT 1
                        FROM classes c3
                        WHERE LOWER(c3.id_gvcn) = LOWER(t.id_giao_vien)
                          AND c3.khoi = CAST(:khoi AS UNSIGNED)
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM teaching_assignments ta3
                        JOIN classes c4 ON c4.id_lop = ta3.id_lop
                        WHERE LOWER(ta3.id_giao_vien) = LOWER(t.id_giao_vien)
                          AND c4.khoi = CAST(:khoi AS UNSIGNED)
                    )
                )
            ORDER BY u.id_tai_khoan DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> searchAccounts(@Param("q") String q,
                                  @Param("vaiTro") String vaiTro,
                                  @Param("trangThai") String trangThai,
                                  @Param("khoi") String khoi,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*)
            FROM users u
            LEFT JOIN roles r ON r.id_vai_tro = u.id_vai_tro
            LEFT JOIN teachers t ON t.id_tai_khoan = u.id_tai_khoan
            WHERE
                (
                    :q IS NULL OR :q = '' OR
                    LOWER(u.ten_dang_nhap) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(t.ho_ten, '')) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(u.email, '')) LIKE CONCAT('%', LOWER(:q), '%')
                )
                AND (
                    :vaiTro IS NULL OR :vaiTro = '' OR
                    (
                        LOWER(:vaiTro) = 'admin'
                        AND LOWER(COALESCE(r.ten_vai_tro, '')) = 'admin'
                    )
                    OR (
                        LOWER(:vaiTro) = 'gvcn'
                        AND LOWER(COALESCE(r.ten_vai_tro, '')) = 'gvcn'
                    )
                    OR (
                        LOWER(:vaiTro) = 'gvbm'
                        AND LOWER(COALESCE(r.ten_vai_tro, '')) IN ('gvbm', 'giao_vien')
                    )
                )
                AND (
                    :trangThai IS NULL OR :trangThai = '' OR
                    LOWER(COALESCE(u.trang_thai, 'hoat_dong')) = LOWER(:trangThai)
                )
                AND (
                    :khoi IS NULL OR :khoi = '' OR
                    EXISTS (
                        SELECT 1
                        FROM classes c3
                        WHERE LOWER(c3.id_gvcn) = LOWER(t.id_giao_vien)
                          AND c3.khoi = CAST(:khoi AS UNSIGNED)
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM teaching_assignments ta3
                        JOIN classes c4 ON c4.id_lop = ta3.id_lop
                        WHERE LOWER(ta3.id_giao_vien) = LOWER(t.id_giao_vien)
                          AND c4.khoi = CAST(:khoi AS UNSIGNED)
                    )
                )
            """, nativeQuery = true)
    long countSearchAccounts(@Param("q") String q,
                             @Param("vaiTro") String vaiTro,
                             @Param("trangThai") String trangThai,
                             @Param("khoi") String khoi);

    @Query(value = """
            SELECT DISTINCT ten_vai_tro
            FROM roles
            WHERE ten_vai_tro IS NOT NULL
              AND TRIM(ten_vai_tro) <> ''
            ORDER BY ten_vai_tro
            """, nativeQuery = true)
    List<String> findRoleNames();

    @Query(value = """
            SELECT DISTINCT khoi
            FROM classes
            WHERE khoi IS NOT NULL
            ORDER BY khoi
            """, nativeQuery = true)
    List<Integer> findGradeOptions();

    @Query(value = """
            SELECT COUNT(*)
            FROM users
            """, nativeQuery = true)
    long countAllAccounts();

    @Query(value = """
            SELECT COUNT(*)
            FROM users u
            JOIN roles r ON r.id_vai_tro = u.id_vai_tro
            WHERE LOWER(r.ten_vai_tro) = 'admin'
            """, nativeQuery = true)
    long countAdminAccounts();

    @Query(value = """
            SELECT COUNT(DISTINCT u.id_tai_khoan)
            FROM users u
            JOIN roles r ON r.id_vai_tro = u.id_vai_tro
            JOIN teachers t ON t.id_tai_khoan = u.id_tai_khoan
            JOIN classes c ON LOWER(c.id_gvcn) = LOWER(t.id_giao_vien)
            WHERE LOWER(r.ten_vai_tro) = 'gvcn'
              AND LOWER(COALESCE(u.trang_thai, 'hoat_dong')) = 'hoat_dong'
            """, nativeQuery = true)
    long countActiveHomeroomTeacherAccounts();

    @Query(value = """
            SELECT COUNT(DISTINCT u.id_tai_khoan)
            FROM users u
            JOIN roles r ON r.id_vai_tro = u.id_vai_tro
            JOIN teachers t ON t.id_tai_khoan = u.id_tai_khoan
            JOIN teaching_assignments ta ON LOWER(ta.id_giao_vien) = LOWER(t.id_giao_vien)
            WHERE LOWER(r.ten_vai_tro) IN ('giao_vien', 'gvbm')
              AND LOWER(COALESCE(u.trang_thai, 'hoat_dong')) = 'hoat_dong'
            """, nativeQuery = true)
    long countActiveSubjectTeacherAccounts();
}
