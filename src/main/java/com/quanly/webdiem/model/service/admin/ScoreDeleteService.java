package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ScoreDAO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScoreDeleteService {

    private final ScoreDAO scoreDAO;
    private final ActivityLogService activityLogService;

    public ScoreDeleteService(ScoreDAO scoreDAO, ActivityLogService activityLogService) {
        this.scoreDAO = scoreDAO;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public void deleteScoreGroup(String studentId, String subjectId, String namHoc) {
        String normalizedStudentId = safeTrim(studentId);
        String normalizedSubjectId = safeTrim(subjectId);
        String normalizedNamHoc = safeTrim(namHoc);
        List<Object[]> scoreSummary = scoreDAO.findScoreGroupSummary(
                normalizedStudentId,
                normalizedSubjectId,
                normalizedNamHoc
        );

        long deletedRows = scoreDAO.deleteByIdHocSinhIgnoreCaseAndIdMonHocIgnoreCaseAndNamHoc(
                normalizedStudentId,
                normalizedSubjectId,
                normalizedNamHoc
        );
        scoreDAO.deleteAverageScoresByGroup(normalizedStudentId, normalizedSubjectId, normalizedNamHoc);

        if (deletedRows <= 0) {
            throw new RuntimeException("Không tìm thấy dữ liệu điểm để xóa.");
        }

        logDeleteAction(normalizedStudentId, normalizedSubjectId, normalizedNamHoc, scoreSummary);
    }

    private void logDeleteAction(String studentId,
                                 String subjectId,
                                 String namHoc,
                                 List<Object[]> scoreSummary) {
        String username = resolveCurrentUsername();
        if (username == null) {
            return;
        }

        Object[] row = scoreSummary == null || scoreSummary.isEmpty() ? null : scoreSummary.get(0);
        String studentName = readString(row, 1, studentId);
        String subjectName = readString(row, 3, subjectId);
        String className = readString(row, 4, "-");
        String detail = "Đã xóa nhóm điểm môn " + subjectName
                + " của học sinh " + studentName + " (" + studentId + ")"
                + ", lớp " + className
                + ", năm học " + namHoc + ".";

        activityLogService.logScoreDeleted(
                buildRecordId(studentId, subjectId, namHoc),
                username,
                detail,
                null
        );
    }

    private String resolveCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = safeTrim(authentication.getName());
        if (username.isEmpty() || "anonymousUser".equalsIgnoreCase(username)) {
            return null;
        }
        return username;
    }

    private String buildRecordId(String studentId, String subjectId, String namHoc) {
        return safeValue(studentId) + "|" + safeValue(subjectId) + "|" + safeValue(namHoc);
    }

    private String readString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        String value = safeTrim(String.valueOf(row[index]));
        return value.isEmpty() ? fallback : value;
    }

    private String safeValue(String value) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? "-" : trimmed;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
