USE quan_ly_diem_thpt;

-- Chuẩn hóa tên loại điểm theo Thông tư 22:
--  - Không còn tách "15 phút", "1 tiết", "điểm miệng"
--  - Nhóm chung thành "Đánh giá thường xuyên"
UPDATE score_types
SET ten_loai = 'Giữa kỳ',
    he_so = 2
WHERE id_loai_diem = 4;

UPDATE score_types
SET ten_loai = 'Cuối kỳ',
    he_so = 3
WHERE id_loai_diem = 5;

UPDATE score_types
SET ten_loai = 'Đánh giá thường xuyên',
    he_so = 1
WHERE id_loai_diem <> 4
  AND id_loai_diem <> 5;
