package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.TeacherRoleDAO;
import com.quanly.webdiem.model.entity.Subject;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.TeacherCreateForm;
import com.quanly.webdiem.model.entity.TeacherRole;
import com.quanly.webdiem.model.service.FileStorageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TeacherCreateService {

    private static final Pattern TEACHER_ID_PATTERN = Pattern.compile("(?i)^GV(\\d+)$");
    private static final Pattern GRADE_PATTERN = Pattern.compile("(10|11|12)");

    private final TeacherDAO teacherDAO;
    private final TeacherRoleDAO teacherRoleDAO;
    private final SubjectDAO subjectDAO;
    private final FileStorageService fileStorageService;

    public TeacherCreateService(TeacherDAO teacherDAO,
                                TeacherRoleDAO teacherRoleDAO,
                                SubjectDAO subjectDAO,
                                FileStorageService fileStorageService) {
        this.teacherDAO = teacherDAO;
        this.teacherRoleDAO = teacherRoleDAO;
        this.subjectDAO = subjectDAO;
        this.fileStorageService = fileStorageService;
    }

    public void applyDefaultValues(TeacherCreateForm form) {
        if (form == null) {
            return;
        }

        if (isBlank(form.getTrangThai())) {
            form.setTrangThai("dang_lam");
        }

        if (isBlank(form.getNamHoc())) {
            form.setNamHoc(resolveCurrentSchoolYear());
        }
    }

    public List<Subject> getSubjectsForForm() {
        try {
            return subjectDAO.findAll().stream()
                    .sorted(Comparator.comparing(Subject::getTenMonHoc, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (RuntimeException ex) {
            return List.of();
        }
    }

    public List<OptionItem> getTeacherRoleTypes() {
        try {
            List<OptionItem> dbOptions = teacherRoleDAO.findRoleTypesForCreateForm().stream()
                    .map(row -> new OptionItem(asString(row, 1), asString(row, 2)))
                    .filter(item -> item.getValue() != null && item.getLabel() != null)
                    .toList();

            if (!dbOptions.isEmpty()) {
                return dbOptions;
            }
        } catch (RuntimeException ignored) {
            // Fall through to default options.
        }

        return List.of(
                new OptionItem("GVCN", "Giáo viên chủ nhiệm"),
                new OptionItem("GVBM", "Giáo viên bộ môn")
        );
    }

    public List<OptionItem> getGenderOptions() {
        return List.of(
                new OptionItem("Nam", "Nam"),
                new OptionItem("Nu", "Nữ"),
                new OptionItem("Khac", "Khác")
        );
    }

    public List<OptionItem> getDegreeOptions() {
        return List.of(
                new OptionItem("CU_NHAN", "Cử nhân"),
                new OptionItem("THAC_SI", "Thạc sĩ"),
                new OptionItem("TIEN_SI", "Tiến sĩ"),
                new OptionItem("KHAC", "Khác")
        );
    }

    public List<OptionItem> getStatusOptions() {
        return List.of(
                new OptionItem("dang_lam", "Đang công tác"),
                new OptionItem("nghi_viec", "Đã nghỉ")
        );
    }

    public List<ClassSuggestionItem> suggestSubjectClasses(String query, String schoolYear, String subjectId) {
        String normalizedSubjectId = normalize(subjectId);
        Set<String> subjectGradeScope = resolveSubjectGradeScope(normalizedSubjectId);

        return teacherDAO.suggestSubjectClassesForTeacherForm(normalize(query), normalize(schoolYear)).stream()
                .map(this::mapClassSuggestion)
                .filter(item -> item.id() != null)
                .filter(item -> matchesSubjectGradeScope(item.grade(), subjectGradeScope))
                .toList();
    }

    public List<ClassSuggestionItem> suggestHomeroomClasses(String query, String schoolYear, boolean includeAssigned) {
        List<Object[]> rows = includeAssigned
                ? teacherDAO.suggestHomeroomClassesForEdit(normalize(query), normalize(schoolYear))
                : teacherDAO.suggestAvailableHomeroomClassesForCreate(normalize(query), normalize(schoolYear));

        return rows.stream()
                .map(this::mapClassSuggestion)
                .filter(item -> item.id() != null)
                .toList();
    }

    public String suggestNextTeacherId() {
        SuggestedTeacherCode latestSuggestedCode = suggestFromLatestCreatedTeacher();
        if (latestSuggestedCode != null) {
            return formatTeacherCode(latestSuggestedCode.nextNumber(), latestSuggestedCode.minWidth());
        }

        Integer maxCode;
        try {
            maxCode = teacherDAO.findMaxTeacherCodeNumber();
        } catch (RuntimeException ex) {
            maxCode = null;
        }
        int next = maxCode == null ? 1 : maxCode + 1;
        return formatTeacherCode(next, 3);
    }

    @Transactional
    public void createTeacher(TeacherCreateForm form) {
        Subject subject = findSubjectOrThrow(form.getMonHocId());
        Teacher teacher = mapToTeacher(form, subject);
        teacherDAO.save(teacher);
        assignPrimaryTeacherForSubject(subject.getIdMonHoc(), teacher.getIdGiaoVien());

        List<String> selectedRoleCodes = normalizeRoleCodes(form.getVaiTroMa());
        if (selectedRoleCodes.size() != 1) {
            throw new RuntimeException("Chỉ được chọn 1 vai trò giáo viên.");
        }

        List<RoleTypeItem> roleTypes = findRoleTypesByCodes(selectedRoleCodes);
        if (roleTypes.size() != 1) {
            throw new RuntimeException("Vai trò giáo viên không hợp lệ.");
        }
        String selectedRoleCode = selectedRoleCodes.get(0);

        List<TeacherRole> teacherRoles = roleTypes.stream()
                .map(roleType -> buildTeacherRole(teacher.getIdGiaoVien(), roleType, form.getNamHoc()))
                .toList();

        try {
            teacherRoleDAO.saveAll(teacherRoles);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể lưu vai trò giáo viên. Vui lòng kiểm tra lại trạng thái và năm học.");
        }

        syncTeachingAssignmentsForSubjectClasses(
                teacher.getIdGiaoVien(),
                subject.getIdMonHoc(),
                normalize(form.getNamHoc()),
                parseClassIds(form.getLopBoMon())
        );
        syncHomeroomAssignment(
                teacher.getIdGiaoVien(),
                selectedRoleCode,
                normalize(form.getLopChuNhiem())
        );
    }

    private SuggestedTeacherCode suggestFromLatestCreatedTeacher() {
        String latestTeacherCode;
        try {
            latestTeacherCode = teacherDAO.findLatestCreatedTeacherCode();
        } catch (RuntimeException ex) {
            latestTeacherCode = null;
        }

        String normalizedCode = normalize(latestTeacherCode);
        if (normalizedCode == null) {
            return null;
        }

        Matcher matcher = TEACHER_ID_PATTERN.matcher(normalizedCode);
        if (!matcher.matches()) {
            return null;
        }

        String numberPart = matcher.group(1);
        try {
            int currentNumber = Integer.parseInt(numberPart);
            int minWidth = Math.max(3, numberPart.length());
            return new SuggestedTeacherCode(currentNumber + 1, minWidth);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<RoleTypeItem> findRoleTypesByCodes(List<String> roleCodes) {
        List<String> normalizedCodes = normalizeRoleCodes(roleCodes);
        if (normalizedCodes.isEmpty()) {
            return List.of();
        }

        try {
            return teacherRoleDAO.findRoleTypesByCodes(normalizedCodes).stream()
                    .map(this::mapRoleType)
                    .filter(item -> item.getId() != null)
                    .toList();
        } catch (RuntimeException ex) {
            throw new RuntimeException(
                    "Chưa cấu hình bảng teacher_role_types hoặc dữ liệu vai trò giáo viên. Vui lòng chạy migration role."
            );
        }
    }

    private List<String> normalizeRoleCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }

        return roleCodes.stream()
                .filter(code -> code != null && !code.trim().isEmpty())
                .map(code -> code.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.toList());
    }

    private Teacher mapToTeacher(TeacherCreateForm form, Subject subject) {
        Teacher teacher = new Teacher();

        String teacherId = normalize(form.getIdGiaoVien()).toUpperCase(Locale.ROOT);
        String fullName = normalizeFullName(form.getHoTen());
        String phone = normalize(form.getSoDienThoai());
        String email = normalize(form.getEmail()).toLowerCase(Locale.ROOT);
        String address = normalize(form.getDiaChi());
        String note = normalize(form.getGhiChu());


        teacher.setIdGiaoVien(teacherId);
        teacher.setHoTen(fullName);
        teacher.setNgaySinh(form.getNgaySinh());
        teacher.setGioiTinh(mapGenderForDatabase(form.getGioiTinh()));
        teacher.setSoDienThoai(phone);
        teacher.setEmail(email);
        teacher.setDiaChi(address);
        teacher.setChuyenMon(subject.getTenMonHoc());
        teacher.setTrinhDo(mapDegreeLabel(form.getTrinhDo()));
        teacher.setNgayVaoLam(form.getNgayBatDauCongTac());
        teacher.setTrangThai(form.getTrangThai());
        teacher.setGhiChu(note);

        String avatarPath = fileStorageService.saveTeacherAvatar(teacherId, form.getAvatar());
        teacher.setAnh(avatarPath);

        return teacher;
    }

    private Subject findSubjectOrThrow(String subjectId) {
        String normalizedId = normalize(subjectId);
        if (normalizedId == null) {
            throw new RuntimeException("Môn dạy không tồn tại.");
        }

        return subjectDAO.findById(normalizedId)
                .orElseThrow(() -> new RuntimeException("Môn dạy không tồn tại."));
    }

    private void assignPrimaryTeacherForSubject(String subjectId, String teacherId) {
        if (isBlank(subjectId) || isBlank(teacherId)) {
            return;
        }

        int updated = subjectDAO.assignPrimaryTeacher(subjectId, teacherId);
        if (updated <= 0) {
            throw new RuntimeException("Không thể cập nhật giáo viên phụ trách cho môn học.");
        }
    }

    private String formatTeacherCode(int number, int minWidth) {
        int width = Math.max(3, minWidth);
        String format = "GV%0" + width + "d";
        return String.format(format, Math.max(1, number));
    }

    private void syncTeachingAssignmentsForSubjectClasses(String teacherId,
                                                          String subjectId,
                                                          String schoolYear,
                                                          List<String> classIds) {
        if (isBlank(teacherId) || isBlank(subjectId) || isBlank(schoolYear)) {
            return;
        }

        teacherDAO.deleteTeachingAssignmentsByTeacherSubjectAndYear(teacherId, subjectId, schoolYear);
        if (classIds == null || classIds.isEmpty()) {
            return;
        }

        for (String classId : classIds) {
            teacherDAO.ensureTeachingAssignmentForTeacherSubjectClassSemester(teacherId, subjectId, classId, schoolYear, 1);
            teacherDAO.ensureTeachingAssignmentForTeacherSubjectClassSemester(teacherId, subjectId, classId, schoolYear, 2);
        }
    }

    private void syncHomeroomAssignment(String teacherId, String roleCode, String homeroomClassId) {
        if (isBlank(teacherId)) {
            return;
        }

        teacherDAO.clearHomeroomClassByTeacherId(teacherId);
        if (!"GVCN".equalsIgnoreCase(roleCode) || isBlank(homeroomClassId)) {
            return;
        }

        teacherDAO.assignHomeroomTeacherToClass(homeroomClassId, teacherId);
    }

    private List<String> parseClassIds(String raw) {
        String normalizedRaw = normalize(raw);
        if (normalizedRaw == null) {
            return List.of();
        }

        String[] tokens = normalizedRaw.split("[,;\\n]+");
        LinkedHashSet<String> uniqueClassIds = new LinkedHashSet<>();
        for (String token : tokens) {
            String normalizedToken = normalize(token);
            if (normalizedToken == null) {
                continue;
            }
            uniqueClassIds.add(normalizedToken.toUpperCase(Locale.ROOT));
        }

        return new ArrayList<>(uniqueClassIds);
    }

    private TeacherRole buildTeacherRole(String teacherId, RoleTypeItem roleType, String schoolYear) {
        TeacherRole role = new TeacherRole();
        role.setIdGiaoVien(teacherId);
        role.setIdLoaiVaiTro(roleType.getId());
        role.setNamHoc(schoolYear);
        role.setGhiChu("Gán vai trò khi thêm giáo viên mới");
        return role;
    }

    private String resolveCurrentSchoolYear() {
        List<String> schoolYears = subjectDAO.findSchoolYears();
        if (!schoolYears.isEmpty()) {
            return schoolYears.get(0);
        }

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        if (today.getMonthValue() >= 8) {
            return year + "-" + (year + 1);
        }
        return (year - 1) + "-" + year;
    }

    private String mapGenderForDatabase(String gender) {
        if ("Nam".equalsIgnoreCase(gender)) {
            return "Nam";
        }
        if ("Nu".equalsIgnoreCase(gender)) {
            return "Nu";
        }
        return null;
    }

    private String mapDegreeLabel(String degreeCode) {
        if ("CU_NHAN".equalsIgnoreCase(degreeCode)) {
            return "Cử nhân";
        }
        if ("THAC_SI".equalsIgnoreCase(degreeCode)) {
            return "Thạc sĩ";
        }
        if ("TIEN_SI".equalsIgnoreCase(degreeCode)) {
            return "Tiến sĩ";
        }
        return "Khác";
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeFullName(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }

        String[] words = normalized.split("\\s+");
        StringBuilder output = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!output.isEmpty()) {
                output.append(' ');
            }

            output.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
            if (word.length() > 1) {
                output.append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }

        return output.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private RoleTypeItem mapRoleType(Object[] row) {
        return new RoleTypeItem(
                asInt(row, 0),
                asString(row, 1),
                asString(row, 2)
        );
    }

    private Integer asInt(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }
        try {
            return Integer.parseInt(row[index].toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String asString(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }
        String value = row[index].toString().trim();
        return value.isEmpty() ? null : value;
    }

    private ClassSuggestionItem mapClassSuggestion(Object[] row) {
        String classId = asString(row, 0);
        String className = asString(row, 1);
        String grade = asString(row, 2);
        String schoolYear = asString(row, 3);
        if (classId == null) {
            return new ClassSuggestionItem(null, null, null, null);
        }
        return new ClassSuggestionItem(classId, defaultIfBlank(className, classId), grade, schoolYear);
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private Set<String> resolveSubjectGradeScope(String subjectId) {
        if (isBlank(subjectId)) {
            return Set.of();
        }
        Subject subject = subjectDAO.findById(subjectId).orElse(null);
        if (subject == null) {
            return Set.of();
        }
        return parseGradeScope(subject.getKhoiApDung());
    }

    private Set<String> parseGradeScope(String rawGradeScope) {
        String normalized = normalize(rawGradeScope);
        if (normalized == null) {
            return Set.of();
        }

        LinkedHashSet<String> gradeTokens = new LinkedHashSet<>();
        Matcher matcher = GRADE_PATTERN.matcher(normalized);
        while (matcher.find()) {
            gradeTokens.add(matcher.group(1));
        }

        if (!gradeTokens.isEmpty()) {
            return gradeTokens;
        }

        for (String token : normalized.split(",")) {
            String grade = normalize(token);
            if (grade != null) {
                gradeTokens.add(grade);
            }
        }
        return gradeTokens;
    }

    private boolean matchesSubjectGradeScope(String classGrade, Set<String> subjectGradeScope) {
        if (subjectGradeScope == null || subjectGradeScope.isEmpty()) {
            return true;
        }
        String normalizedClassGrade = normalize(classGrade);
        return normalizedClassGrade != null && subjectGradeScope.contains(normalizedClassGrade);
    }

    private record SuggestedTeacherCode(int nextNumber, int minWidth) {
    }

    public static class OptionItem {
        private final String value;
        private final String label;

        public OptionItem(String value, String label) {
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

    public record ClassSuggestionItem(String id, String name, String grade, String schoolYear) {
    }

    private static class RoleTypeItem {
        private final Integer id;
        private final String maVaiTro;
        private final String tenVaiTro;

        private RoleTypeItem(Integer id, String maVaiTro, String tenVaiTro) {
            this.id = id;
            this.maVaiTro = maVaiTro;
            this.tenVaiTro = tenVaiTro;
        }

        public Integer getId() {
            return id;
        }

        public String getMaVaiTro() {
            return maVaiTro;
        }

        public String getTenVaiTro() {
            return tenVaiTro;
        }
    }
}
