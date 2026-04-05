package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.admin.StudentService;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherStudentCreateService {

    private final TeacherStudentScopeService teacherStudentScopeService;
    private final StudentService studentService;

    public TeacherStudentCreateService(TeacherStudentScopeService teacherStudentScopeService,
                                       StudentService studentService) {
        this.teacherStudentScopeService = teacherStudentScopeService;
        this.studentService = studentService;
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
            throw new RuntimeException("Không thể xác định thông tin khóa học hoặc khối của lớp chủ nhiệm.");
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
}
