package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.SubjectCreateForm;
import com.quanly.webdiem.model.entity.SubjectSharedService;
import com.quanly.webdiem.model.entity.Subject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SubjectUpdateService {

    private static final String META_TX_KEY = "So cot diem thuong xuyen";
    private static final String META_NOTE_KEY = "Ghi chu";
    private static final int DEFAULT_TX_COUNT = 3;

    private final SubjectDAO subjectDAO;
    private final TeacherDAO teacherDAO;
    private final CourseDAO courseDAO;
    private final SubjectSharedService sharedService;

    public SubjectUpdateService(SubjectDAO subjectDAO,
                                TeacherDAO teacherDAO,
                                CourseDAO courseDAO,
                                SubjectSharedService sharedService) {
        this.subjectDAO = subjectDAO;
        this.teacherDAO = teacherDAO;
        this.courseDAO = courseDAO;
        this.sharedService = sharedService;
    }

    @Transactional
    public void updateSubject(String subjectId, SubjectCreateForm form) {
        Subject subject = findSubjectOrThrow(subjectId);
        String currentSubjectId = subject.getIdMonHoc();
        String requestedSubjectId = normalizeSubjectId(form.getIdMonHoc(), "Ma mon hoc khong duoc de trong.");

        validateTargetSubjectId(currentSubjectId, requestedSubjectId);

        applyEditableSubjectFields(subject, form);
        subjectDAO.save(subject);

        if (!currentSubjectId.equalsIgnoreCase(requestedSubjectId)) {
            renameSubjectWithReferences(currentSubjectId, requestedSubjectId);
        }
    }

    Subject findSubjectOrThrow(String subjectId) {
        return subjectDAO.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc."));
    }

    void applyEditableSubjectFields(Subject subject, SubjectCreateForm form) {
        if (form == null) {
            throw new RuntimeException("Du lieu mon hoc khong hop le.");
        }

        String tenMonHoc = sharedService.normalize(form.getTenMonHoc());
        if (tenMonHoc == null) {
            throw new RuntimeException("Ten mon hoc khong duoc de trong.");
        }

        String courseId = sharedService.normalize(form.getCourseId());
        if (courseId == null) {
            throw new RuntimeException("Vui long chon khoa hoc.");
        }

        String namHoc = sharedService.normalize(form.getNamHoc());
        if (namHoc == null) {
            throw new RuntimeException("Nam hoc khong duoc de trong.");
        }

        String hocKyCode = sharedService.toHocKyCode(form.getHocKy());
        if (hocKyCode == null) {
            throw new RuntimeException("Ky hoc khong hop le.");
        }

        String khoiApDung = normalizeGradeList(form.getKhoiApDung());

        String toBoMon = sharedService.normalize(form.getToBoMon());
        if (toBoMon == null) {
            throw new RuntimeException("To bo mon khong duoc de trong.");
        }
        Integer soDiemThuongXuyen = normalizeFrequentScoreCount(form.getSoDiemThuongXuyen());

        if (!courseDAO.existsById(courseId)) {
            throw new RuntimeException("Khoa hoc khong ton tai.");
        }

        String giaoVienPhuTrach = normalizeTeacherId(form.getGiaoVienPhuTrach());
        if (giaoVienPhuTrach != null && subjectDAO.countTeachersById(giaoVienPhuTrach) == 0) {
            throw new RuntimeException("Giao vien phu trach khong ton tai.");
        }

        form.setCourseId(courseId);
        form.setNamHoc(namHoc);
        form.setHocKy(hocKyCode);
        form.setKhoiApDung(khoiApDung);
        form.setToBoMon(toBoMon);
        form.setSoDiemThuongXuyen(soDiemThuongXuyen);
        form.setGiaoVienPhuTrach(giaoVienPhuTrach);

        subject.setTenMonHoc(tenMonHoc);
        subject.setIdKhoa(courseId);
        subject.setNamHocApDung(namHoc);
        subject.setHocKyApDung(hocKyCode);
        subject.setKhoiApDung(khoiApDung);
        subject.setToBoMon(toBoMon);
        subject.setIdGiaoVienPhuTrach(giaoVienPhuTrach);
        subject.setMoTa(buildDescriptionWithMetadata(sharedService.normalize(form.getMoTa()), soDiemThuongXuyen));
    }

    private void validateTargetSubjectId(String currentSubjectId, String requestedSubjectId) {
        if (requestedSubjectId.equalsIgnoreCase(currentSubjectId)) {
            return;
        }

        if (subjectDAO.existsById(requestedSubjectId)) {
            throw new RuntimeException("Ma mon hoc da ton tai.");
        }
        if (teacherDAO.existsById(requestedSubjectId)) {
            throw new RuntimeException("Ma mon hoc khong duoc trung voi ma giao vien.");
        }
    }

    private void renameSubjectWithReferences(String oldSubjectId, String newSubjectId) {
        try {
            subjectDAO.reassignSubjectIdInAverageScores(oldSubjectId, newSubjectId);
            subjectDAO.reassignSubjectIdInScores(oldSubjectId, newSubjectId);
            subjectDAO.reassignSubjectIdInTeachingAssignments(oldSubjectId, newSubjectId);

            int updated = subjectDAO.renameSubjectId(oldSubjectId, newSubjectId);
            if (updated != 1) {
                throw new RuntimeException("Khong the cap nhat ma mon hoc.");
            }
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Khong the doi ma mon hoc do co du lieu lien quan.");
        }
    }

    private String normalizeSubjectId(String rawSubjectId, String requiredMessage) {
        String normalized = sharedService.normalize(rawSubjectId);
        if (normalized == null) {
            throw new RuntimeException(requiredMessage);
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeTeacherId(String rawTeacher) {
        String teacherId = sharedService.parseTeacherId(rawTeacher);
        if (teacherId == null) {
            return null;
        }
        return teacherId.toUpperCase(Locale.ROOT);
    }

    private Integer normalizeFrequentScoreCount(Integer rawCount) {
        int value = rawCount == null ? DEFAULT_TX_COUNT : rawCount;
        if (value < 2 || value > 4) {
            throw new RuntimeException("So diem thuong xuyen phai trong khoang 2 den 4.");
        }
        return value;
    }

    private String buildDescriptionWithMetadata(String note, int soDiemThuongXuyen) {
        StringBuilder builder = new StringBuilder();
        builder.append(META_TX_KEY).append(": ").append(soDiemThuongXuyen);
        if (note != null) {
            builder.append('\n').append(META_NOTE_KEY).append(": ").append(note);
        }
        return builder.toString();
    }

    private String normalizeGradeList(String rawGrades) {
        String normalized = sharedService.normalize(rawGrades);
        if (normalized == null) {
            throw new RuntimeException("Khoi lop khong duoc de trong.");
        }

        List<String> parts = sharedService.splitCsv(normalized, ",");
        if (parts.isEmpty()) {
            throw new RuntimeException("Khoi lop khong hop le.");
        }

        List<String> unique = new ArrayList<>();
        for (String part : parts) {
            String grade = sharedService.normalize(part);
            if (grade == null || !grade.matches("\\d{1,2}")) {
                throw new RuntimeException("Khoi lop khong hop le.");
            }
            if (!unique.contains(grade)) {
                unique.add(grade);
            }
        }

        return String.join(",", unique);
    }
}
