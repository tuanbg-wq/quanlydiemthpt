package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.entity.ActivityLog;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.StudentClassHistoryService;
import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.admin.StudentService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherStudentService {

    private final StudentService studentService;
    private final StudentClassHistoryService studentClassHistoryService;
    private final ActivityLogService activityLogService;
    private final TeacherStudentScopeService teacherStudentScopeService;

    public TeacherStudentService(StudentService studentService,
                                 StudentClassHistoryService studentClassHistoryService,
                                 ActivityLogService activityLogService,
                                 TeacherStudentScopeService teacherStudentScopeService) {
        this.studentService = studentService;
        this.studentClassHistoryService = studentClassHistoryService;
        this.activityLogService = activityLogService;
        this.teacherStudentScopeService = teacherStudentScopeService;
    }

    @Transactional(readOnly = true)
    public List<Student> searchInScope(TeacherHomeroomScope scope, StudentSearch search) {
        StudentSearch scopedSearch = search == null ? new StudentSearch() : search;
        scopedSearch.setCourseId(null);
        scopedSearch.setKhoi(null);
        scopedSearch.setClassId(scope == null ? null : scope.getClassId());
        return studentService.search(scopedSearch);
    }

    @Transactional(readOnly = true)
    public Student getStudentForDisplay(String studentId, TeacherHomeroomScope scope) {
        Student student = teacherStudentScopeService.getStudentInScopeOrThrow(studentId, scope);
        studentService.populateConductForStudent(student);
        return student;
    }

    @Transactional(readOnly = true)
    public List<?> getStudentClassHistories(String studentId) {
        return studentClassHistoryService.getHistoryByStudent(studentId);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> getStudentEditLogs(String studentId) {
        return activityLogService.getStudentEditLogs(studentId);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogService.ClassStudentActivityItem> getRecentActivitiesByScope(TeacherHomeroomScope scope, int limit) {
        if (!teacherStudentScopeService.hasHomeroomClass(scope)) {
            return List.of();
        }
        return activityLogService.getRecentStudentActivitiesByClassIds(List.of(scope.getClassId()), limit);
    }
}
