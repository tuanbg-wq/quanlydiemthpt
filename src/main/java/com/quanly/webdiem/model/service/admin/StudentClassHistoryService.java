package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.StudentClassHistoryDAO;
import com.quanly.webdiem.model.entity.StudentClassHistory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudentClassHistoryService {

    public static final String CHUYEN_LOP = "CHUYEN_LOP";
    public static final String CHUYEN_TRUONG = "CHUYEN_TRUONG";

    private final StudentClassHistoryDAO historyDAO;

    public StudentClassHistoryService(StudentClassHistoryDAO historyDAO) {
        this.historyDAO = historyDAO;
    }

    // ===============================
    // Lưu lịch sử chuyển lớp
    // ===============================
    public void saveClassHistory(
            String studentId,
            String lopCu,
            String lopMoi,
            String ghiChu
    ) {
        saveClassHistory(studentId, lopCu, lopMoi, ghiChu, null);
    }

    public void saveClassHistory(
            String studentId,
            String lopCu,
            String lopMoi,
            String ghiChu,
            LocalDate ngayChuyen
    ) {
        if (studentId == null || studentId.isBlank()) {
            throw new RuntimeException("Thiếu mã học sinh.");
        }

        if (lopCu != null && lopMoi != null && lopCu.equals(lopMoi)) {
            return;
        }

        StudentClassHistory history = new StudentClassHistory();
        history.setStudentId(studentId);
        history.setLoaiChuyen(CHUYEN_LOP);
        history.setLopCu(lopCu);
        history.setLopMoi(lopMoi);
        history.setNgayChuyen(ngayChuyen != null ? ngayChuyen : LocalDate.now());
        history.setGhiChu(ghiChu);

        historyDAO.save(history);
    }

    // ===============================
    // Lưu lịch sử chuyển trường
    // ===============================
    public void saveSchoolTransferHistory(
            String studentId,
            String truongCu,
            String truongMoi,
            String ghiChu
    ) {
        saveSchoolTransferHistory(studentId, truongCu, truongMoi, ghiChu, null);
    }

    public void saveSchoolTransferHistory(
            String studentId,
            String truongCu,
            String truongMoi,
            String ghiChu,
            LocalDate ngayChuyen
    ) {
        if (studentId == null || studentId.isBlank()) {
            throw new RuntimeException("Thiếu mã học sinh.");
        }

        StudentClassHistory history = new StudentClassHistory();
        history.setStudentId(studentId);
        history.setLoaiChuyen(CHUYEN_TRUONG);
        history.setTruongCu(truongCu);
        history.setTruongMoi(truongMoi);
        history.setNgayChuyen(ngayChuyen != null ? ngayChuyen : LocalDate.now());
        history.setGhiChu(ghiChu);

        historyDAO.save(history);
    }

    // ===============================
    // Lấy toàn bộ lịch sử của 1 học sinh
    // ===============================
    public List<StudentClassHistory> getHistoryByStudent(String studentId) {
        return historyDAO.findByStudentIdOrderByNgayChuyenDesc(studentId);
    }

    // ===============================
    // Kiểm tra học sinh có lịch sử theo loại không
    // ===============================
    public boolean hasHistoryByType(String studentId, String loaiChuyen) {
        return historyDAO.existsByStudentIdAndLoaiChuyen(studentId, loaiChuyen);
    }

    // ===============================
    // Lấy lịch sử mới nhất theo loại
    // ===============================
    public StudentClassHistory getLatestHistoryByType(String studentId, String loaiChuyen) {
        return historyDAO
                .findFirstByStudentIdAndLoaiChuyenOrderByNgayChuyenDesc(studentId, loaiChuyen)
                .orElse(null);
    }

    public void rebindStudentId(String oldId, String newId) {
        if (oldId == null || newId == null || oldId.equals(newId)) {
            return;
        }

        historyDAO.updateStudentId(oldId, newId);
    }

    // ===============================
    // Lấy lớp trước đó
    // ===============================
    public String getPreviousClass(String studentId) {
        StudentClassHistory history = getLatestHistoryByType(studentId, CHUYEN_LOP);
        return history != null ? history.getLopCu() : null;
    }
}
