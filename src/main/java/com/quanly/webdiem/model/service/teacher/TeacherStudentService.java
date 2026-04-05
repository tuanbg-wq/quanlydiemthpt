package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.entity.ActivityLog;
import com.quanly.webdiem.model.service.admin.ActivityLogService;
import com.quanly.webdiem.model.service.admin.StudentClassHistoryService;
import com.quanly.webdiem.model.service.admin.StudentSearch;
import com.quanly.webdiem.model.service.admin.StudentService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional(readOnly = true)
    public String suggestNextStudentId() {
        return studentService.suggestNextStudentId();
    }

    @Transactional
    public void createStudentForHomeroom(Student student,
                                         TeacherHomeroomScope scope,
                                         MultipartFile avatar,
                                         String username,
                                         String ipAddress) {
        ClassEntity homeroomClass = teacherStudentScopeService.getHomeroomClassOrThrow(scope);
        if (homeroomClass.getKhoaHoc() == null || homeroomClass.getKhoi() == null) {
            throw new RuntimeException("KhĂ´ng thá»ƒ xĂ¡c Ä‘á»‹nh thĂ´ng tin khĂ³a há»c hoáº·c khá»‘i cá»§a lá»›p chá»§ nhiá»‡m.");
        }
        studentService.createWithAutoCourseClass(
                student,
                homeroomClass.getKhoaHoc().getIdKhoa(),
                homeroomClass.getKhoaHoc().getTenKhoa(),
                homeroomClass.getIdLop(),
                homeroomClass.getKhoi(),
                avatar,
                username,
                ipAddress
        );
    }

    @Transactional
    public void updateStudentInScope(String studentId,
                                     Student formStudent,
                                     String transferClassId,
                                     MultipartFile avatar,
                                     TeacherHomeroomScope scope,
                                     String username,
                                     String ipAddress) {
        Student currentStudent = teacherStudentScopeService.getStudentInScopeOrThrow(studentId, scope);
        ClassEntity currentClass = currentStudent.getLop();
        if (currentClass == null || currentClass.getKhoaHoc() == null || currentClass.getKhoi() == null) {
            throw new RuntimeException("KhĂ´ng xĂ¡c Ä‘á»‹nh Ä‘Æ°á»£c lá»›p hiá»‡n táº¡i cá»§a há»c sinh.");
        }
        studentService.updateStudent(
                studentId,
                formStudent,
                currentClass.getKhoaHoc().getIdKhoa(),
                currentClass.getKhoaHoc().getTenKhoa(),
                currentClass.getKhoi(),
                currentClass.getIdLop(),
                transferClassId,
                avatar,
                username,
                ipAddress
        );
    }

    @Transactional
    public void deleteStudentInScope(String studentId,
                                     TeacherHomeroomScope scope,
                                     String username,
                                     String ipAddress) {
        teacherStudentScopeService.getStudentInScopeOrThrow(studentId, scope);
        studentService.deleteStudent(studentId, username, ipAddress);
    }
}
