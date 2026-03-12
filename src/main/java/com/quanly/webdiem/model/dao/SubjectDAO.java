package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectDAO extends JpaRepository<Subject, String> {

    @Query(value = """
            SELECT
                s.id_mon_hoc AS idMonHoc,
                s.ten_mon_hoc AS tenMonHoc,
                COALESCE(GROUP_CONCAT(DISTINCT c.khoi ORDER BY c.khoi SEPARATOR ','), '') AS khoiCsv,
                COUNT(DISTINCT ta.hoc_ky) AS hocKyCount,
                MIN(ta.hoc_ky) AS hocKyMin,
                COALESCE(
                    GROUP_CONCAT(DISTINCT NULLIF(TRIM(t.chuyen_mon), '') ORDER BY t.chuyen_mon SEPARATOR ', '),
                    ''
                ) AS toBoMonCsv,
                COALESCE(GROUP_CONCAT(DISTINCT t.ho_ten ORDER BY t.ho_ten SEPARATOR '|'), '') AS giaoVienCsv,
                COALESCE(MAX(ta.nam_hoc), '') AS namHoc,
                COALESCE(s.mo_ta, '') AS moTa
            FROM subjects s
            LEFT JOIN teaching_assignments ta ON ta.id_mon_hoc = s.id_mon_hoc
            LEFT JOIN classes c ON c.id_lop = ta.id_lop
            LEFT JOIN teachers t ON t.id_giao_vien = ta.id_giao_vien
            WHERE
                (:q IS NULL OR :q = '' OR
                    LOWER(s.id_mon_hoc) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(s.ten_mon_hoc) LIKE CONCAT('%', LOWER(:q), '%'))
                AND (:khoi IS NULL OR c.khoi = :khoi)
                AND (:toBoMon IS NULL OR :toBoMon = '' OR LOWER(COALESCE(t.chuyen_mon, '')) = LOWER(:toBoMon))
            GROUP BY s.id_mon_hoc, s.ten_mon_hoc, s.mo_ta
            ORDER BY s.id_mon_hoc ASC
            """, nativeQuery = true)
    List<Object[]> searchForManagement(@Param("q") String q,
                                       @Param("khoi") Integer khoi,
                                       @Param("toBoMon") String toBoMon);

    @Query(value = """
            SELECT DISTINCT c.khoi
            FROM classes c
            WHERE c.khoi IS NOT NULL
            ORDER BY c.khoi
            """, nativeQuery = true)
    List<Integer> findDistinctGrades();

    @Query(value = """
            SELECT DISTINCT TRIM(t.chuyen_mon)
            FROM teachers t
            WHERE t.chuyen_mon IS NOT NULL AND TRIM(t.chuyen_mon) <> ''
            ORDER BY TRIM(t.chuyen_mon)
            """, nativeQuery = true)
    List<String> findDistinctDepartments();

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
}
