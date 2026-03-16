package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ScoreDAO;
import com.quanly.webdiem.model.entity.Score;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreUpdateService {

    private final ScoreDAO scoreDAO;

    public ScoreUpdateService(ScoreDAO scoreDAO) {
        this.scoreDAO = scoreDAO;
    }

    @Transactional
    public void updateScoreEntries(String studentId,
                                   String subjectId,
                                   String namHoc,
                                   List<ScoreManagementService.ScoreEntryUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            throw new RuntimeException("Không có dữ liệu điểm để cập nhật.");
        }

        List<Score> scores = scoreDAO.findScoresForEdit(studentId, subjectId, namHoc);
        if (scores.isEmpty()) {
            throw new RuntimeException("Không tìm thấy bản ghi điểm để cập nhật.");
        }

        Map<Integer, ScoreManagementService.ScoreEntryUpdate> updateMap = new HashMap<>();
        for (ScoreManagementService.ScoreEntryUpdate update : updates) {
            if (update == null || update.getScoreId() == null) {
                continue;
            }
            updateMap.put(update.getScoreId(), update);
        }

        for (Score score : scores) {
            ScoreManagementService.ScoreEntryUpdate update = updateMap.get(score.getIdDiem());
            if (update == null) {
                continue;
            }

            BigDecimal parsedScore = parseScore(update.getScoreValue());
            score.setDiem(parsedScore);
            score.setGhiChu(defaultIfBlank(update.getScoreNote(), null));
        }

        scoreDAO.saveAll(scores);
    }

    private BigDecimal parseScore(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("Điểm không được để trống.");
        }

        try {
            BigDecimal value = new BigDecimal(raw.trim())
                    .setScale(2, RoundingMode.HALF_UP);
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.TEN) > 0) {
                throw new RuntimeException("Điểm phải nằm trong khoảng từ 0 đến 10.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Điểm phải là số hợp lệ.");
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }
        return trimmed;
    }
}
