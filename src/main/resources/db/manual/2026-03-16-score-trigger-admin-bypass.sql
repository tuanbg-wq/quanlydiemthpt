-- Add admin bypass support for score permission trigger
-- Usage:
-- 1) Run this script on DB.
-- 2) App will set session variable @app_is_admin=1 when current user is admin.

USE quan_ly_diem_thpt;

DROP TRIGGER IF EXISTS before_score_insert_check_permission;

DELIMITER //

CREATE TRIGGER before_score_insert_check_permission
BEFORE INSERT ON scores
FOR EACH ROW
BEGIN
    DECLARE assigned INT DEFAULT 0;

    -- Admin session bypass
    IF COALESCE(@app_is_admin, 0) = 1 THEN
        SET assigned = 1;
    ELSE
        SELECT COUNT(*) INTO assigned
        FROM teaching_assignments
        WHERE id_giao_vien = NEW.id_giao_vien
          AND id_mon_hoc = NEW.id_mon_hoc
          AND nam_hoc = NEW.nam_hoc
          AND hoc_ky = NEW.hoc_ky
          AND id_lop = (
                SELECT id_lop
                FROM students
                WHERE id_hoc_sinh = NEW.id_hoc_sinh
          );
    END IF;

    IF assigned = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giao vien khong duoc phan cong day mon nay cho lop cua hoc sinh';
    END IF;
END//

DELIMITER ;
