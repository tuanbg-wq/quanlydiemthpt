package com.quanly.webdiem.model.service.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseCompatibilityService implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCompatibilityService.class);

    private static final String AVERAGE_SCORES_TABLE = "average_scores";
    private static final String CONDUCTS_TABLE = "conducts";
    private static final String REPORT_EXPORT_HISTORY_TABLE = "report_export_history";
    private static final String NGAY_CAP_NHAT_COLUMN = "ngay_cap_nhat";
    private static final String CREATED_ROLE_CODE_COLUMN = "created_role_code";
    private static final String REFRESH_AVERAGE_SCORE_PROCEDURE = "sp_refresh_average_score_group";

    private static final String CREATE_AVERAGE_SCORES_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS average_scores (
                id_trung_binh INT NOT NULL AUTO_INCREMENT,
                id_hoc_sinh VARCHAR(10) NOT NULL,
                id_mon_hoc VARCHAR(10) NOT NULL,
                nam_hoc VARCHAR(20) NOT NULL,
                hoc_ky INT NOT NULL,
                dtb_hoc_ky DECIMAL(4,2) NULL,
                xep_loai VARCHAR(20) NULL,
                ngay_cap_nhat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (id_trung_binh),
                UNIQUE KEY uk_average_scores_group (id_hoc_sinh, id_mon_hoc, nam_hoc, hoc_ky)
            )
            """;

    private static final String CREATE_REFRESH_AVERAGE_SCORE_PROCEDURE_SQL = """
            CREATE PROCEDURE sp_refresh_average_score_group(
                IN p_student_id VARCHAR(10),
                IN p_subject_id VARCHAR(10),
                IN p_school_year VARCHAR(20),
                IN p_semester INT
            )
            BEGIN
                DO 0;
            END
            """;

    private static final String CREATE_REPORT_EXPORT_HISTORY_TABLE_SQL = """
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
            )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final Object initLock = new Object();
    private volatile boolean initialized;

    public DatabaseCompatibilityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureCompatibility();
    }

    private void ensureCompatibility() {
        if (initialized) {
            return;
        }
        synchronized (initLock) {
            if (initialized) {
                return;
            }
            ensureAverageScoresTableExists();
            ensureConductTimestampColumnCompatible();
            ensureRefreshAverageScoreProcedureExists();
            ensureReportExportHistoryCompatible();
            initialized = true;
        }
    }

    private void ensureAverageScoresTableExists() {
        try {
            jdbcTemplate.execute(CREATE_AVERAGE_SCORES_TABLE_SQL);
        } catch (Exception ex) {
            LOGGER.warn("Khong the dam bao bang {} ton tai.", AVERAGE_SCORES_TABLE, ex);
        }
    }

    private void ensureConductTimestampColumnCompatible() {
        if (!columnExists(CONDUCTS_TABLE, NGAY_CAP_NHAT_COLUMN)) {
            return;
        }

        try {
            jdbcTemplate.update("""
                    UPDATE conducts
                    SET ngay_cap_nhat = CURRENT_TIMESTAMP
                    WHERE ngay_cap_nhat IS NULL
                    """);
        } catch (Exception ex) {
            LOGGER.warn("Khong the cap nhat du lieu null cho cot {}.{}.", CONDUCTS_TABLE, NGAY_CAP_NHAT_COLUMN, ex);
        }

        try {
            jdbcTemplate.execute("""
                    ALTER TABLE conducts
                    MODIFY COLUMN ngay_cap_nhat TIMESTAMP NOT NULL
                    DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP
                    """);
        } catch (Exception ex) {
            LOGGER.warn("Khong the chuan hoa cot {}.{} ve TIMESTAMP tu cap nhat.", CONDUCTS_TABLE, NGAY_CAP_NHAT_COLUMN, ex);
        }
    }

    private void ensureRefreshAverageScoreProcedureExists() {
        if (routineExists(REFRESH_AVERAGE_SCORE_PROCEDURE)) {
            return;
        }

        try {
            jdbcTemplate.execute("DROP PROCEDURE IF EXISTS " + REFRESH_AVERAGE_SCORE_PROCEDURE);
            jdbcTemplate.execute(CREATE_REFRESH_AVERAGE_SCORE_PROCEDURE_SQL);
            LOGGER.warn("Da tao thu tuc du phong {} de tranh loi trigger tren CSDL moi.", REFRESH_AVERAGE_SCORE_PROCEDURE);
        } catch (Exception ex) {
            LOGGER.warn("Khong the tao thu tuc du phong {}.", REFRESH_AVERAGE_SCORE_PROCEDURE, ex);
        }
    }

    private void ensureReportExportHistoryCompatible() {
        try {
            jdbcTemplate.execute(CREATE_REPORT_EXPORT_HISTORY_TABLE_SQL);
        } catch (Exception ex) {
            LOGGER.warn("Khong the dam bao bang {} ton tai.", REPORT_EXPORT_HISTORY_TABLE, ex);
            return;
        }

        if (!columnExists(REPORT_EXPORT_HISTORY_TABLE, CREATED_ROLE_CODE_COLUMN)) {
            try {
                jdbcTemplate.execute("""
                        ALTER TABLE report_export_history
                        ADD COLUMN created_role_code VARCHAR(20) NOT NULL DEFAULT 'ADMIN'
                        AFTER created_by
                        """);
            } catch (Exception ex) {
                LOGGER.warn("Khong the bo sung cot {}.{}.", REPORT_EXPORT_HISTORY_TABLE, CREATED_ROLE_CODE_COLUMN, ex);
            }
        }

        try {
            jdbcTemplate.execute("""
                    CREATE INDEX idx_report_export_history_role_code
                    ON report_export_history (created_role_code)
                    """);
        } catch (Exception ex) {
            LOGGER.debug("Index idx_report_export_history_role_code da ton tai hoac khong the tao.", ex);
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }

    private boolean routineExists(String routineName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.routines
                WHERE routine_schema = DATABASE()
                  AND routine_name = ?
                  AND routine_type = 'PROCEDURE'
                """, Integer.class, routineName);
        return count != null && count > 0;
    }
}
