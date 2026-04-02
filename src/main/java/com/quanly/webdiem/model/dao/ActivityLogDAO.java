package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityLogDAO extends JpaRepository<ActivityLog, Integer> {

    List<ActivityLog> findByBangTacDongAndIdBanGhiOrderByThoiGianDesc(String bangTacDong, String idBanGhi);
    List<ActivityLog> findByBangTacDongAndIdBanGhiInOrderByThoiGianDesc(String bangTacDong, List<String> idBanGhiList);
    List<ActivityLog> findByBangTacDongAndIdBanGhiInAndIdTaiKhoanOrderByThoiGianDesc(String bangTacDong,
                                                                                      List<String> idBanGhiList,
                                                                                      Integer idTaiKhoan);
    List<ActivityLog> findByBangTacDongAndHanhDongOrderByThoiGianDesc(String bangTacDong, String hanhDong);

    @Modifying
    @Query(
            value = """
            UPDATE activity_logs
            SET id_ban_ghi = :newId
            WHERE bang_tac_dong = :tableName
              AND id_ban_ghi = :oldId
            """,
            nativeQuery = true
    )
    int rebindRecordId(@Param("tableName") String tableName,
                       @Param("oldId") String oldId,
                       @Param("newId") String newId);

    @Query(
            value = """
            SELECT
                COALESCE(NULLIF(TRIM(t.ho_ten), ''), NULLIF(TRIM(u.ten_dang_nhap), ''), 'Hệ thống') AS actorName,
                CASE
                    WHEN LOWER(COALESCE(r.ten_vai_tro, '')) = 'admin' THEN 'Admin'
                    WHEN t.id_giao_vien IS NOT NULL
                         AND EXISTS (
                             SELECT 1
                             FROM classes cx
                             WHERE LOWER(COALESCE(cx.id_gvcn, '')) = LOWER(t.id_giao_vien)
                               AND TRIM(COALESCE(cx.id_gvcn, '')) <> ''
                         ) THEN 'GVCN'
                    WHEN LOWER(COALESCE(r.ten_vai_tro, '')) = 'gvcn' THEN 'GVCN'
                    WHEN LOWER(COALESCE(r.ten_vai_tro, '')) IN ('gvbm', 'giao_vien') THEN 'Giáo viên'
                    ELSE COALESCE(NULLIF(TRIM(r.ten_vai_tro), ''), 'Tài khoản')
                END AS actorRole,
                COALESCE(l.hanh_dong, '') AS actionCode,
                COALESCE(l.noi_dung, '') AS actionDetail,
                l.thoi_gian AS actionTime
            FROM activity_logs l
            LEFT JOIN users u ON u.id_tai_khoan = l.id_tai_khoan
            LEFT JOIN roles r ON r.id_vai_tro = u.id_vai_tro
            LEFT JOIN teachers t ON t.id_tai_khoan = u.id_tai_khoan
            WHERE LOWER(COALESCE(l.bang_tac_dong, '')) = LOWER(:tableName)
              AND (
                    :q IS NULL OR :q = '' OR
                    LOWER(COALESCE(l.noi_dung, '')) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(u.ten_dang_nhap, '')) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(t.ho_ten, '')) LIKE CONCAT('%', LOWER(:q), '%')
                  )
              AND (
                    :loai IS NULL OR :loai = '' OR
                    (:loai = 'KHEN_THUONG' AND UPPER(COALESCE(l.hanh_dong, '')) LIKE '%KHEN_THUONG%') OR
                    (:loai = 'KY_LUAT' AND UPPER(COALESCE(l.hanh_dong, '')) LIKE '%KY_LUAT%')
                  )
            ORDER BY l.thoi_gian DESC, l.id_nhat_ky DESC
            LIMIT :limit
            """,
            nativeQuery = true
    )
    List<Object[]> findRecentActivitiesByTable(@Param("tableName") String tableName,
                                               @Param("q") String q,
                                               @Param("loai") String loai,
                                               @Param("limit") int limit);
}
