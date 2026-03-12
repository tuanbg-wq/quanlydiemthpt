package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.entity.SubjectCreateForm;
import com.quanly.webdiem.model.entity.SubjectSharedService;
import com.quanly.webdiem.model.entity.Subject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectUpdateService {

    private final SubjectDAO subjectDAO;
    private final CourseDAO courseDAO;
    private final SubjectFormService formService;
    private final SubjectSharedService sharedService;

    public SubjectUpdateService(SubjectDAO subjectDAO,
                                CourseDAO courseDAO,
                                SubjectFormService formService,
                                SubjectSharedService sharedService) {
        this.subjectDAO = subjectDAO;
        this.courseDAO = courseDAO;
        this.formService = formService;
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

        String khoiApDung = sharedService.normalize(form.getKhoiApDung());
        if (khoiApDung == null) {
            throw new RuntimeException("Khoi lop khong duoc de trong.");
        }

        String toBoMon = sharedService.normalize(form.getToBoMon());
        if (toBoMon == null) {
            throw new RuntimeException("To bo mon khong duoc de trong.");
        }

        Course course = courseDAO.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khoa hoc khong ton tai."));

        form.setCourseId(courseId);
        form.setNamHoc(namHoc);
        form.setHocKy(hocKyCode);
        form.setKhoiApDung(khoiApDung);
        form.setToBoMon(toBoMon);

        subject.setTenMonHoc(tenMonHoc);
        if (subject.getSoTiet() == null || subject.getSoTiet() <= 0) {
            subject.setSoTiet(45);
        }
        subject.setMoTa(buildMetadataDescription(form, course));
    }

    private String buildMetadataDescription(SubjectCreateForm form, Course course) {
        List<String> metadataLines = new ArrayList<>();
        metadataLines.add("Khoa hoc ap dung: " + course.getIdKhoa() + " - " + course.getTenKhoa());
        metadataLines.add("Nam hoc ap dung: " + sharedService.defaultIfBlank(form.getNamHoc(), "-"));
        metadataLines.add("Ky hoc ap dung: "
                + sharedService.toHocKyDisplay(sharedService.defaultIfBlank(form.getHocKy(),
                SubjectSharedService.HOC_KY_CA_NAM)));
        metadataLines.add("Khoi lop ap dung: " + sharedService.defaultIfBlank(form.getKhoiApDung(), "-"));
        metadataLines.add("To bo mon: " + sharedService.defaultIfBlank(form.getToBoMon(), "-"));
        metadataLines.add("Giao vien phu trach: " + formService.resolveTeacherDisplay(form.getGiaoVienPhuTrach()));

        String note = sharedService.normalize(form.getMoTa());
        if (note != null) {
            metadataLines.add("Ghi chu: " + note);
        }

        return String.join("\n", metadataLines);
    }
}
