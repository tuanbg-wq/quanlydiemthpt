package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.SubjectCreateForm;
import com.quanly.webdiem.model.entity.SubjectSharedService;
import com.quanly.webdiem.model.entity.Subject;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class SubjectCreateService {

    private final SubjectDAO subjectDAO;
    private final TeacherDAO teacherDAO;
    private final SubjectUpdateService updateService;
    private final SubjectSharedService sharedService;

    public SubjectCreateService(SubjectDAO subjectDAO,
                                TeacherDAO teacherDAO,
                                SubjectUpdateService updateService,
                                SubjectSharedService sharedService) {
        this.subjectDAO = subjectDAO;
        this.teacherDAO = teacherDAO;
        this.updateService = updateService;
        this.sharedService = sharedService;
    }

    public void createSubject(SubjectCreateForm form) {
        if (form == null) {
            throw new RuntimeException("Du lieu mon hoc khong hop le.");
        }

        String idMonHoc = sharedService.normalize(form.getIdMonHoc());
        if (idMonHoc == null) {
            throw new RuntimeException("Ma mon hoc khong duoc de trong.");
        }

        idMonHoc = idMonHoc.toUpperCase(Locale.ROOT);
        if (subjectDAO.existsById(idMonHoc)) {
            throw new RuntimeException("Ma mon hoc da ton tai.");
        }
        if (teacherDAO.existsById(idMonHoc)) {
            throw new RuntimeException("Ma mon hoc khong duoc trung voi ma giao vien.");
        }

        Subject subject = new Subject();
        subject.setIdMonHoc(idMonHoc);
        updateService.applyEditableSubjectFields(subject, form);
        subjectDAO.save(subject);
    }
}
