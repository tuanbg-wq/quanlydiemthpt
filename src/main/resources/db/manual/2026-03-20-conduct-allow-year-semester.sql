-- Allow annual conduct records (hoc_ky = 0) for save mode "Ca nam".
-- Run once on MariaDB/MySQL-compatible servers.

USE quan_ly_diem_thpt;

ALTER TABLE conducts
    DROP CONSTRAINT CONSTRAINT_1,
    ADD CONSTRAINT chk_conducts_hoc_ky CHECK (hoc_ky IN (0, 1, 2));
