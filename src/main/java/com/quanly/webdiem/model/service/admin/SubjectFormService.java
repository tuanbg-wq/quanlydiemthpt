package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.form.SubjectCreateForm;
import com.quanly.webdiem.model.service.shared.SubjectSharedService;
import com.quanly.webdiem.model.entity.Subject;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class SubjectFormService {

    private static final String META_TX_KEY = "So cot diem thuong xuyen";
    private static final String META_NOTE_KEY = "Ghi chu";
    private static final int DEFAULT_TX_COUNT = 3;

    private final SubjectDAO subjectDAO;
    private final CourseDAO courseDAO;
    private final SubjectSharedService sharedService;

    public SubjectFormService(SubjectDAO subjectDAO,
                              CourseDAO courseDAO,
                              SubjectSharedService sharedService) {
        this.subjectDAO = subjectDAO;
        this.courseDAO = courseDAO;
        this.sharedService = sharedService;
    }

    public SubjectCreateForm getEditForm(String subjectId) {
        Subject subject = findSubjectOrThrow(subjectId);
        Map<String, String> metadata = sharedService.parseMetadata(subject.getMoTa());
        String hocKyCode = sharedService.toHocKyCode(subject.getHocKyApDung());
        if (hocKyCode == null) {
            hocKyCode = sharedService.toHocKyCode(metadata.get("Ky hoc ap dung"));
        }
        String moTa = resolveFormDescription(subject.getMoTa(), metadata);

        SubjectCreateForm form = new SubjectCreateForm();
        form.setIdMonHoc(subject.getIdMonHoc());
        form.setTenMonHoc(subject.getTenMonHoc());
        form.setCourseId(sharedService.defaultIfBlank(subject.getIdKhoa(), sharedService.parseCourseId(metadata.get("Khoa hoc ap dung"))));
        form.setNamHoc(sharedService.defaultIfBlank(subject.getNamHocApDung(), sharedService.defaultIfBlank(metadata.get("Nam hoc ap dung"), null)));
        form.setHocKy(hocKyCode);
        form.setKhoiApDung(sharedService.defaultIfBlank(subject.getKhoiApDung(), sharedService.defaultIfBlank(metadata.get("Khoi lop ap dung"), null)));
        form.setToBoMon(sharedService.defaultIfBlank(subject.getToBoMon(), sharedService.defaultIfBlank(metadata.get("To bo mon"), null)));
        form.setSoDiemThuongXuyen(resolveFrequentScoreCount(metadata));
        form.setGiaoVienPhuTrach(sharedService.defaultIfBlank(
                subject.getIdGiaoVienPhuTrach(),
                sharedService.parseTeacherId(metadata.get("Giao vien phu trach"))
        ));
        form.setMoTa(moTa);
        return form;
    }

    public List<Course> getCoursesForForm() {
        return courseDAO.findAll().stream()
                .sorted(Comparator.comparing(Course::getIdKhoa))
                .toList();
    }

    public List<String> getSchoolYearsForForm() {
        List<String> schoolYears = subjectDAO.findSchoolYears();
        if (!schoolYears.isEmpty()) {
            return schoolYears;
        }
        return List.of("2025-2026", "2026-2027");
    }

    public List<SubjectService.TeacherOption> getTeachersForForm() {
        return subjectDAO.findTeacherOptions().stream()
                .map(sharedService::mapTeacherOption)
                .filter(t -> t.getId() != null && t.getName() != null)
                .toList();
    }

    public List<SubjectService.SuggestionItem> suggestCourses(String query) {
        String q = sharedService.normalize(query);
        return subjectDAO.findCourseSuggestions(q).stream()
                .map(this::mapCourseSuggestion)
                .filter(item -> item.getValue() != null && item.getLabel() != null)
                .toList();
    }

    public List<SubjectService.SuggestionItem> suggestSchoolYears(String query) {
        String q = sharedService.normalize(query);
        return subjectDAO.findSchoolYearSuggestions(q).stream()
                .map(sharedService::normalize)
                .filter(v -> v != null)
                .map(v -> new SubjectService.SuggestionItem(v, v))
                .toList();
    }

    public List<SubjectService.SuggestionItem> suggestTeachers(String query) {
        String q = sharedService.normalize(query);
        return subjectDAO.findTeacherSuggestions(q).stream()
                .map(sharedService::mapTeacherOption)
                .filter(t -> t.getId() != null && t.getName() != null)
                .map(t -> new SubjectService.SuggestionItem(t.getId(), t.getName() + " (" + t.getId() + ")"))
                .toList();
    }

    private SubjectService.SuggestionItem mapCourseSuggestion(Object[] row) {
        String id = sharedService.asString(row, 0, null);
        String name = sharedService.asString(row, 1, null);
        String startYear = sharedService.asString(row, 2, null);

        if (id == null || name == null) {
            return new SubjectService.SuggestionItem(null, null);
        }

        String detail = sharedService.normalize(name);
        if (detail == null || detail.isBlank()) {
            detail = startYear == null ? "" : ("khoa " + startYear);
        } else {
            detail = sharedService.lowerCaseFirst(detail);
        }

        String label = detail.isBlank() ? id : (id + "(" + detail + ")");
        return new SubjectService.SuggestionItem(id, label);
    }

    private Subject findSubjectOrThrow(String subjectId) {
        return subjectDAO.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc."));
    }

    private String resolveFormDescription(String currentDescription, Map<String, String> metadata) {
        String normalized = sharedService.normalize(currentDescription);
        if (normalized == null) {
            return null;
        }
        String metadataNote = sharedService.defaultIfBlank(metadata.get(META_NOTE_KEY), null);
        if (metadataNote != null) {
            return metadataNote;
        }
        if (metadata.containsKey(META_TX_KEY)) {
            return null;
        }
        if (normalized.contains("Khoa hoc ap dung:") || normalized.contains("Nam hoc ap dung:")) {
            return sharedService.defaultIfBlank(metadata.get("Ghi chu"), null);
        }
        return normalized;
    }

    private Integer resolveFrequentScoreCount(Map<String, String> metadata) {
        String rawValue = sharedService.defaultIfBlank(metadata.get(META_TX_KEY), null);
        if (rawValue == null) {
            return DEFAULT_TX_COUNT;
        }
        try {
            int parsed = Integer.parseInt(rawValue);
            if (parsed >= 2 && parsed <= 4) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // fall back to default when metadata is invalid.
        }
        return DEFAULT_TX_COUNT;
    }
}
