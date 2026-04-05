package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.service.admin.ScoreCreateService;
import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherScoreEditService {

    private final ScoreCreateService scoreCreateService;
    private final ScoreManagementService scoreManagementService;

    public TeacherScoreEditService(ScoreCreateService scoreCreateService,
                                   ScoreManagementService scoreManagementService) {
        this.scoreCreateService = scoreCreateService;
        this.scoreManagementService = scoreManagementService;
    }

    @Transactional(readOnly = true)
    public ScoreCreateService.ScoreCreatePageData getPageData(ScoreCreateService.ScoreCreateFilter filter) {
        return scoreCreateService.getCreatePageData(filter);
    }

    @Transactional
    public void save(ScoreCreateService.ScoreSaveRequest request) {
        scoreCreateService.save(request);
    }

    @Transactional
    public void deleteScoreGroup(String studentId, String subjectId, String schoolYear) {
        scoreManagementService.deleteScoreGroup(studentId, subjectId, schoolYear);
    }
}
