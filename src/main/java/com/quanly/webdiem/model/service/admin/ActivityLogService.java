package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ActivityLogDAO;
import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.ActivityLog;
import com.quanly.webdiem.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityLogService {

    private static final String STUDENTS_TABLE = "students";

    private final ActivityLogDAO activityLogDAO;
    private final UserDAO userDAO;

    public ActivityLogService(ActivityLogDAO activityLogDAO, UserDAO userDAO) {
        this.activityLogDAO = activityLogDAO;
        this.userDAO = userDAO;
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> getStudentEditLogs(String studentId) {
        return activityLogDAO.findByBangTacDongAndIdBanGhiOrderByThoiGianDesc(STUDENTS_TABLE, studentId);
    }

    @Transactional
    public void logStudentUpdate(String studentId, String username, String summary, String ipAddress) {
        if (studentId == null || studentId.isBlank() || username == null || username.isBlank()) {
            return;
        }

        User actor = userDAO.findByTenDangNhap(username).orElse(null);
        if (actor == null || actor.getIdTaiKhoan() == null) {
            return;
        }

        ActivityLog log = new ActivityLog();
        log.setIdTaiKhoan(actor.getIdTaiKhoan());
        log.setHanhDong("CAP_NHAT_HOC_SINH");
        log.setBangTacDong(STUDENTS_TABLE);
        log.setIdBanGhi(studentId);
        log.setNoiDung(summary == null || summary.isBlank() ? "Cập nhật hồ sơ học sinh." : summary);
        log.setDiaChiIp(ipAddress);

        activityLogDAO.save(log);
    }

    @Transactional
    public void rebindStudentRecordId(String oldStudentId, String newStudentId) {
        if (oldStudentId == null || newStudentId == null || oldStudentId.equals(newStudentId)) {
            return;
        }

        activityLogDAO.rebindRecordId(STUDENTS_TABLE, oldStudentId, newStudentId);
    }
}
