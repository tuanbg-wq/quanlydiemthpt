USE quan_ly_diem_thpt;

CREATE TABLE IF NOT EXISTS report_export_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    report_type VARCHAR(120) NOT NULL,
    report_type_code VARCHAR(50) NOT NULL,
    export_format VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    created_role_code VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_rows BIGINT NOT NULL DEFAULT 0,
    filter_summary VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_report_export_history_created_at (created_at),
    KEY idx_report_export_history_type_code (report_type_code),
    KEY idx_report_export_history_format (export_format),
    KEY idx_report_export_history_role_code (created_role_code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
