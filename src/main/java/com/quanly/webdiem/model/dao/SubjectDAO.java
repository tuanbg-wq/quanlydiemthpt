package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectDAO extends JpaRepository<Subject, String> {

    @Query(value = """
            SELECT
                s.id_mon_hoc AS idMonHoc,
                s.ten_mon_hoc AS tenMonHoc,
                COALESCE(s.khoi_ap_dung, '') AS khoiApDung,
                COALESCE(s.hoc_ky_ap_dung, '') AS hocKyApDung,
                COALESCE(s.to_bo_mon, '') AS toBoMon,
                COALESCE(tMain.ho_ten, '') AS giaoVienPhuTrach,
                COALESCE(GROUP_CONCAT(DISTINCT tAssign.ho_ten ORDER BY tAssign.ho_ten SEPARATOR '|'), '') AS giaoVienPhanCongCsv,
                COALESCE(s.nam_hoc_ap_dung, '') AS namHoc,
                COALESCE(s.mo_ta, '') AS moTa
            FROM subjects s
            LEFT JOIN teachers tMain ON LOWER(tMain.id_giao_vien) = LOWER(s.id_giao_vien_phu_trach)
            LEFT JOIN teaching_assignments ta ON ta.id_mon_hoc = s.id_mon_hoc
            LEFT JOIN teachers tAssign ON tAssign.id_giao_vien = ta.id_giao_vien
            WHERE
                (:q IS NULL OR :q = '' OR
                    LOWER(s.id_mon_hoc) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(s.ten_mon_hoc) LIKE CONCAT('%', LOWER(:q), '%'))
                AND (
                    :khoi IS NULL
                    OR (
                        REPLACE(CONCAT(',', COALESCE(s.khoi_ap_dung, ''), ','), ' ', '') COLLATE utf8mb4_unicode_ci
                        LIKE CONCAT('%,', CAST(:khoi AS CHAR), ',%') COLLATE utf8mb4_unicode_ci
                    )
                )
                AND (:toBoMon IS NULL OR :toBoMon = '' OR LOWER(COALESCE(s.to_bo_mon, '')) = LOWER(:toBoMon))
            GROUP BY s.id_mon_hoc, s.ten_mon_hoc, s.khoi_ap_dung, s.hoc_ky_ap_dung, s.to_bo_mon, tMain.ho_ten, s.nam_hoc_ap_dung, s.mo_ta
            ORDER BY s.id_mon_hoc ASC
            """, nativeQuery = true)
    List<Object[]> searchForManagement(@Param("q") String q,
                                       @Param("khoi") Integer khoi,
                                       @Param("toBoMon") String toBoMon);

    @Query(value = """
            SELECT DISTINCT CAST(TRIM(
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
            WHERE s.khoi_ap_dung IS NOT NULL
              AND TRIM(s.khoi_ap_dung) <> ''
            ORDER BY khoi
            """, nativeQuery = true)
    List<Integer> findDistinctGrades();

    @Query(value = """
            SELECT DISTINCT TRIM(s.to_bo_mon)
            FROM subjects s
            WHERE s.to_bo_mon IS NOT NULL
              AND TRIM(s.to_bo_mon) <> ''
            ORDER BY TRIM(s.to_bo_mon)
            """, nativeQuery = true)
    List<String> findDistinctDepartments();

    @Query(value = """
            SELECT COUNT(*)
            FROM teachers t
            WHERE LOWER(t.id_giao_vien) = LOWER(:teacherId)
            """, nativeQuery = true)
    long countTeachersById(@Param("teacherId") String teacherId);

    @Query(value = """
            SELECT t.id_giao_vien, t.ho_ten
            FROM teachers t
            WHERE t.ho_ten IS NOT NULL
            ORDER BY t.ho_ten
            """, nativeQuery = true)
    List<Object[]> findTeacherOptions();

    @Query(value = """
            SELECT sy.nam_hoc
            FROM school_years sy
            ORDER BY sy.ngay_bat_dau DESC
            """, nativeQuery = true)
    List<String> findSchoolYears();

    @Query(value = """
            SELECT c.id_khoa, c.ten_khoa, YEAR(c.ngay_bat_dau) AS namBatDau
            FROM courses c
            WHERE
                (:q IS NULL OR :q = '' OR
                 LOWER(c.id_khoa) LIKE CONCAT('%', LOWER(:q), '%') OR
                 LOWER(c.ten_khoa) LIKE CONCAT('%', LOWER(:q), '%') OR
                 LOWER(CONCAT(c.id_khoa, '-', YEAR(c.ngay_bat_dau))) LIKE CONCAT('%', LOWER(:q), '%'))
            ORDER BY c.id_khoa
            LIMIT 30
            """, nativeQuery = true)
    List<Object[]> findCourseSuggestions(@Param("q") String q);

    @Query(value = """
            SELECT sy.nam_hoc
            FROM school_years sy
            WHERE
                (:q IS NULL OR :q = '' OR
                 LOWER(sy.nam_hoc) LIKE CONCAT('%', LOWER(:q), '%'))
            ORDER BY sy.ngay_bat_dau DESC
            LIMIT 30
            """, nativeQuery = true)
    List<String> findSchoolYearSuggestions(@Param("q") String q);

    @Query(value = """
            SELECT t.id_giao_vien, t.ho_ten
            FROM teachers t
            WHERE t.ho_ten IS NOT NULL
              AND (
                  :q IS NULL OR :q = '' OR
                  LOWER(t.id_giao_vien) LIKE CONCAT('%', LOWER(:q), '%') OR
                  LOWER(t.ho_ten) LIKE CONCAT('%', LOWER(:q), '%')
              )
            ORDER BY t.ho_ten
            LIMIT 30
            """, nativeQuery = true)
    List<Object[]> findTeacherSuggestions(@Param("q") String q);

    @Modifying
    @Query(value = """
            UPDATE teaching_assignments
            SET id_mon_hoc = :newSubjectId
            WHERE id_mon_hoc = :oldSubjectId
            """, nativeQuery = true)
    int reassignSubjectIdInTeachingAssignments(@Param("oldSubjectId") String oldSubjectId,
                                               @Param("newSubjectId") String newSubjectId);

    @Modifying
    @Query(value = """
            UPDATE scores
            SET id_mon_hoc = :newSubjectId
            WHERE id_mon_hoc = :oldSubjectId
            """, nativeQuery = true)
    int reassignSubjectIdInScores(@Param("oldSubjectId") String oldSubjectId,
                                  @Param("newSubjectId") String newSubjectId);

    @Modifying
    @Query(value = """
            UPDATE average_scores
            SET id_mon_hoc = :newSubjectId
            WHERE id_mon_hoc = :oldSubjectId
            """, nativeQuery = true)
    int reassignSubjectIdInAverageScores(@Param("oldSubjectId") String oldSubjectId,
                                         @Param("newSubjectId") String newSubjectId);

    @Modifying
    @Query(value = """
            UPDATE subjects
            SET id_mon_hoc = :newSubjectId
            WHERE id_mon_hoc = :oldSubjectId
            """, nativeQuery = true)
    int renameSubjectId(@Param("oldSubjectId") String oldSubjectId,
                        @Param("newSubjectId") String newSubjectId);
}
