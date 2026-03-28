package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.ConductRecord;
import com.quanly.webdiem.model.entity.ConductRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConductDAO extends JpaRepository<ConductRecord, ConductRecordId> {

    @Query(value = """
            SELECT
                cd.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), cd.id_hoc_sinh) AS tenHocSinh,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                COALESCE(CAST(c.khoi AS CHAR), '') AS khoi,
                COALESCE(NULLIF(TRIM(c.id_khoa), ''), '') AS idKhoa,
                CASE
                    WHEN c.id_khoa IS NULL OR TRIM(c.id_khoa) = '' THEN '-'
                    ELSE CONCAT(
                        c.id_khoa,
                        ' (',
                        COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa),
                        ')'
                    )
                END AS khoaHoc,
                COALESCE(NULLIF(TRIM(cd.xep_loai), ''), '') AS xepLoai,
                COALESCE(NULLIF(TRIM(cd.nhan_xet), ''), '') AS nhanXet,
                COALESCE(DATE_FORMAT(cd.ngay_cap_nhat, '%d/%m/%Y'), '') AS ngayQuyetDinh,
                COALESCE(cd.nam_hoc, '') AS namHoc,
                cd.hoc_ky AS hocKy
            FROM conducts cd
            LEFT JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(cd.id_hoc_sinh)
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE NOT EXISTS (
                SELECT 1
                FROM conducts newer
                WHERE LOWER(newer.id_hoc_sinh) = LOWER(cd.id_hoc_sinh)
                  AND (
                    COALESCE(newer.ngay_cap_nhat, '1970-01-01 00:00:00') > COALESCE(cd.ngay_cap_nhat, '1970-01-01 00:00:00')
                    OR (
                      COALESCE(newer.ngay_cap_nhat, '1970-01-01 00:00:00') = COALESCE(cd.ngay_cap_nhat, '1970-01-01 00:00:00')
                      AND COALESCE(newer.nam_hoc, '') > COALESCE(cd.nam_hoc, '')
                    )
                    OR (
                      COALESCE(newer.ngay_cap_nhat, '1970-01-01 00:00:00') = COALESCE(cd.ngay_cap_nhat, '1970-01-01 00:00:00')
                      AND COALESCE(newer.nam_hoc, '') = COALESCE(cd.nam_hoc, '')
                      AND COALESCE(newer.hoc_ky, -1) > COALESCE(cd.hoc_ky, -1)
                    )
                  )
            )
              AND (
                :q IS NULL OR :q = '' OR
                LOWER(st.ho_ten) LIKE CONCAT('%', LOWER(:q), '%') OR
                LOWER(cd.id_hoc_sinh) LIKE CONCAT('%', LOWER(:q), '%') OR
                LOWER(COALESCE(cd.nhan_xet, '')) LIKE CONCAT('%', LOWER(:q), '%')
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
                :courseId IS NULL OR :courseId = '' OR
                LOWER(c.id_khoa) = LOWER(:courseId)
              )
            ORDER BY c.khoi ASC, c.ten_lop ASC, st.ho_ten ASC, cd.id_hoc_sinh ASC
            """, nativeQuery = true)
    List<Object[]> searchForManagement(@Param("q") String q,
                                       @Param("khoi") Integer khoi,
                                       @Param("classId") String classId,
                                       @Param("courseId") String courseId);

    @Query(value = """
            SELECT DISTINCT c.khoi
            FROM classes c
            WHERE c.khoi IS NOT NULL
            ORDER BY c.khoi ASC
            """, nativeQuery = true)
    List<Integer> findDistinctGrades();

    @Query(value = """
            SELECT
                c.id_lop AS idLop,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), c.id_lop) AS tenLop,
                c.khoi AS khoi
            FROM classes c
            ORDER BY c.khoi ASC, c.ten_lop ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctClassesForFilter();

    @Query(value = """
            SELECT
                k.id_khoa AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), k.id_khoa) AS tenKhoa
            FROM courses k
            WHERE k.id_khoa IS NOT NULL
              AND TRIM(k.id_khoa) <> ''
            ORDER BY k.id_khoa ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctCoursesForFilter();
}
