package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.service.admin.ConductEventUpsertRequest;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherConductEditService {

    private final TeacherConductService teacherConductService;

    public TeacherConductEditService(TeacherConductService teacherConductService) {
        this.teacherConductService = teacherConductService;
    }

    @Transactional(readOnly = true)
    public ConductManagementService.ConductRow getEventDetailInScope(TeacherHomeroomScope scope, Long eventId) {
        return teacherConductService.getEventDetailInScope(scope, eventId);
    }

    @Transactional(readOnly = true)
    public ConductEventUpsertRequest getEditDataInScope(TeacherHomeroomScope scope, Long eventId) {
        return teacherConductService.getEditDataInScope(scope, eventId);
    }

    @Transactional
    public void updateEvent(TeacherHomeroomScope scope, ConductEventUpsertRequest request) {
        teacherConductService.updateEvent(scope, request);
    }

    @Transactional
    public void deleteEvent(TeacherHomeroomScope scope, Long eventId) {
        teacherConductService.deleteEvent(scope, eventId);
    }
}
