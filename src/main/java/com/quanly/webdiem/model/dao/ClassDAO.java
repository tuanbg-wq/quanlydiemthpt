package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassDAO extends JpaRepository<ClassEntity, String> {

    @Query(value = """
            SELECT
                c.id_lop AS idLop,
                c.ten_lop AS tenLop,
                c.khoi AS khoi,
                CONCAT(
                    c.id_khoa,
                    '( ',
                    COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa),
                    ')'
                ) AS khoaHoc,
                COALESCE(NULLIF(TRIM(t.ho_ten), ''), '-') AS gvcnTen,
                COALESCE(NULLIF(TRIM(t.email), ''), '-') AS gvcnEmail,
                COALESCE(NULLIF(TRIM(t.anh), ''), '') AS gvcnAvatar,
                COALESCE(c.si_so, 0) AS siSo,
                COALESCE(NULLIF(TRIM(c.nam_hoc), ''), '-') AS namHoc
            FROM classes c
            LEFT JOIN courses k ON k.id_khoa = c.id_khoa
            LEFT JOIN teachers t ON LOWER(t.id_giao_vien) = LOWER(c.id_gvcn)
            WHERE (
                    :q IS NULL OR :q = '' OR
                    LOWER(c.ten_lop) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(c.id_lop) LIKE CONCAT('%', LOWER(:q), '%')
                )
              AND (
                    :khoi IS NULL OR :khoi = '' OR
                    c.khoi = CAST(:khoi AS UNSIGNED)
                )
              AND (
                    :khoa IS NULL OR :khoa = '' OR
                    LOWER(c.id_khoa) = LOWER(:khoa)
                )
            ORDER BY c.khoi ASC, c.ten_lop ASC, c.id_lop ASC
            """, nativeQuery = true)
    List<Object[]> searchForManagement(@Param("q") String q,
                                       @Param("khoi") String khoi,
                                       @Param("khoa") String khoa);

    @Query(value = """
            SELECT COUNT(*)
            FROM classes
            """, nativeQuery = true)
    long countAllClasses();

    @Query(value = """
            SELECT COALESCE(SUM(COALESCE(c.si_so, 0)), 0)
            FROM classes c
            """, nativeQuery = true)
    long sumAllClassSizes();

    @Query(value = """
            SELECT COUNT(DISTINCT c.id_gvcn)
            FROM classes c
            WHERE c.id_gvcn IS NOT NULL
              AND TRIM(c.id_gvcn) <> ''
            """, nativeQuery = true)
    long countDistinctHomeroomTeachers();

    @Query(value = """
            SELECT COUNT(*)
            FROM classes c
            WHERE LOWER(COALESCE(c.id_gvcn, '')) = LOWER(:teacherId)
              AND TRIM(COALESCE(c.id_gvcn, '')) <> ''
              AND LOWER(c.id_lop) <> LOWER(:classId)
            """, nativeQuery = true)
    long countOtherHomeroomClassesByTeacherId(@Param("teacherId") String teacherId,
                                              @Param("classId") String classId);

    @Query(value = """
            SELECT DISTINCT c.khoi
            FROM classes c
            WHERE c.khoi IS NOT NULL
            ORDER BY c.khoi
            """, nativeQuery = true)
    List<Integer> findDistinctGrades();

    @Query(value = """
            SELECT
                c.id_khoa AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa) AS tenKhoa
            FROM classes c
            LEFT JOIN courses k ON k.id_khoa = c.id_khoa
            WHERE c.id_khoa IS NOT NULL
              AND TRIM(c.id_khoa) <> ''
            GROUP BY c.id_khoa, k.ten_khoa
            ORDER BY c.id_khoa ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctCoursesForFilter();

    @Query(value = """
            SELECT
                c.id_lop AS idLop,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), c.id_lop) AS tenLop,
                c.khoi AS khoi,
                COALESCE(NULLIF(TRIM(c.nam_hoc), ''), '-') AS namHoc,
                COALESCE(c.si_so, 0) AS siSo,
                COALESCE(NULLIF(TRIM(c.ghi_chu), ''), '') AS ghiChu,
                COALESCE(NULLIF(TRIM(c.id_gvcn), ''), '') AS idGvcn,
                COALESCE(NULLIF(TRIM(t.ho_ten), ''), '-') AS gvcnTen,
                COALESCE(NULLIF(TRIM(t.email), ''), '') AS gvcnEmail,
                COALESCE(NULLIF(TRIM(t.so_dien_thoai), ''), '') AS gvcnPhone,
                COALESCE(NULLIF(TRIM(t.anh), ''), '') AS gvcnAvatar,
                COALESCE(NULLIF(TRIM(c.id_khoa), ''), '') AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), COALESCE(NULLIF(TRIM(c.id_khoa), ''), '-')) AS tenKhoa
            FROM classes c
            LEFT JOIN teachers t ON LOWER(t.id_giao_vien) = LOWER(c.id_gvcn)
            LEFT JOIN courses k ON k.id_khoa = c.id_khoa
            WHERE LOWER(c.id_lop) = LOWER(:classId)
            LIMIT 1
            """, nativeQuery = true)
    Optional<Object[]> findClassInfoById(@Param("classId") String classId);
}
