package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.entity.Subject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class SubjectDeleteService {

    private final SubjectDAO subjectDAO;

    public SubjectDeleteService(SubjectDAO subjectDAO) {
        this.subjectDAO = subjectDAO;
    }

    public void deleteSubject(String subjectId) {
        Subject subject = findSubjectOrThrow(subjectId);
        try {
            subjectDAO.delete(subject);
            subjectDAO.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException(
                    "Khong the xoa mon hoc nay vi da co du lieu lien quan (phan cong day, diem...)."
            );
        }
    }

    private Subject findSubjectOrThrow(String subjectId) {
        return subjectDAO.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc."));
    }
}
