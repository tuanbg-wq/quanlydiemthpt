package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.ReportExportHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportExportHistoryDAO extends JpaRepository<ReportExportHistory, Long> {

    List<ReportExportHistory> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    List<ReportExportHistory> findAllByCreatedByIgnoreCaseAndCreatedRoleCodeIgnoreCaseAndReportTypeCodeIgnoreCaseOrderByCreatedAtDescIdDesc(
            String createdBy,
            String createdRoleCode,
            String reportTypeCode,
            Pageable pageable
    );
}
