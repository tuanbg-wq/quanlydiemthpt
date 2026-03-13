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
                CASE
                    WHEN LOWER(COALESCE(t.trang_thai, '')) = 'dang_lam' THEN
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
                        )
                    ELSE '-'
                END AS chuNhiemLop,
                CASE
                    WHEN LOWER(COALESCE(t.trang_thai, '')) = 'dang_lam' THEN
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
                        )
                    ELSE '-'
                END AS lopBoMon,
                CASE
                    WHEN LOWER(COALESCE(t.trang_thai, '')) = 'dang_lam' THEN
                        COALESCE(
                            NULLIF(
                                (
                                    SELECT trt.ten_vai_tro
                                    FROM teacher_roles tr
                                    JOIN teacher_role_types trt ON trt.id_loai_vai_tro = tr.id_loai_vai_tro
                                    WHERE tr.id_giao_vien = t.id_giao_vien
                                    ORDER BY tr.nam_hoc DESC, tr.id DESC
                                    LIMIT 1
                                ),
                                ''
                            ),
                            NULLIF(r.ten_vai_tro, ''),
                            '-'
                        )
                    ELSE '-'
                END AS vaiTro,
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
                    OR EXISTS (
                        SELECT 1
                        FROM subjects s2
                        WHERE LOWER(s2.id_giao_vien_phu_trach) = LOWER(t.id_giao_vien)
                          AND (
                                REPLACE(CONCAT(',', COALESCE(s2.khoi_ap_dung, ''), ','), ' ', '') COLLATE utf8mb4_unicode_ci
                                LIKE CONCAT('%,', CAST(:khoi AS CHAR), ',%') COLLATE utf8mb4_unicode_ci
                          )
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
            SELECT DISTINCT g.khoi
            FROM (
                SELECT c.khoi
                FROM classes c
                WHERE c.id_gvcn IS NOT NULL
                  AND TRIM(c.id_gvcn) <> ''

                UNION

                SELECT c2.khoi
                FROM teaching_assignments ta
                JOIN classes c2 ON c2.id_lop = ta.id_lop

                UNION

                SELECT CAST(TRIM(
                    SUBSTRING_INDEX(
                        SUBSTRING_INDEX(REPLACE(s.khoi_ap_dung, ' ', ''), ',', n.n),
                        ',',
                        -1
                    )
                ) AS UNSIGNED) AS khoi
                FROM subjects s
                JOIN (
                    SELECT 1 AS n UNION ALL
                    SELECT 2 UNION ALL
                    SELECT 3 UNION ALL
                    SELECT 4
                ) n
                  ON n.n <= 1 + LENGTH(REPLACE(s.khoi_ap_dung, ' ', ''))
                             - LENGTH(REPLACE(REPLACE(s.khoi_ap_dung, ' ', ''), ',', ''))
                WHERE s.id_giao_vien_phu_trach IS NOT NULL
                  AND TRIM(s.id_giao_vien_phu_trach) <> ''
                  AND s.khoi_ap_dung IS NOT NULL
                  AND TRIM(s.khoi_ap_dung) <> ''
            ) g
            WHERE g.khoi IS NOT NULL
            ORDER BY g.khoi
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

    @Query(value = """
            SELECT COUNT(*)
            FROM classes c
            WHERE LOWER(c.id_gvcn) = LOWER(:teacherId)
            """, nativeQuery = true)
    long countHomeroomClassReferences(@Param("teacherId") String teacherId);

    @Query(value = """
            SELECT COUNT(*)
            FROM teaching_assignments ta
            WHERE LOWER(ta.id_giao_vien) = LOWER(:teacherId)
            """, nativeQuery = true)
    long countTeachingAssignmentReferences(@Param("teacherId") String teacherId);

    @Query(value = """
            SELECT
                c.nam_hoc AS namHoc,
                GROUP_CONCAT(DISTINCT c.ten_lop ORDER BY c.ten_lop SEPARATOR ', ') AS classNames
            FROM classes c
            WHERE LOWER(c.id_gvcn) = LOWER(:teacherId)
            GROUP BY c.nam_hoc
            ORDER BY c.nam_hoc DESC
            """, nativeQuery = true)
    List<Object[]> findHomeroomHistoryByTeacherId(@Param("teacherId") String teacherId);

    @Query(value = """
            SELECT
                ta.nam_hoc AS namHoc,
                GROUP_CONCAT(DISTINCT COALESCE(NULLIF(TRIM(s.ten_mon_hoc), ''), NULLIF(TRIM(t.chuyen_mon), ''))
                    ORDER BY s.ten_mon_hoc SEPARATOR ', ') AS subjectNames,
                GROUP_CONCAT(DISTINCT c.ten_lop ORDER BY c.ten_lop SEPARATOR ', ') AS classNames
            FROM teaching_assignments ta
            JOIN teachers t ON t.id_giao_vien = ta.id_giao_vien
            LEFT JOIN subjects s ON s.id_mon_hoc = ta.id_mon_hoc
            LEFT JOIN classes c ON c.id_lop = ta.id_lop
            WHERE LOWER(ta.id_giao_vien) = LOWER(:teacherId)
            GROUP BY ta.nam_hoc
            ORDER BY ta.nam_hoc DESC
            """, nativeQuery = true)
    List<Object[]> findSubjectAssignmentHistoryByTeacherId(@Param("teacherId") String teacherId);

    @Query(value = """
            SELECT
                tr.nam_hoc AS namHoc,
                trt.ten_vai_tro AS roleName
            FROM teacher_roles tr
            JOIN teacher_role_types trt ON trt.id_loai_vai_tro = tr.id_loai_vai_tro
            WHERE LOWER(tr.id_giao_vien) = LOWER(:teacherId)
            ORDER BY tr.nam_hoc DESC, tr.id DESC
            """, nativeQuery = true)
    List<Object[]> findRoleHistoryByTeacherId(@Param("teacherId") String teacherId);
}

