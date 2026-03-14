package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ClassManagementDeleteService {

    private static final String ERROR_CLASS_NOT_FOUND = "Khong tim thay lop hoc.";
    private static final String ERROR_DELETE_BLOCKED =
            "Khong the xoa lop hoc vi du lieu lien quan dang ton tai.";
    private static final String ERROR_DELETE_FAILED = "Khong the xoa lop hoc. Vui long thu lai.";

    private final ClassDAO classDAO;

    public ClassManagementDeleteService(ClassDAO classDAO) {
        this.classDAO = classDAO;
    }

    @Transactional
    public void deleteClass(String classId) {
        String normalizedClassId = normalizeUpper(classId);
        if (normalizedClassId == null || !classDAO.existsById(normalizedClassId)) {
            throw new RuntimeException(ERROR_CLASS_NOT_FOUND);
        }

        try {
            classDAO.deleteById(normalizedClassId);
            classDAO.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException(ERROR_DELETE_BLOCKED);
        } catch (Exception ex) {
            throw new RuntimeException(ERROR_DELETE_FAILED);
        }
    }

    private String normalizeUpper(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}
