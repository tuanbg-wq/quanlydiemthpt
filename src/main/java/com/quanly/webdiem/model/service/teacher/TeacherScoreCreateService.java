package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherScoreCreateService {

    private final ScoreCreateService scoreCreateService;

    public TeacherScoreCreateService(ScoreCreateService scoreCreateService) {
        this.scoreCreateService = scoreCreateService;
    }

    @Transactional(readOnly = true)
    public ScoreCreateService.ScoreCreatePageData getCreatePageData(ScoreCreateService.ScoreCreateFilter filter) {
        return scoreCreateService.getCreatePageData(filter);
    }

    @Transactional
    public void save(ScoreCreateService.ScoreSaveRequest request) {
        scoreCreateService.save(request);
    }

    @Transactional(readOnly = true)
    public List<ScoreCreateService.StudentItem> suggestStudents(String classId, String q) {
        return scoreCreateService.suggestStudents(classId, q);
    }
}
