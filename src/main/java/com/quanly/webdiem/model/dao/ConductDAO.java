package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.ConductRecord;
import com.quanly.webdiem.model.entity.ConductRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ConductDAO extends JpaRepository<ConductRecord, ConductRecordId> {

    @Query(value = """
            SELECT
                e.id AS id,
                e.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), e.id_hoc_sinh) AS tenHocSinh,
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
                COALESCE(NULLIF(TRIM(e.loai), ''), '') AS loai,
                COALESCE(NULLIF(TRIM(e.loai_chi_tiet), ''), '') AS loaiChiTiet,
                COALESCE(NULLIF(TRIM(e.so_quyet_dinh), ''), '') AS soQuyetDinh,
                COALESCE(NULLIF(TRIM(e.noi_dung), ''), '') AS noiDung,
                COALESCE(NULLIF(TRIM(e.ghi_chu), ''), '') AS ghiChu,
                COALESCE(DATE_FORMAT(e.ngay_ban_hanh, '%d/%m/%Y'), '') AS ngayBanHanh,
                COALESCE(e.nam_hoc, '') AS namHoc,
                e.hoc_ky AS hocKy
            FROM conduct_events e
            LEFT JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(e.id_hoc_sinh)
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE (
                    :q IS NULL OR :q = '' OR
                    LOWER(st.ho_ten) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(e.id_hoc_sinh) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(e.noi_dung, '')) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(e.so_quyet_dinh, '')) LIKE CONCAT('%', LOWER(:q), '%')
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
              AND (
                    :loai IS NULL OR :loai = '' OR
                    UPPER(e.loai) = UPPER(:loai)
                )
            ORDER BY
                COALESCE(e.ngay_ban_hanh, DATE(e.ngay_cap_nhat)) DESC,
                e.id DESC
            """, nativeQuery = true)
    List<Object[]> searchEventsForManagement(@Param("q") String q,
                                             @Param("khoi") Integer khoi,
                                             @Param("classId") String classId,
                                             @Param("courseId") String courseId,
                                             @Param("loai") String loai);

    @Query(value = """
            SELECT
                e.id AS id,
                e.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), e.id_hoc_sinh) AS tenHocSinh,
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
                COALESCE(NULLIF(TRIM(e.loai), ''), '') AS loai,
                COALESCE(NULLIF(TRIM(e.loai_chi_tiet), ''), '') AS loaiChiTiet,
                COALESCE(NULLIF(TRIM(e.so_quyet_dinh), ''), '') AS soQuyetDinh,
                COALESCE(NULLIF(TRIM(e.noi_dung), ''), '') AS noiDung,
                COALESCE(NULLIF(TRIM(e.ghi_chu), ''), '') AS ghiChu,
                COALESCE(DATE_FORMAT(e.ngay_ban_hanh, '%d/%m/%Y'), '') AS ngayBanHanh,
                COALESCE(e.nam_hoc, '') AS namHoc,
                e.hoc_ky AS hocKy
            FROM conduct_events e
            LEFT JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(e.id_hoc_sinh)
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE e.id = :eventId
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findEventDetail(@Param("eventId") Long eventId);

    @Query(value = """
            SELECT
                st.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), st.id_hoc_sinh) AS hoTen,
                COALESCE(NULLIF(TRIM(c.id_lop), ''), '') AS classId,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                COALESCE(CAST(c.khoi AS CHAR), '') AS khoi,
                COALESCE(NULLIF(TRIM(c.id_khoa), ''), '') AS courseId,
                CASE
                    WHEN c.id_khoa IS NULL OR TRIM(c.id_khoa) = '' THEN '-'
                    ELSE CONCAT(
                        c.id_khoa,
                        ' (',
                        COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa),
                        ')'
                    )
                END AS khoaHoc
            FROM students st
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE (:khoi IS NULL OR c.khoi = :khoi)
              AND (:classId IS NULL OR :classId = '' OR LOWER(c.id_lop) = LOWER(:classId))
              AND (:courseId IS NULL OR :courseId = '' OR LOWER(c.id_khoa) = LOWER(:courseId))
              AND (
                    :q IS NULL OR :q = '' OR
                    LOWER(st.id_hoc_sinh) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(st.ho_ten) LIKE CONCAT('%', LOWER(:q), '%')
                  )
            ORDER BY c.khoi ASC, c.ten_lop ASC, st.ho_ten ASC, st.id_hoc_sinh ASC
            LIMIT 40
            """, nativeQuery = true)
    List<Object[]> findStudentsForRewardForm(@Param("khoi") Integer khoi,
                                             @Param("classId") String classId,
                                             @Param("courseId") String courseId,
                                             @Param("q") String q);

    @Query(value = """
            SELECT
                st.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), st.id_hoc_sinh) AS hoTen,
                COALESCE(NULLIF(TRIM(c.id_lop), ''), '') AS classId,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                COALESCE(CAST(c.khoi AS CHAR), '') AS khoi,
                COALESCE(NULLIF(TRIM(c.id_khoa), ''), '') AS courseId,
                CASE
                    WHEN c.id_khoa IS NULL OR TRIM(c.id_khoa) = '' THEN '-'
                    ELSE CONCAT(
                        c.id_khoa,
                        ' (',
                        COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa),
                        ')'
                    )
                END AS khoaHoc
            FROM students st
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE LOWER(st.id_hoc_sinh) = LOWER(:studentId)
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findStudentSnapshot(@Param("studentId") String studentId);

    @Query(value = """
            SELECT
                st.ngay_nhap_hoc AS ngayNhapHoc,
                k.ngay_ket_thuc AS ngayKetThucKhoa,
                COALESCE(NULLIF(TRIM(c.id_khoa), ''), '') AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), '') AS tenKhoa
            FROM students st
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE LOWER(st.id_hoc_sinh) = LOWER(:studentId)
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findStudentDateConstraints(@Param("studentId") String studentId);

    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO conduct_events
                (id_hoc_sinh, loai, loai_chi_tiet, so_quyet_dinh, noi_dung, ngay_ban_hanh, ghi_chu, nam_hoc, hoc_ky)
            VALUES
                (:studentId, :loai, :loaiChiTiet, :soQuyetDinh, :noiDung, :ngayBanHanh, :ghiChu, :namHoc, :hocKy)
            """, nativeQuery = true)
    int insertEvent(@Param("studentId") String studentId,
                    @Param("loai") String loai,
                    @Param("loaiChiTiet") String loaiChiTiet,
                    @Param("soQuyetDinh") String soQuyetDinh,
                    @Param("noiDung") String noiDung,
                    @Param("ngayBanHanh") String ngayBanHanh,
                    @Param("ghiChu") String ghiChu,
                    @Param("namHoc") String namHoc,
                    @Param("hocKy") Integer hocKy);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE conduct_events
            SET
                loai = :loai,
                loai_chi_tiet = :loaiChiTiet,
                so_quyet_dinh = :soQuyetDinh,
                noi_dung = :noiDung,
                ngay_ban_hanh = :ngayBanHanh,
                ghi_chu = :ghiChu,
                nam_hoc = :namHoc,
                hoc_ky = :hocKy
            WHERE id = :eventId
            """, nativeQuery = true)
    int updateEvent(@Param("eventId") Long eventId,
                    @Param("loai") String loai,
                    @Param("loaiChiTiet") String loaiChiTiet,
                    @Param("soQuyetDinh") String soQuyetDinh,
                    @Param("noiDung") String noiDung,
                    @Param("ngayBanHanh") String ngayBanHanh,
                    @Param("ghiChu") String ghiChu,
                    @Param("namHoc") String namHoc,
                    @Param("hocKy") Integer hocKy);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM conduct_events WHERE id = :eventId", nativeQuery = true)
    int deleteEvent(@Param("eventId") Long eventId);

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
