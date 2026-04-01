package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService.TeacherHomeroomScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class TeacherStudentScopeService {

    private final TeacherHomeroomScopeService teacherHomeroomScopeService;
    private final StudentDAO studentDAO;
    private final ClassDAO classDAO;

    public TeacherStudentScopeService(TeacherHomeroomScopeService teacherHomeroomScopeService,
                                      StudentDAO studentDAO,
                                      ClassDAO classDAO) {
        this.teacherHomeroomScopeService = teacherHomeroomScopeService;
        this.studentDAO = studentDAO;
        this.classDAO = classDAO;
    }

    @Transactional(readOnly = true)
    public TeacherHomeroomScope resolveScopeByUsername(String username) {
        return teacherHomeroomScopeService.resolveByUsername(username);
    }

    public boolean hasHomeroomClass(TeacherHomeroomScope scope) {
        return scope != null && scope.hasHomeroomClass();
    }

    @Transactional(readOnly = true)
    public Student getStudentOrThrow(String studentId) {
        return studentDAO.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh."));
    }

    @Transactional(readOnly = true)
    public Student getStudentInScopeOrThrow(String studentId, TeacherHomeroomScope scope) {
        Student student = getStudentOrThrow(studentId);
        if (!isStudentInScope(student, scope)) {
            throw new RuntimeException("Bạn chỉ được thao tác học sinh thuộc lớp chủ nhiệm.");
        }
        return student;
    }

    @Transactional(readOnly = true)
    public ClassEntity getHomeroomClassOrThrow(TeacherHomeroomScope scope) {
        if (!hasHomeroomClass(scope)) {
            throw new RuntimeException("Tài khoản chưa được phân công lớp chủ nhiệm.");
        }
        return classDAO.findById(scope.getClassId())
                .orElseThrow(() -> new RuntimeException("Không thể xác định lớp chủ nhiệm hiện tại."));
    }

    @Transactional(readOnly = true)
    public List<ClassEntity> getTransferClassOptions() {
        return classDAO.findAll().stream()
                .sorted(Comparator.comparing(ClassEntity::getIdLop, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public boolean isStudentInScope(Student student, TeacherHomeroomScope scope) {
        if (student == null || !hasHomeroomClass(scope) || student.getLop() == null) {
            return false;
        }
        String studentClassId = student.getLop().getIdLop();
        return studentClassId != null && studentClassId.equalsIgnoreCase(scope.getClassId());
    }
}
