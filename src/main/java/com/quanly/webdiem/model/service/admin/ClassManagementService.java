package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.form.ClassCreateForm;
import com.quanly.webdiem.model.search.ClassSearch;
import com.quanly.webdiem.model.form.CourseCreateForm;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassManagementService {

    private final ClassManagementQueryService queryService;
    private final ClassManagementCreateService createService;
    private final CourseManagementCreateService courseCreateService;
    private final CourseManagementUpdateService courseUpdateService;
    private final CourseManagementDeleteService courseDeleteService;
    private final ClassManagementUpdateService updateService;
    private final ClassManagementDeleteService deleteService;

    public ClassManagementService(ClassManagementQueryService queryService,
                                  ClassManagementCreateService createService,
                                  CourseManagementCreateService courseCreateService,
                                  CourseManagementUpdateService courseUpdateService,
                                  CourseManagementDeleteService courseDeleteService,
                                  ClassManagementUpdateService updateService,
                                  ClassManagementDeleteService deleteService) {
        this.queryService = queryService;
        this.createService = createService;
        this.courseCreateService = courseCreateService;
        this.courseUpdateService = courseUpdateService;
        this.courseDeleteService = courseDeleteService;
        this.updateService = updateService;
        this.deleteService = deleteService;
    }

    public ClassPageResult search(ClassSearch search) {
        return queryService.search(search);
    }

    public ClassStats getStats() {
        return queryService.getStats();
    }

    public List<String> getGrades() {
        return queryService.getGrades();
    }

    public List<CourseOption> getCourses() {
        return queryService.getCourses();
    }

    public List<CourseOption> getCoursesForCreate() {
        return createService.getCoursesForCreate();
    }

    public void createClass(ClassCreateForm form) {
        createService.createClass(form);
    }

    public void createCourse(CourseCreateForm form) {
        courseCreateService.createCourse(form);
    }

    public CourseCreateForm getCourseFormForEdit(String courseId) {
        return courseUpdateService.getCourseFormForEdit(courseId);
    }

    public void updateCourse(String courseId, CourseCreateForm form) {
        courseUpdateService.updateCourse(courseId, form);
    }

    public void deleteCourse(String courseId) {
        courseDeleteService.deleteCourse(courseId);
    }

    public boolean courseExists(String courseId) {
        return courseUpdateService.existsCourse(courseId);
    }

    public List<SuggestionItem> suggestHomeroomTeachers(String query) {
        return createService.suggestHomeroomTeachers(query);
    }

    public List<SuggestionItem> suggestHomeroomTeachers(String query, String classId) {
        return createService.suggestHomeroomTeachers(query, classId);
    }

    public List<SuggestionItem> suggestClassCodes(String query,
                                                  String courseId,
                                                  String grade,
                                                  String excludeClassId) {
        return createService.suggestClassCodes(query, courseId, grade, excludeClassId);
    }

    public ClassCreateForm getClassFormForEdit(String classId) {
        return updateService.getClassFormForEdit(classId);
    }

    public void updateClass(String classId, ClassCreateForm form) {
        updateService.updateClass(classId, form);
    }

    public void deleteClass(String classId) {
        deleteService.deleteClass(classId);
    }

    public static class ClassRow {
        private final String idLop;
        private final String tenLop;
        private final String khoi;
        private final String khoaHoc;
        private final String gvcnTen;
        private final String gvcnEmail;
        private final String gvcnAvatar;
        private final int siSo;
        private final String namHoc;
        private final String matchedStudents;

        public ClassRow(String idLop,
                        String tenLop,
                        String khoi,
                        String khoaHoc,
                        String gvcnTen,
                        String gvcnEmail,
                        String gvcnAvatar,
                        int siSo,
                        String namHoc,
                        String matchedStudents) {
            this.idLop = idLop;
            this.tenLop = tenLop;
            this.khoi = khoi;
            this.khoaHoc = khoaHoc;
            this.gvcnTen = gvcnTen;
            this.gvcnEmail = gvcnEmail;
            this.gvcnAvatar = gvcnAvatar;
            this.siSo = siSo;
            this.namHoc = namHoc;
            this.matchedStudents = matchedStudents;
        }

        public String getIdLop() {
            return idLop;
        }

        public String getTenLop() {
            return tenLop;
        }

        public String getMaLop() {
            return idLop;
        }

        public String getTenLopHienThi() {
            if (tenLop == null || tenLop.isBlank()) {
                return idLop;
            }
            return tenLop;
        }

        public String getMaVaTenLop() {
            String code = idLop == null ? "" : idLop.trim();
            String name = getTenLopHienThi();
            if (code.isEmpty()) {
                return name;
            }
            if (name == null || name.isBlank() || name.equalsIgnoreCase(code)) {
                return code;
            }
            return code + " - " + name;
        }

        public String getKhoi() {
            return khoi;
        }

        public String getKhoaHoc() {
            return khoaHoc;
        }

        public String getGvcnTen() {
            return gvcnTen;
        }

        public String getGvcnEmail() {
            return gvcnEmail;
        }

        public String getGvcnAvatar() {
            return gvcnAvatar;
        }

        public String getGvcnAvatarUrl() {
            if (gvcnAvatar == null || gvcnAvatar.isBlank()) {
                return "";
            }
            if (gvcnAvatar.startsWith("/")) {
                return gvcnAvatar;
            }
            return "/uploads/" + gvcnAvatar;
        }

        public int getSiSo() {
            return siSo;
        }

        public String getNamHoc() {
            return namHoc;
        }

        public String getMatchedStudents() {
            return matchedStudents;
        }

        public boolean hasMatchedStudents() {
            return matchedStudents != null && !matchedStudents.isBlank();
        }

        public String getGvcnInitials() {
            if (gvcnTen == null || gvcnTen.isBlank() || "-".equals(gvcnTen.trim())) {
                return "--";
            }

            String[] words = gvcnTen.trim().split("\\s+");
            if (words.length == 1) {
                String oneWord = words[0];
                return oneWord.length() >= 2
                        ? oneWord.substring(0, 2).toUpperCase()
                        : oneWord.toUpperCase();
            }

            String first = words[words.length - 2];
            String last = words[words.length - 1];
            return (first.substring(0, 1) + last.substring(0, 1)).toUpperCase();
        }
    }

    public static class ClassPageResult {
        private final List<ClassRow> items;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final int fromRecord;
        private final int toRecord;

        public ClassPageResult(List<ClassRow> items,
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

        public List<ClassRow> getItems() {
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

    public static class ClassStats {
        private final long totalClasses;
        private final long totalStudents;
        private final long totalHomeroomTeachers;

        public ClassStats(long totalClasses,
                          long totalStudents,
                          long totalHomeroomTeachers) {
            this.totalClasses = totalClasses;
            this.totalStudents = totalStudents;
            this.totalHomeroomTeachers = totalHomeroomTeachers;
        }

        public long getTotalClasses() {
            return totalClasses;
        }

        public long getTotalStudents() {
            return totalStudents;
        }

        public long getTotalHomeroomTeachers() {
            return totalHomeroomTeachers;
        }
    }

    public static class CourseOption {
        private final String id;
        private final String name;

        public CourseOption(String id, String name) {
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
        private final String display;

        public SuggestionItem(String value, String label, String display) {
            this.value = value;
            this.label = label;
            this.display = display;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public String getDisplay() {
            return display;
        }
    }
}
