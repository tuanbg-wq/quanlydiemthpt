package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.StudentService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherStudentEditService {

    private final StudentService studentService;
    private final TeacherStudentScopeService teacherStudentScopeService;

    public TeacherStudentEditService(StudentService studentService,
                                     TeacherStudentScopeService teacherStudentScopeService) {
        this.studentService = studentService;
        this.teacherStudentScopeService = teacherStudentScopeService;
    }

    @Transactional(readOnly = true)
    public Student getStudentForEdit(String studentId, TeacherHomeroomScope scope) {
        Student student = teacherStudentScopeService.getStudentInScopeOrThrow(studentId, scope);
        studentService.populateConductForStudent(student);
        return student;
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
            throw new RuntimeException("Không xác định được lớp hiện tại của học sinh.");
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
