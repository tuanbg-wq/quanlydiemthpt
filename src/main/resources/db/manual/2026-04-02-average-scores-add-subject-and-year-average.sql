USE quan_ly_diem_thpt;

ALTER TABLE average_scores
    ADD COLUMN IF NOT EXISTS ten_mon_hoc VARCHAR(255) NULL AFTER id_mon_hoc,
    ADD COLUMN IF NOT EXISTS dtb_ca_nam_mon DECIMAL(4,2) NULL AFTER dtb_hoc_ky,
    ADD COLUMN IF NOT EXISTS diem_giua_ky DECIMAL(4,2) NULL AFTER dtb_ca_nam_mon,
    ADD COLUMN IF NOT EXISTS diem_cuoi_ky DECIMAL(4,2) NULL AFTER diem_giua_ky,
    DROP COLUMN IF EXISTS dtb_nam_hoc,
    DROP COLUMN IF EXISTS dtb_mon;

DROP PROCEDURE IF EXISTS sp_refresh_average_score_group;
DELIMITER //
CREATE PROCEDURE sp_refresh_average_score_group(
    IN p_student_id VARCHAR(10),
    IN p_subject_id VARCHAR(10),
    IN p_school_year VARCHAR(20),
    IN p_semester INT
)
BEGIN
    DECLARE v_dtb_hk DECIMAL(4,2);
    DECLARE v_midterm DECIMAL(4,2);
    DECLARE v_final DECIMAL(4,2);
    DECLARE v_hk1 DECIMAL(4,2);
    DECLARE v_hk2 DECIMAL(4,2);
    DECLARE v_dtb_year DECIMAL(4,2);
    DECLARE v_subject_name VARCHAR(255);

    SELECT COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), p_subject_id)
    INTO v_subject_name
    FROM subjects sb
    WHERE LOWER(sb.id_mon_hoc) = LOWER(p_subject_id)
    LIMIT 1;

    IF v_subject_name IS NULL THEN
        SET v_subject_name = p_subject_id;
    END IF;

    IF p_semester IN (1, 2) THEN
        SELECT
            ROUND(AVG(CASE WHEN sc.id_loai_diem = 4 THEN sc.diem END), 2),
            ROUND(AVG(CASE WHEN sc.id_loai_diem = 5 THEN sc.diem END), 2),
            ROUND(
                SUM(
                    sc.diem * CASE
                        WHEN sc.id_loai_diem = 4 THEN 2
                        WHEN sc.id_loai_diem = 5 THEN 3
                        ELSE 1
                    END
                )
                / NULLIF(
                    SUM(
                        CASE
                            WHEN sc.id_loai_diem = 4 THEN 2
                            WHEN sc.id_loai_diem = 5 THEN 3
                            ELSE 1
                        END
                    ),
                    0
                ),
                2
            )
        INTO v_midterm, v_final, v_dtb_hk
        FROM scores sc
        WHERE LOWER(sc.id_hoc_sinh) = LOWER(p_student_id)
          AND LOWER(sc.id_mon_hoc) = LOWER(p_subject_id)
          AND sc.nam_hoc = p_school_year
          AND sc.hoc_ky = p_semester;

        IF v_dtb_hk IS NULL THEN
            DELETE FROM average_scores
            WHERE LOWER(id_hoc_sinh) = LOWER(p_student_id)
              AND LOWER(id_mon_hoc) = LOWER(p_subject_id)
              AND nam_hoc = p_school_year
              AND hoc_ky = p_semester;
        ELSE
            INSERT INTO average_scores (
                id_hoc_sinh,
                id_mon_hoc,
                ten_mon_hoc,
                nam_hoc,
                hoc_ky,
                dtb_hoc_ky,
                dtb_ca_nam_mon,
                diem_giua_ky,
                diem_cuoi_ky,
                xep_loai,
                ngay_cap_nhat
            )
            VALUES (
                p_student_id,
                p_subject_id,
                v_subject_name,
                p_school_year,
                p_semester,
                v_dtb_hk,
                NULL,
                v_midterm,
                v_final,
                CASE
                    WHEN v_dtb_hk >= 8.0 THEN 'Gioi'
                    WHEN v_dtb_hk >= 6.5 THEN 'Kha'
                    WHEN v_dtb_hk >= 5.0 THEN 'Trung_binh'
                    WHEN v_dtb_hk >= 3.5 THEN 'Yeu'
                    ELSE 'Kem'
                END,
                CURRENT_TIMESTAMP
            )
            ON DUPLICATE KEY UPDATE
                ten_mon_hoc = VALUES(ten_mon_hoc),
                dtb_hoc_ky = VALUES(dtb_hoc_ky),
                diem_giua_ky = VALUES(diem_giua_ky),
                diem_cuoi_ky = VALUES(diem_cuoi_ky),
                xep_loai = VALUES(xep_loai),
                ngay_cap_nhat = CURRENT_TIMESTAMP;
        END IF;
    END IF;

    SELECT dtb_hoc_ky
    INTO v_hk1
    FROM average_scores
    WHERE LOWER(id_hoc_sinh) = LOWER(p_student_id)
      AND LOWER(id_mon_hoc) = LOWER(p_subject_id)
      AND nam_hoc = p_school_year
      AND hoc_ky = 1
    LIMIT 1;

    SELECT dtb_hoc_ky
    INTO v_hk2
    FROM average_scores
    WHERE LOWER(id_hoc_sinh) = LOWER(p_student_id)
      AND LOWER(id_mon_hoc) = LOWER(p_subject_id)
      AND nam_hoc = p_school_year
      AND hoc_ky = 2
    LIMIT 1;

    IF v_hk1 IS NOT NULL AND v_hk2 IS NOT NULL THEN
        SET v_dtb_year = ROUND((v_hk1 + (2 * v_hk2)) / 3, 2);

        UPDATE average_scores
        SET ten_mon_hoc = v_subject_name,
            dtb_ca_nam_mon = v_dtb_year,
            ngay_cap_nhat = CURRENT_TIMESTAMP
        WHERE LOWER(id_hoc_sinh) = LOWER(p_student_id)
          AND LOWER(id_mon_hoc) = LOWER(p_subject_id)
          AND nam_hoc = p_school_year
          AND hoc_ky IN (1, 2);
    ELSE
        UPDATE average_scores
        SET ten_mon_hoc = v_subject_name,
            dtb_ca_nam_mon = NULL,
            ngay_cap_nhat = CURRENT_TIMESTAMP
        WHERE LOWER(id_hoc_sinh) = LOWER(p_student_id)
          AND LOWER(id_mon_hoc) = LOWER(p_subject_id)
          AND nam_hoc = p_school_year
          AND hoc_ky IN (1, 2);

    END IF;
