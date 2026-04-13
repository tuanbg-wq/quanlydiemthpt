package com.quanly.webdiem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_export_history")
public class ReportExportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "report_type", nullable = false, length = 120)
    private String reportType;

    @Column(name = "report_type_code", nullable = false, length = 50)
    private String reportTypeCode;

    @Column(name = "export_format", nullable = false, length = 10)
    private String exportFormat;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(name = "created_role_code", nullable = false, length = 20)
    private String createdRoleCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_rows", nullable = false)
    private Long totalRows;

    @Column(name = "filter_summary", nullable = false, length = 255)
    private String filterSummary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportTypeCode() {
        return reportTypeCode;
    }

    public void setReportTypeCode(String reportTypeCode) {
        this.reportTypeCode = reportTypeCode;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedRoleCode() {
        return createdRoleCode;
    }

    public void setCreatedRoleCode(String createdRoleCode) {
        this.createdRoleCode = createdRoleCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Long totalRows) {
        this.totalRows = totalRows;
    }

    public String getFilterSummary() {
        return filterSummary;
    }

    public void setFilterSummary(String filterSummary) {
        this.filterSummary = filterSummary;
    }
}
