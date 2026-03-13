-- Chay 1 lan tren MySQL 8+ de dong bo schema mon hoc voi code hien tai.
ALTER TABLE subjects
    DROP COLUMN IF EXISTS so_tiet,
    DROP COLUMN IF EXISTS he_so;
