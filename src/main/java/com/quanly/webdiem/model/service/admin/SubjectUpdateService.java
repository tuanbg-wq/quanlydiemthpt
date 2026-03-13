package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.entity.SubjectCreateForm;
import com.quanly.webdiem.model.entity.SubjectSharedService;
import com.quanly.webdiem.model.entity.Subject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SubjectUpdateService {

    private final SubjectDAO subjectDAO;
    private final CourseDAO courseDAO;
    private final SubjectSharedService sharedService;

    public SubjectUpdateService(SubjectDAO subjectDAO,
                                CourseDAO courseDAO,
                                SubjectSharedService sharedService) {
        this.subjectDAO = subjectDAO;
        this.courseDAO = courseDAO;
        this.sharedService = sharedService;
    }

    public void updateSubject(String subjectId, SubjectCreateForm form) {
        Subject subject = findSubjectOrThrow(subjectId);
        applyEditableSubjectFields(subject, form);
        subjectDAO.save(subject);
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
        form.setGiaoVienPhuTrach(giaoVienPhuTrach);

        subject.setTenMonHoc(tenMonHoc);
        subject.setIdKhoa(courseId);
        subject.setNamHocApDung(namHoc);
        subject.setHocKyApDung(hocKyCode);
        subject.setKhoiApDung(khoiApDung);
        subject.setToBoMon(toBoMon);
        subject.setIdGiaoVienPhuTrach(giaoVienPhuTrach);
        subject.setMoTa(sharedService.normalize(form.getMoTa()));
    }

    private String normalizeTeacherId(String rawTeacher) {
        String teacherId = sharedService.parseTeacherId(rawTeacher);
        if (teacherId == null) {
            return null;
        }
        return teacherId.toUpperCase(Locale.ROOT);
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
