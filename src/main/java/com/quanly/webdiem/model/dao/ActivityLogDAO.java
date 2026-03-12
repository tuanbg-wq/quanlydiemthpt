package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityLogDAO extends JpaRepository<ActivityLog, Integer> {

    List<ActivityLog> findByBangTacDongAndIdBanGhiOrderByThoiGianDesc(String bangTacDong, String idBanGhi);

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
}
