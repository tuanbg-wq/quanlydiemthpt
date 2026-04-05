package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateFilter;
import com.quanly.webdiem.model.service.admin.ConductRewardCreatePageData;
import com.quanly.webdiem.model.service.admin.ConductRewardCreateRequest;
import com.quanly.webdiem.model.service.admin.ConductStudentCandidate;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherConductCreateService {

    private final TeacherConductService teacherConductService;

    public TeacherConductCreateService(TeacherConductService teacherConductService) {
        this.teacherConductService = teacherConductService;
    }

    @Transactional(readOnly = true)
    public ConductRewardCreatePageData getCreatePageData(TeacherHomeroomScope scope,
                                                         ConductRewardCreateFilter filter) {
        return teacherConductService.getCreatePageData(scope, filter);
    }

    @Transactional(readOnly = true)
    public List<ConductStudentCandidate> suggestStudents(TeacherHomeroomScope scope, String q) {
        return teacherConductService.suggestStudents(scope, q);
    }

    @Transactional(readOnly = true)
    public String suggestRewardDecisionNumber() {
        return teacherConductService.suggestRewardDecisionNumber();
    }

    @Transactional(readOnly = true)
    public String suggestDisciplineDecisionNumber() {
        return teacherConductService.suggestDisciplineDecisionNumber();
    }

    public void applyDefaultRewardDecisionNumber(ConductRewardCreateRequest request) {
        teacherConductService.applyDefaultRewardDecisionNumber(request);
    }

    public void applyDefaultDisciplineDecisionNumber(ConductRewardCreateRequest request) {
        teacherConductService.applyDefaultDisciplineDecisionNumber(request);
    }

    @Transactional
    public void createReward(TeacherHomeroomScope scope, ConductRewardCreateRequest request) {
        teacherConductService.createReward(scope, request);
    }

    @Transactional
    public void createDiscipline(TeacherHomeroomScope scope, ConductRewardCreateRequest request) {
        teacherConductService.createDiscipline(scope, request);
    }

    @Transactional(readOnly = true)
    public ConductManagementService.ConductRow getLatestReward(TeacherHomeroomScope scope, String studentId) {
        return teacherConductService.getLatestEventByStudentAndTypeInScope(
                scope,
                studentId,
                ConductManagementService.LOAI_KHEN_THUONG
        );
    }

    @Transactional(readOnly = true)
    public ConductManagementService.ConductRow getLatestDiscipline(TeacherHomeroomScope scope, String studentId) {
        return teacherConductService.getLatestEventByStudentAndTypeInScope(
                scope,
                studentId,
                ConductManagementService.LOAI_KY_LUAT
        );
    }
}