END//
DELIMITER ;

DROP TRIGGER IF EXISTS after_score_insert;
DELIMITER //
CREATE TRIGGER after_score_insert
AFTER INSERT ON scores
FOR EACH ROW
BEGIN
    CALL sp_refresh_average_score_group(NEW.id_hoc_sinh, NEW.id_mon_hoc, NEW.nam_hoc, NEW.hoc_ky);
END//
DELIMITER ;

DROP TRIGGER IF EXISTS after_score_update_dtb;
DELIMITER //
CREATE TRIGGER after_score_update_dtb
AFTER UPDATE ON scores
FOR EACH ROW
BEGIN
    IF LOWER(OLD.id_hoc_sinh) <> LOWER(NEW.id_hoc_sinh)
       OR LOWER(OLD.id_mon_hoc) <> LOWER(NEW.id_mon_hoc)
       OR OLD.nam_hoc <> NEW.nam_hoc
       OR OLD.hoc_ky <> NEW.hoc_ky THEN
        CALL sp_refresh_average_score_group(OLD.id_hoc_sinh, OLD.id_mon_hoc, OLD.nam_hoc, OLD.hoc_ky);
    END IF;

    CALL sp_refresh_average_score_group(NEW.id_hoc_sinh, NEW.id_mon_hoc, NEW.nam_hoc, NEW.hoc_ky);
END//
DELIMITER ;

DROP TRIGGER IF EXISTS after_score_delete_dtb;
DELIMITER //
CREATE TRIGGER after_score_delete_dtb
AFTER DELETE ON scores
FOR EACH ROW
BEGIN
    CALL sp_refresh_average_score_group(OLD.id_hoc_sinh, OLD.id_mon_hoc, OLD.nam_hoc, OLD.hoc_ky);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_backfill_average_scores;
DELIMITER //
CREATE PROCEDURE sp_backfill_average_scores()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_student_id VARCHAR(10);
    DECLARE v_subject_id VARCHAR(10);
    DECLARE v_school_year VARCHAR(20);

    DECLARE cur CURSOR FOR
        SELECT DISTINCT sc.id_hoc_sinh, sc.id_mon_hoc, sc.nam_hoc
        FROM scores sc;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_student_id, v_subject_id, v_school_year;
        IF done = 1 THEN
            LEAVE read_loop;
        END IF;

        CALL sp_refresh_average_score_group(v_student_id, v_subject_id, v_school_year, 1);
        CALL sp_refresh_average_score_group(v_student_id, v_subject_id, v_school_year, 2);
    END LOOP;

    CLOSE cur;

    DELETE av
    FROM average_scores av
    WHERE NOT EXISTS (
              SELECT 1
              FROM scores sc
              WHERE LOWER(sc.id_hoc_sinh) = LOWER(av.id_hoc_sinh)
                AND LOWER(sc.id_mon_hoc) = LOWER(av.id_mon_hoc)
                AND sc.nam_hoc = av.nam_hoc
          );
END//
DELIMITER ;

CALL sp_backfill_average_scores();
DROP PROCEDURE IF EXISTS sp_backfill_average_scores;

UPDATE average_scores av
LEFT JOIN subjects sb ON LOWER(sb.id_mon_hoc) = LOWER(av.id_mon_hoc)
SET av.ten_mon_hoc = COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), av.id_mon_hoc)
WHERE av.ten_mon_hoc IS NULL OR TRIM(av.ten_mon_hoc) = '';
