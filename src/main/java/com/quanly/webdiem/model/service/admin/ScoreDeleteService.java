package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ScoreDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreDeleteService {

    private final ScoreDAO scoreDAO;

    public ScoreDeleteService(ScoreDAO scoreDAO) {
        this.scoreDAO = scoreDAO;
    }

    @Transactional
    public void deleteScoreGroup(String studentId, String subjectId, String namHoc) {
        String normalizedStudentId = safeTrim(studentId);
        String normalizedSubjectId = safeTrim(subjectId);
        String normalizedNamHoc = safeTrim(namHoc);

        long deletedRows = scoreDAO.deleteByIdHocSinhIgnoreCaseAndIdMonHocIgnoreCaseAndNamHoc(
                normalizedStudentId,
                normalizedSubjectId,
                normalizedNamHoc
        );
        scoreDAO.deleteAverageScoresByGroup(normalizedStudentId, normalizedSubjectId, normalizedNamHoc);

        if (deletedRows <= 0) {
            throw new RuntimeException("Không tìm thấy dữ liệu điểm để xóa.");
        }
    }

    private String safeTrim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
