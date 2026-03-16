package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface ScoreDAO extends JpaRepository<Score, Integer> {

    @Query(value = """
            SELECT
                s.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), s.id_hoc_sinh) AS tenHocSinh,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                s.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), s.id_mon_hoc) AS tenMonHoc,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 4 THEN s.diem END), 1) AS diemGiuaKy,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 5 THEN s.diem END), 1) AS diemCuoiKy,
                ROUND(
                    COALESCE(
                        MAX(av.dtb_nam_hoc),
                        MAX(av.dtb_hoc_ky),
                        SUM(s.diem * COALESCE(stt.he_so, 1)) / NULLIF(SUM(COALESCE(stt.he_so, 1)), 0)
                    ),
                    1
                ) AS tongKet,
                COALESCE(NULLIF(TRIM(MAX(cd.xep_loai)), ''), '-') AS hanhKiem,
                s.nam_hoc AS namHoc
            FROM scores s
            LEFT JOIN score_types stt ON stt.id_loai_diem = s.id_loai_diem
            LEFT JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN subjects sb ON LOWER(sb.id_mon_hoc) = LOWER(s.id_mon_hoc)
            LEFT JOIN average_scores av
                ON LOWER(av.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
               AND LOWER(av.id_mon_hoc) = LOWER(s.id_mon_hoc)
               AND av.nam_hoc = s.nam_hoc
            LEFT JOIN conducts cd
                ON LOWER(cd.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
               AND cd.nam_hoc = s.nam_hoc
            WHERE (
                    :q IS NULL OR :q = '' OR
                    LOWER(st.ho_ten) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(s.id_hoc_sinh) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(sb.ten_mon_hoc) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(s.id_mon_hoc) LIKE CONCAT('%', LOWER(:q), '%')
                )
              AND (
                    :khoi IS NULL OR
                    c.khoi = :khoi
                )
              AND (
                    :classId IS NULL OR :classId = '' OR
                    LOWER(c.id_lop) = LOWER(:classId)
                )
              AND (
                    :subjectId IS NULL OR :subjectId = '' OR
                    LOWER(s.id_mon_hoc) = LOWER(:subjectId)
                )
              AND (
                    :hocKy IS NULL OR
                    :hocKy = 0 OR
                    s.hoc_ky = :hocKy
                )
              AND (
                    :courseId IS NULL OR :courseId = '' OR
                    LOWER(c.id_khoa) = LOWER(:courseId)
                )
            GROUP BY
                s.id_hoc_sinh,
                st.ho_ten,
                c.khoi,
                c.ten_lop,
                st.id_lop,
                s.id_mon_hoc,
                sb.ten_mon_hoc,
                s.nam_hoc
            ORDER BY
                c.khoi ASC,
                c.ten_lop ASC,
                st.ho_ten ASC,
                s.id_hoc_sinh ASC,
                sb.ten_mon_hoc ASC,
                s.nam_hoc DESC
            """, nativeQuery = true)
    List<Object[]> searchForManagement(@Param("q") String q,
                                       @Param("khoi") Integer khoi,
                                       @Param("classId") String classId,
                                       @Param("subjectId") String subjectId,
                                       @Param("hocKy") Integer hocKy,
                                       @Param("courseId") String courseId);

    @Query(value = """
            SELECT DISTINCT c.khoi
            FROM scores s
            JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            WHERE c.khoi IS NOT NULL
            ORDER BY c.khoi ASC
            """, nativeQuery = true)
    List<Integer> findDistinctGrades();

    @Query(value = """
            SELECT
                c.id_lop AS idLop,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), c.id_lop) AS tenLop,
                c.khoi AS khoi
            FROM scores s
            JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            GROUP BY c.id_lop, c.ten_lop, c.khoi
            ORDER BY c.khoi ASC, c.ten_lop ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctClassesForFilter();

    @Query(value = """
            SELECT
                sb.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), sb.id_mon_hoc) AS tenMonHoc
            FROM scores s
            JOIN subjects sb ON LOWER(sb.id_mon_hoc) = LOWER(s.id_mon_hoc)
            GROUP BY sb.id_mon_hoc, sb.ten_mon_hoc
            ORDER BY sb.ten_mon_hoc ASC, sb.id_mon_hoc ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctSubjectsForFilter();

    @Query(value = """
            SELECT
                c.id_khoa AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa) AS tenKhoa
            FROM scores s
            JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE c.id_khoa IS NOT NULL
              AND TRIM(c.id_khoa) <> ''
            GROUP BY c.id_khoa, k.ten_khoa
            ORDER BY c.id_khoa ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctCoursesForFilter();

    @Query(value = """
            SELECT COUNT(DISTINCT s.id_hoc_sinh)
            FROM scores s
            """, nativeQuery = true)
    long countDistinctStudentsWithScores();

    @Query(value = """
            SELECT ROUND(AVG(s.diem), 1)
            FROM scores s
            """, nativeQuery = true)
    Double calculateSchoolAverage();

    @Query(value = """
            SELECT COUNT(*)
            FROM (
                SELECT
                    s.id_hoc_sinh,
                    s.id_mon_hoc,
                    s.nam_hoc,
                    s.hoc_ky
                FROM scores s
                GROUP BY s.id_hoc_sinh, s.id_mon_hoc, s.nam_hoc, s.hoc_ky
            ) grouped_scores
            """, nativeQuery = true)
    long countScoreGroups();

    @Query(value = """
            SELECT COUNT(*)
            FROM (
                SELECT
                    SUM(s.diem * COALESCE(stt.he_so, 1)) / NULLIF(SUM(COALESCE(stt.he_so, 1)), 0) AS tongKet
                FROM scores s
                LEFT JOIN score_types stt ON stt.id_loai_diem = s.id_loai_diem
                GROUP BY s.id_hoc_sinh, s.id_mon_hoc, s.nam_hoc, s.hoc_ky
                HAVING tongKet >= 6.5
            ) grouped_scores
            """, nativeQuery = true)
    long countGoodScoreGroups();

    @Query(value = """
            SELECT
                s.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), s.id_hoc_sinh) AS tenHocSinh,
                s.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), s.id_mon_hoc) AS tenMonHoc,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                s.nam_hoc AS namHoc
            FROM scores s
            LEFT JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN subjects sb ON LOWER(sb.id_mon_hoc) = LOWER(s.id_mon_hoc)
            WHERE LOWER(s.id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(s.id_mon_hoc) = LOWER(:subjectId)
              AND s.nam_hoc = :namHoc
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findScoreGroupSummary(@Param("studentId") String studentId,
                                         @Param("subjectId") String subjectId,
                                         @Param("namHoc") String namHoc);

    @Query(value = """
            SELECT
                s.id_diem AS idDiem,
                s.hoc_ky AS hocKy,
                s.id_loai_diem AS idLoaiDiem,
                CASE
                    WHEN s.id_loai_diem = 4 THEN 'Giữa kỳ (HS2)'
                    WHEN s.id_loai_diem = 5 THEN 'Cuối kỳ (HS3)'
                    ELSE 'Đánh giá thường xuyên (HS1)'
                END AS tenLoaiDiem,
                s.diem AS diem,
                COALESCE(DATE_FORMAT(s.ngay_nhap, '%d/%m/%Y'), '') AS ngayNhap,
                COALESCE(NULLIF(TRIM(s.ghi_chu), ''), '') AS ghiChu
            FROM scores s
            LEFT JOIN score_types stt ON stt.id_loai_diem = s.id_loai_diem
            WHERE LOWER(s.id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(s.id_mon_hoc) = LOWER(:subjectId)
              AND s.nam_hoc = :namHoc
            ORDER BY s.hoc_ky ASC, s.id_loai_diem ASC, s.ngay_nhap ASC, s.id_diem ASC
            """, nativeQuery = true)
    List<Object[]> findScoreEntriesByGroup(@Param("studentId") String studentId,
                                           @Param("subjectId") String subjectId,
                                           @Param("namHoc") String namHoc);

    @Query("""
            SELECT s
            FROM Score s
            WHERE LOWER(s.idHocSinh) = LOWER(:studentId)
              AND LOWER(s.idMonHoc) = LOWER(:subjectId)
              AND s.namHoc = :namHoc
            ORDER BY s.hocKy ASC, s.idLoaiDiem ASC, s.idDiem ASC
            """)
    List<Score> findScoresForEdit(@Param("studentId") String studentId,
                                  @Param("subjectId") String subjectId,
                                  @Param("namHoc") String namHoc);

    @Transactional
    long deleteByIdHocSinhIgnoreCaseAndIdMonHocIgnoreCaseAndNamHoc(String idHocSinh,
                                                                    String idMonHoc,
                                                                    String namHoc);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM average_scores
            WHERE LOWER(id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(id_mon_hoc) = LOWER(:subjectId)
              AND nam_hoc = :namHoc
            """, nativeQuery = true)
    int deleteAverageScoresByGroup(@Param("studentId") String studentId,
                                   @Param("subjectId") String subjectId,
                                   @Param("namHoc") String namHoc);

    @Query(value = """
            SELECT sy.nam_hoc
            FROM school_years sy
            ORDER BY sy.ngay_bat_dau DESC
            """, nativeQuery = true)
    List<String> findSchoolYearsForCreate();

    @Query(value = """
            SELECT
                c.id_khoa AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa) AS tenKhoa
            FROM classes c
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE c.id_khoa IS NOT NULL
              AND TRIM(c.id_khoa) <> ''
            GROUP BY c.id_khoa, k.ten_khoa
            ORDER BY c.id_khoa ASC
            """, nativeQuery = true)
    List<Object[]> findCoursesForCreate();

    @Query(value = """
            SELECT DISTINCT c.khoi
            FROM classes c
            WHERE c.khoi IS NOT NULL
            ORDER BY c.khoi ASC
            """, nativeQuery = true)
    List<Integer> findGradesForCreate();

    @Query(value = """
            SELECT
                c.id_lop AS idLop,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), c.id_lop) AS tenLop
            FROM classes c
            WHERE (:grade IS NULL OR c.khoi = :grade)
              AND (:courseId IS NULL OR :courseId = '' OR LOWER(c.id_khoa) = LOWER(:courseId))
              AND (:namHoc IS NULL OR :namHoc = '' OR c.nam_hoc = :namHoc)
            ORDER BY c.khoi ASC, c.ten_lop ASC, c.id_lop ASC
            """, nativeQuery = true)
    List<Object[]> findClassesForCreate(@Param("grade") Integer grade,
                                        @Param("courseId") String courseId,
                                        @Param("namHoc") String namHoc);

    @Query(value = """
            SELECT
                sb.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), sb.id_mon_hoc) AS tenMonHoc
            FROM subjects sb
            WHERE (
                    :grade IS NULL OR
                    REPLACE(CONCAT(',', COALESCE(sb.khoi_ap_dung, ''), ','), ' ', '') COLLATE utf8mb4_unicode_ci
                    LIKE CONCAT('%,', CAST(:grade AS CHAR), ',%') COLLATE utf8mb4_unicode_ci
                )
            ORDER BY sb.ten_mon_hoc ASC, sb.id_mon_hoc ASC
            """, nativeQuery = true)
    List<Object[]> findSubjectsForCreate(@Param("grade") Integer grade);

    @Query(value = """
            SELECT COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), sb.id_mon_hoc)
            FROM subjects sb
            WHERE LOWER(sb.id_mon_hoc) = LOWER(:subjectId)
            LIMIT 1
            """, nativeQuery = true)
    String findSubjectNameById(@Param("subjectId") String subjectId);

    @Query(value = """
            SELECT
                st.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), st.id_hoc_sinh) AS hoTen,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop
            FROM students st
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            WHERE (:classId IS NULL OR :classId = '' OR LOWER(st.id_lop) = LOWER(:classId))
              AND (
                    :q IS NULL OR :q = '' OR
                    LOWER(st.id_hoc_sinh) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(st.ho_ten) LIKE CONCAT('%', LOWER(:q), '%')
                )
            ORDER BY st.ho_ten ASC, st.id_hoc_sinh ASC
            LIMIT 120
            """, nativeQuery = true)
    List<Object[]> findStudentsForCreate(@Param("classId") String classId,
                                         @Param("q") String q);

    @Query(value = """
            SELECT
                s.hoc_ky AS hocKy,
                s.id_loai_diem AS idLoaiDiem,
                s.diem AS diem,
                COALESCE(NULLIF(TRIM(s.ghi_chu), ''), '') AS ghiChu
            FROM scores s
            WHERE LOWER(s.id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(s.id_mon_hoc) = LOWER(:subjectId)
              AND s.nam_hoc = :namHoc
            ORDER BY s.hoc_ky ASC, s.id_loai_diem ASC, s.id_diem ASC
            """, nativeQuery = true)
    List<Object[]> findRawScoreEntriesForCreate(@Param("studentId") String studentId,
                                                @Param("subjectId") String subjectId,
                                                @Param("namHoc") String namHoc);

    @Query(value = """
            SELECT
                st.id_loai_diem AS idLoaiDiem,
                COALESCE(st.he_so, 1) AS heSo,
                COALESCE(NULLIF(TRIM(st.ten_loai), ''), '') AS tenLoai
            FROM score_types st
            ORDER BY st.id_loai_diem ASC
            """, nativeQuery = true)
    List<Object[]> findScoreTypeDefinitions();

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM scores
            WHERE LOWER(id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(id_mon_hoc) = LOWER(:subjectId)
              AND nam_hoc = :namHoc
              AND hoc_ky = :hocKy
            """, nativeQuery = true)
    int deleteScoresByGroupAndSemester(@Param("studentId") String studentId,
                                       @Param("subjectId") String subjectId,
                                       @Param("namHoc") String namHoc,
                                       @Param("hocKy") Integer hocKy);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO scores
                (id_hoc_sinh, id_mon_hoc, id_loai_diem, nam_hoc, hoc_ky, diem, ngay_nhap, ghi_chu)
            VALUES
                (:studentId, :subjectId, :scoreTypeId, :namHoc, :hocKy, :scoreValue, CURRENT_DATE(), :note)
            """, nativeQuery = true)
    int insertScoreEntry(@Param("studentId") String studentId,
                         @Param("subjectId") String subjectId,
                         @Param("scoreTypeId") Integer scoreTypeId,
                         @Param("namHoc") String namHoc,
                         @Param("hocKy") Integer hocKy,
                         @Param("scoreValue") BigDecimal scoreValue,
                         @Param("note") String note);
}
