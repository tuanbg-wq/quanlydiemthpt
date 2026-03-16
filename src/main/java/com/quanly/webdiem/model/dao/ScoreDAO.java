package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScoreDAO extends JpaRepository<Score, Integer> {

    @Query(value = """
            SELECT
                s.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), s.id_hoc_sinh) AS tenHocSinh,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), s.id_mon_hoc) AS tenMonHoc,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 1 THEN s.diem END), 1) AS diemMieng,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 2 THEN s.diem END), 1) AS diem15Phut,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 3 THEN s.diem END), 1) AS diem1Tiet,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 4 THEN s.diem END), 1) AS diemGiuaKy,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 5 THEN s.diem END), 1) AS diemCuoiKy,
                ROUND(
                    COALESCE(
                        MAX(av.dtb_mon),
                        SUM(s.diem * COALESCE(stt.he_so, 1)) / NULLIF(SUM(COALESCE(stt.he_so, 1)), 0)
                    ),
                    1
                ) AS tongKet,
                COALESCE(NULLIF(TRIM(MAX(cd.xep_loai)), ''), '-') AS hanhKiem,
                s.hoc_ky AS hocKy,
                s.nam_hoc AS namHoc
            FROM scores s
            LEFT JOIN score_types stt ON stt.id_loai_diem = s.id_loai_diem
            LEFT JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN subjects sb ON LOWER(sb.id_mon_hoc) = LOWER(s.id_mon_hoc)
            LEFT JOIN average_scores av
                ON LOWER(av.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
               AND LOWER(av.id_mon_hoc) = LOWER(s.id_mon_hoc)
               AND av.hoc_ky = s.hoc_ky
               AND av.nam_hoc = s.nam_hoc
            LEFT JOIN conducts cd
                ON LOWER(cd.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
               AND cd.hoc_ky = s.hoc_ky
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
                s.hoc_ky,
                s.nam_hoc
            ORDER BY
                c.khoi ASC,
                c.ten_lop ASC,
                st.ho_ten ASC,
                s.id_hoc_sinh ASC,
                sb.ten_mon_hoc ASC,
                s.nam_hoc DESC,
                s.hoc_ky ASC
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
}
