package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.form.SubjectCreateForm;
import com.quanly.webdiem.model.search.SubjectSearch;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectQueryService queryService;
    private final SubjectFormService formService;
    private final SubjectCreateService createService;
    private final SubjectUpdateService updateService;
    private final SubjectDeleteService deleteService;

    public SubjectService(SubjectQueryService queryService,
                          SubjectFormService formService,
                          SubjectCreateService createService,
                          SubjectUpdateService updateService,
                          SubjectDeleteService deleteService) {
        this.queryService = queryService;
        this.formService = formService;
        this.createService = createService;
        this.updateService = updateService;
        this.deleteService = deleteService;
    }

    public SubjectPageResult search(SubjectSearch search) {
        return queryService.search(search);
    }

    public SubjectCreateForm getEditForm(String subjectId) {
        return formService.getEditForm(subjectId);
    }

    public void createSubject(SubjectCreateForm form) {
        createService.createSubject(form);
    }

    public void updateSubject(String subjectId, SubjectCreateForm form) {
        updateService.updateSubject(subjectId, form);
    }

    public void deleteSubject(String subjectId) {
        deleteService.deleteSubject(subjectId);
    }

    public List<Integer> getGrades() {
        return queryService.getGrades();
    }

    public List<String> getDepartments() {
        return queryService.getDepartments();
    }

    public List<Course> getCoursesForForm() {
        return formService.getCoursesForForm();
    }

    public List<String> getSchoolYearsForForm() {
        return formService.getSchoolYearsForForm();
    }

    public List<TeacherOption> getTeachersForForm() {
        return formService.getTeachersForForm();
    }

    public List<SuggestionItem> suggestCourses(String query) {
        return formService.suggestCourses(query);
    }

    public List<SuggestionItem> suggestSchoolYears(String query) {
        return formService.suggestSchoolYears(query);
    }

    public List<SuggestionItem> suggestTeachers(String query) {
        return formService.suggestTeachers(query);
    }

    public static class SubjectRow {
        private final String idMonHoc;
        private final String tenMonHoc;
        private final List<String> khoiLopList;
        private final int soDiemThuongXuyen;
        private final String namHoc;
        private final String hocKy;
        private final String toBoMon;
        private final String giaoVienChinh;
        private final int soGiaoVienKhac;

        public SubjectRow(String idMonHoc,
                          String tenMonHoc,
                          List<String> khoiLopList,
                          int soDiemThuongXuyen,
                          String namHoc,
                          String hocKy,
                          String toBoMon,
                          String giaoVienChinh,
                          int soGiaoVienKhac) {
            this.idMonHoc = idMonHoc;
            this.tenMonHoc = tenMonHoc;
            this.khoiLopList = khoiLopList;
            this.soDiemThuongXuyen = soDiemThuongXuyen;
            this.namHoc = namHoc;
            this.hocKy = hocKy;
            this.toBoMon = toBoMon;
            this.giaoVienChinh = giaoVienChinh;
            this.soGiaoVienKhac = soGiaoVienKhac;
        }

        public String getIdMonHoc() {
            return idMonHoc;
        }

        public String getTenMonHoc() {
            return tenMonHoc;
        }

        public List<String> getKhoiLopList() {
            return khoiLopList;
        }

        public int getSoDiemThuongXuyen() {
            return soDiemThuongXuyen;
        }

        public String getNamHoc() {
            return namHoc;
        }

        public String getHocKy() {
            return hocKy;
        }

        public String getToBoMon() {
            return toBoMon;
        }

        public String getGiaoVienChinh() {
            return giaoVienChinh;
        }

        public int getSoGiaoVienKhac() {
            return soGiaoVienKhac;
        }
    }

    public static class SubjectPageResult {
        private final List<SubjectRow> items;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final int fromRecord;
        private final int toRecord;

        public SubjectPageResult(List<SubjectRow> items,
                                 int page,
                                 int totalPages,
                                 int totalItems,
                                 int fromRecord,
                                 int toRecord) {
            this.items = items;
            this.page = page;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.fromRecord = fromRecord;
            this.toRecord = toRecord;
        }

        public List<SubjectRow> getItems() {
            return items;
        }

        public int getPage() {
            return page;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public int getFromRecord() {
            return fromRecord;
        }

        public int getToRecord() {
            return toRecord;
        }
    }

    public static class TeacherOption {
        private final String id;
        private final String name;

        public TeacherOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class SuggestionItem {
        private final String value;
        private final String label;

        public SuggestionItem(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}
