package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherDAO extends JpaRepository<Teacher, String> {

    @Query(value = """
            SELECT COUNT(*)
            FROM teachers t
            WHERE LOWER(t.email) = LOWER(:email)
            """, nativeQuery = true)
    long countByEmailIgnoreCase(@Param("email") String email);

    @Query(value = """
            SELECT COUNT(*)
            FROM teachers t
            WHERE LOWER(t.email) = LOWER(:email)
              AND LOWER(t.id_giao_vien) <> LOWER(:teacherId)
            """, nativeQuery = true)
    long countByEmailIgnoreCaseAndIdGiaoVienNot(@Param("email") String email,
                                                 @Param("teacherId") String teacherId);

    @Query(value = """
            SELECT MAX(CAST(SUBSTRING(t.id_giao_vien, 3) AS UNSIGNED))
            FROM teachers t
            WHERE t.id_giao_vien REGEXP '^GV[0-9]+$'
            """, nativeQuery = true)
    Integer findMaxTeacherCodeNumber();

    @Query(value = """
            SELECT
                t.id_giao_vien AS idGiaoVien,
                COALESCE(t.ho_ten, '-') AS hoTen,
                COALESCE(DATE_FORMAT(t.ngay_sinh, '%d/%m/%Y'), '-') AS ngaySinh,
                COALESCE(t.gioi_tinh, '-') AS gioiTinh,
                COALESCE(t.so_dien_thoai, '-') AS soDienThoai,
                COALESCE(t.email, '-') AS email,
                COALESCE(
                    NULLIF(
                        TRIM(COALESCE(t.chuyen_mon, '')),
                        ''
                    ),
                    NULLIF(
                        (
                            SELECT GROUP_CONCAT(DISTINCT sx.ten_mon_hoc ORDER BY sx.ten_mon_hoc SEPARATOR ', ')
                            FROM teaching_assignments tax
                            JOIN subjects sx ON sx.id_mon_hoc = tax.id_mon_hoc
                            WHERE tax.id_giao_vien = t.id_giao_vien
                        ),
                        ''
                    ),
                    '-'
                ) AS monDay,
                COALESCE(
                    NULLIF(
                        (
                            SELECT c.ten_lop
                            FROM classes c
                            WHERE c.id_gvcn = t.id_giao_vien
                            LIMIT 1
                        ),
                        ''
                    ),
                    '-'
                ) AS chuNhiemLop,
                COALESCE(
                    NULLIF(
                        (
                            SELECT GROUP_CONCAT(DISTINCT c2.ten_lop ORDER BY c2.ten_lop SEPARATOR ', ')
                            FROM teaching_assignments ta2
                            JOIN classes c2 ON c2.id_lop = ta2.id_lop
                            WHERE ta2.id_giao_vien = t.id_giao_vien
                        ),
                        ''
                    ),
                    '-'
                ) AS lopBoMon,
                COALESCE(r.ten_vai_tro, '-') AS vaiTro,
                COALESCE(t.trang_thai, '-') AS trangThai,
                COALESCE(t.anh, '') AS avatar
            FROM teachers t
            LEFT JOIN users u ON u.id_tai_khoan = t.id_tai_khoan
            LEFT JOIN roles r ON r.id_vai_tro = u.id_vai_tro
            WHERE
                (
                    :q IS NULL OR :q = '' OR
                    LOWER(t.id_giao_vien) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(t.ho_ten) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(t.email, '')) LIKE CONCAT('%', LOWER(:q), '%')
                )
                AND (
                    :trangThai IS NULL OR :trangThai = '' OR
                    LOWER(t.trang_thai) = LOWER(:trangThai)
                )
                AND (
                    :boMon IS NULL OR :boMon = '' OR
                    LOWER(COALESCE(t.chuyen_mon, '')) = LOWER(:boMon) OR
                    EXISTS (
                        SELECT 1
                        FROM teaching_assignments tab
                        JOIN subjects sb ON sb.id_mon_hoc = tab.id_mon_hoc
                        WHERE tab.id_giao_vien = t.id_giao_vien
                          AND LOWER(sb.ten_mon_hoc) = LOWER(:boMon)
                    )
                )
                AND (
                    :khoi IS NULL OR :khoi = '' OR
                    EXISTS (
                        SELECT 1
                        FROM classes c3
                        WHERE c3.id_gvcn = t.id_giao_vien
                          AND c3.khoi = CAST(:khoi AS UNSIGNED)
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM teaching_assignments ta3
                        JOIN classes c4 ON c4.id_lop = ta3.id_lop
                        WHERE ta3.id_giao_vien = t.id_giao_vien
                          AND c4.khoi = CAST(:khoi AS UNSIGNED)
                    )
                )
            ORDER BY t.id_giao_vien ASC
            """, nativeQuery = true)
    List<Object[]> searchForManagement(@Param("q") String q,
                                       @Param("boMon") String boMon,
                                       @Param("khoi") String khoi,
                                       @Param("trangThai") String trangThai);

    @Query(value = """
            SELECT DISTINCT s.ten_mon_hoc
            FROM subjects s
            WHERE s.ten_mon_hoc IS NOT NULL
              AND TRIM(s.ten_mon_hoc) <> ''
            ORDER BY s.ten_mon_hoc
            """, nativeQuery = true)
    List<String> findDistinctSubjects();

    @Query(value = """
            SELECT DISTINCT c.khoi
            FROM classes c
            WHERE c.khoi IS NOT NULL
            ORDER BY c.khoi
            """, nativeQuery = true)
    List<Integer> findDistinctGrades();

    @Query(value = """
            SELECT DISTINCT t.trang_thai
            FROM teachers t
            WHERE t.trang_thai IS NOT NULL
              AND TRIM(t.trang_thai) <> ''
            ORDER BY t.trang_thai
            """, nativeQuery = true)
    List<String> findDistinctStatuses();
}

