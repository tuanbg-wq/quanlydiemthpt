package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.TeacherRoleDAO;
import com.quanly.webdiem.model.entity.Subject;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.form.TeacherCreateForm;
import com.quanly.webdiem.model.entity.TeacherRole;
import com.quanly.webdiem.model.service.FileStorageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class TeacherEditService {

    private static final String EDIT_ROLE_NOTE = "Cập nhật vai trò từ màn hình chỉnh sửa giáo viên";
    private static final String ROLE_GVCN = "GVCN";
    private static final String ROLE_GVBM = "GVBM";

    private final TeacherDAO teacherDAO;
    private final TeacherRoleDAO teacherRoleDAO;
    private final SubjectDAO subjectDAO;
    private final FileStorageService fileStorageService;

    public TeacherEditService(TeacherDAO teacherDAO,
                              TeacherRoleDAO teacherRoleDAO,
                              SubjectDAO subjectDAO,
                              FileStorageService fileStorageService) {
        this.teacherDAO = teacherDAO;
        this.teacherRoleDAO = teacherRoleDAO;
        this.subjectDAO = subjectDAO;
        this.fileStorageService = fileStorageService;
    }

    public TeacherCreateForm getEditForm(String teacherId) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        TeacherCreateForm form = new TeacherCreateForm();

        form.setIdGiaoVien(teacher.getIdGiaoVien());
        form.setHoTen(teacher.getHoTen());
        form.setNgaySinh(teacher.getNgaySinh());
        form.setGioiTinh(mapGenderForForm(teacher.getGioiTinh()));
        form.setSoDienThoai(teacher.getSoDienThoai());
        form.setEmail(teacher.getEmail());
        form.setDiaChi(teacher.getDiaChi());
        form.setMonHocId(resolveSubjectId(teacher.getChuyenMon()));
        form.setTrinhDo(mapDegreeCode(teacher.getTrinhDo()));
        form.setNgayBatDauCongTac(teacher.getNgayVaoLam());
        String currentStatus = normalize(teacher.getTrangThai());
        form.setTrangThai(currentStatus);
        form.setGhiChu(teacher.getGhiChu());

        applyRoleInformation(teacher.getIdGiaoVien(), form);
        applyFallbackSchoolYear(teacher.getIdGiaoVien(), form);
        applySubjectClassInformation(teacher.getIdGiaoVien(), form);
        applyHomeroomClassInformation(teacher.getIdGiaoVien(), form);
        applyRoleFallbackFromClassData(form);

        if (isBlank(form.getTrangThai())) {
            form.setTrangThai("dang_lam");
        }

        return form;
    }

    public String getCurrentAvatarPath(String teacherId) {
        Teacher teacher = findTeacherOrThrow(teacherId);
        return normalize(teacher.getAnh());
    }

    @Transactional
    public void updateTeacher(String teacherId, TeacherCreateForm form) {
        String currentTeacherId = normalizeTeacherId(teacherId);
        String requestedTeacherId = normalizeTeacherId(form == null ? null : form.getIdGiaoVien());
        validateTargetTeacherId(currentTeacherId, requestedTeacherId);

        Teacher teacher = findTeacherOrThrow(currentTeacherId);

        Subject subject = subjectDAO.findById(form.getMonHocId())
                .orElseThrow(() -> new RuntimeException("Môn dạy không tồn tại."));

        teacher.setHoTen(normalizeFullName(form.getHoTen()));
        teacher.setNgaySinh(form.getNgaySinh());
        teacher.setGioiTinh(mapGenderForDatabase(form.getGioiTinh()));
        teacher.setSoDienThoai(normalize(form.getSoDienThoai()));
        teacher.setEmail(normalizeLower(form.getEmail()));
        teacher.setDiaChi(normalize(form.getDiaChi()));
        teacher.setChuyenMon(subject.getTenMonHoc());
        teacher.setTrinhDo(mapDegreeLabel(form.getTrinhDo()));
        teacher.setNgayVaoLam(form.getNgayBatDauCongTac());
        String updatedStatus = normalize(form.getTrangThai());
        teacher.setTrangThai(updatedStatus);
        teacher.setGhiChu(normalize(form.getGhiChu()));

        MultipartFile avatar = form.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            String avatarPath = fileStorageService.saveTeacherAvatar(requestedTeacherId, avatar);
            teacher.setAnh(avatarPath);
        }

        teacherDAO.save(teacher);

        if (normalizeSelectedRoleCode(form.getVaiTroMa()) != null) {
            upsertTeacherRole(currentTeacherId, form);
        }

        if (!currentTeacherId.equalsIgnoreCase(requestedTeacherId)) {
            renameTeacherWithReferences(currentTeacherId, requestedTeacherId, updatedStatus);
        }

        assignPrimaryTeacherForSubject(subject.getIdMonHoc(), requestedTeacherId);
        syncTeachingAssignments(requestedTeacherId, form, updatedStatus);
    }

    private void validateTargetTeacherId(String currentTeacherId, String requestedTeacherId) {
        if (requestedTeacherId == null) {
            throw new RuntimeException("Mã giáo viên không hợp lệ.");
        }

        if (requestedTeacherId.equalsIgnoreCase(currentTeacherId)) {
            return;
        }

        if (teacherDAO.existsById(requestedTeacherId)) {
            throw new RuntimeException("Mã giáo viên đã tồn tại.");
        }

        if (subjectDAO.existsById(requestedTeacherId)) {
            throw new RuntimeException("Mã giáo viên không được trùng mã môn học.");
        }
    }

    private void renameTeacherWithReferences(String oldTeacherId, String newTeacherId, String finalStatus) {
        String temporaryTeacherId = buildTemporaryTeacherId(oldTeacherId, newTeacherId);
        boolean requiresStatusRestore = !isWorkingStatus(finalStatus);

        try {
            if (requiresStatusRestore) {
                teacherDAO.updateTeacherStatusById(oldTeacherId, "dang_lam");
            }

            int created = teacherDAO.createTemporaryTeacherForRename(oldTeacherId, temporaryTeacherId);
            if (created != 1) {
                throw new RuntimeException("Không thể chuẩn bị dữ liệu để đổi mã giáo viên.");
            }

            reassignTeacherReferences(oldTeacherId, temporaryTeacherId);

            int updated = teacherDAO.renameTeacherId(oldTeacherId, newTeacherId);
            if (updated != 1) {
                throw new RuntimeException("Không thể cập nhật mã giáo viên.");
            }

            reassignTeacherReferences(temporaryTeacherId, newTeacherId);

            int removed = teacherDAO.deleteByTeacherIdIgnoreCase(temporaryTeacherId);
            if (removed != 1) {
                throw new RuntimeException("Không thể hoàn tất đổi mã giáo viên.");
            }

            if (requiresStatusRestore) {
                teacherDAO.updateTeacherStatusById(newTeacherId, finalStatus);
            }
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể đổi mã giáo viên do có dữ liệu liên quan.");
        } catch (RuntimeException ex) {
            throw new RuntimeException("Không thể đổi mã giáo viên do có dữ liệu liên quan.");
        }
    }

    private void reassignTeacherReferences(String sourceTeacherId, String targetTeacherId) {
        teacherDAO.reassignTeacherIdInClasses(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInTeachingAssignments(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInSubjects(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInScores(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInConducts(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInTeacherRoles(sourceTeacherId, targetTeacherId);
    }

    private String buildTemporaryTeacherId(String oldTeacherId, String newTeacherId) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = "TMP" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 7)
                    .toUpperCase(Locale.ROOT);

            if (candidate.equalsIgnoreCase(oldTeacherId) || candidate.equalsIgnoreCase(newTeacherId)) {
                continue;
            }

            if (!teacherDAO.existsById(candidate) && !subjectDAO.existsById(candidate)) {
                return candidate;
            }
        }

        throw new RuntimeException("Không thể tạo mã tạm để đổi mã giáo viên.");
    }

    private String normalizeTeacherId(String teacherId) {
        String normalized = normalize(teacherId);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
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

    private void applyRoleInformation(String teacherId, TeacherCreateForm form) {
        List<TeacherRole> teacherRoles = teacherRoleDAO.findByIdGiaoVienOrderByNamHocDescIdDesc(teacherId);
        if (teacherRoles.isEmpty()) {
            return;
        }

        TeacherRole latestRole = teacherRoles.get(0);
        if (!isBlank(latestRole.getNamHoc())) {
            form.setNamHoc(latestRole.getNamHoc());
        }

        String roleCode = resolveRoleCode(latestRole.getIdLoaiVaiTro());
        if (!isBlank(roleCode)) {
            form.setVaiTroMa(List.of(roleCode));
        }
    }

    private void applyFallbackSchoolYear(String teacherId, TeacherCreateForm form) {
        if (form == null || !isBlank(form.getNamHoc())) {
            return;
        }

        String schoolYear = null;
        String subjectId = normalize(form.getMonHocId());
        if (!isBlank(subjectId)) {
            schoolYear = normalize(teacherDAO.findLatestSchoolYearByTeacherAndSubject(teacherId, subjectId));
        }

        if (isBlank(schoolYear)) {
            schoolYear = normalize(teacherDAO.findLatestHomeroomSchoolYearByTeacher(teacherId));
        }

        if (!isBlank(schoolYear)) {
            form.setNamHoc(schoolYear);
        }
    }

    private void applySubjectClassInformation(String teacherId, TeacherCreateForm form) {
        if (form == null) {
            return;
        }
        String subjectId = normalize(form.getMonHocId());
        String schoolYear = normalize(form.getNamHoc());
        if (isBlank(subjectId)) {
            return;
        }

        List<String> classIds = findAssignedClassIdsByTeacherSubjectAndYear(teacherId, subjectId, schoolYear);
        if (classIds.isEmpty()) {
            String fallbackSchoolYear = normalize(teacherDAO.findLatestSchoolYearByTeacherAndSubject(teacherId, subjectId));
            if (!isBlank(fallbackSchoolYear) && !fallbackSchoolYear.equalsIgnoreCase(schoolYear)) {
                classIds = findAssignedClassIdsByTeacherSubjectAndYear(teacherId, subjectId, fallbackSchoolYear);
                if (!classIds.isEmpty()) {
                    if (isBlank(form.getNamHoc())) {
                        form.setNamHoc(fallbackSchoolYear);
                    }
                }
            }
        }

        if (!classIds.isEmpty()) {
            form.setLopBoMon(String.join(", ", classIds));
        }
    }

    private void applyHomeroomClassInformation(String teacherId, TeacherCreateForm form) {
        if (form == null) {
            return;
        }
        String schoolYear = normalize(form.getNamHoc());
        String homeroomClassLabel = normalize(teacherDAO.findHomeroomClassDisplayByTeacherAndYear(teacherId, schoolYear));
        if (isBlank(homeroomClassLabel)) {
            String fallbackSchoolYear = normalize(teacherDAO.findLatestHomeroomSchoolYearByTeacher(teacherId));
            if (!isBlank(fallbackSchoolYear) && !fallbackSchoolYear.equalsIgnoreCase(schoolYear)) {
                homeroomClassLabel = normalize(teacherDAO.findHomeroomClassDisplayByTeacherAndYear(teacherId, fallbackSchoolYear));
                if (!isBlank(homeroomClassLabel) && isBlank(form.getLopBoMon()) && isBlank(form.getNamHoc())) {
                    form.setNamHoc(fallbackSchoolYear);
                }
            }
        }

        if (!isBlank(homeroomClassLabel)) {
            form.setLopChuNhiem(homeroomClassLabel);
        }
    }

    private List<String> findAssignedClassIdsByTeacherSubjectAndYear(String teacherId, String subjectId, String schoolYear) {
        if (isBlank(teacherId) || isBlank(subjectId) || isBlank(schoolYear)) {
            return List.of();
        }

        return teacherDAO.findAssignedClassDisplaysForTeacherSubjectAndYear(teacherId, subjectId, schoolYear).stream()
                .map(this::normalize)
                .filter(value -> value != null)
                .toList();
    }

    private void applyRoleFallbackFromClassData(TeacherCreateForm form) {
        if (form == null) {
            return;
        }

        if (!isBlank(form.getLopChuNhiem())) {
            form.setVaiTroMa(List.of(ROLE_GVCN));
            return;
        }

        if ((form.getVaiTroMa() == null || form.getVaiTroMa().isEmpty()) && !isBlank(form.getLopBoMon())) {
            form.setVaiTroMa(List.of(ROLE_GVBM));
        }
    }

    private String resolveRoleCode(Integer roleTypeId) {
        if (roleTypeId == null) {
            return null;
        }

        return buildRoleTypeCodeMap().get(roleTypeId);
    }

    private Map<Integer, String> buildRoleTypeCodeMap() {
        Map<Integer, String> mapping = new LinkedHashMap<>();
        for (Object[] row : teacherRoleDAO.findRoleTypesForCreateForm()) {
            Integer id = asInt(row, 0);
            String code = normalize(asString(row, 1));
            if (id != null && code != null) {
                mapping.putIfAbsent(id, code.toUpperCase(Locale.ROOT));
            }
        }
        return mapping;
    }

    private void upsertTeacherRole(String teacherId, TeacherCreateForm form) {
        String schoolYear = normalize(form.getNamHoc());
        if (schoolYear == null) {
            throw new RuntimeException("Năm học áp dụng vai trò là bắt buộc.");
        }

        String roleCode = normalizeSelectedRoleCode(form.getVaiTroMa());
        if (roleCode == null) {
            throw new RuntimeException("Vui lòng chọn 1 vai trò giáo viên.");
        }

        Integer roleTypeId = resolveRoleTypeId(roleCode);
        if (roleTypeId == null) {
            throw new RuntimeException("Vai trò giáo viên không hợp lệ.");
        }

        try {
            teacherRoleDAO.deleteByTeacherId(teacherId);

            TeacherRole teacherRole = new TeacherRole();
            teacherRole.setIdGiaoVien(teacherId);
            teacherRole.setNamHoc(schoolYear);
            teacherRole.setIdLoaiVaiTro(roleTypeId);
            teacherRole.setGhiChu(EDIT_ROLE_NOTE);
            teacherRoleDAO.save(teacherRole);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể cập nhật vai trò giáo viên do dữ liệu vai trò trong năm học bị trùng. Vui lòng thử lưu lại.");
        }
    }

    private Integer resolveRoleTypeId(String roleCode) {
        List<Object[]> rows = teacherRoleDAO.findRoleTypesByCodes(List.of(roleCode));
        if (rows.isEmpty()) {
            return null;
        }
        return asInt(rows.get(0), 0);
    }

    private String normalizeSelectedRoleCode(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return null;
        }

        return roleCodes.stream()
                .map(this::normalize)
                .filter(value -> value != null)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .findFirst()
                .orElse(null);
    }

    private void syncTeachingAssignments(String teacherId, TeacherCreateForm form, String status) {
        if (!isWorkingStatus(status) || form == null) {
            return;
        }

        String subjectId = normalize(form.getMonHocId());
        String schoolYear = normalize(form.getNamHoc());
        List<String> classIds = TeacherClassDisplaySupport.parseClassIds(form.getLopBoMon());
        String roleCode = normalizeSelectedRoleCode(form.getVaiTroMa());
        String homeroomClassId = TeacherClassDisplaySupport.extractClassId(form.getLopChuNhiem());

        if (isBlank(subjectId) || isBlank(schoolYear) || isBlank(roleCode)) {
            return;
        }

        teacherDAO.deleteTeachingAssignmentsByTeacherSubjectAndYear(teacherId, subjectId, schoolYear);
        for (String classId : classIds) {
            teacherDAO.ensureTeachingAssignmentForTeacherSubjectClassSemester(teacherId, subjectId, classId, schoolYear, 1);
            teacherDAO.ensureTeachingAssignmentForTeacherSubjectClassSemester(teacherId, subjectId, classId, schoolYear, 2);
        }

        teacherDAO.clearHomeroomClassByTeacherId(teacherId);
        if (ROLE_GVCN.equalsIgnoreCase(roleCode) && !isBlank(homeroomClassId)) {
            String assignedTeacherId = normalize(teacherDAO.findHomeroomTeacherIdByClassId(homeroomClassId));
            if (!isBlank(assignedTeacherId) && !assignedTeacherId.equalsIgnoreCase(teacherId)) {
                throw new RuntimeException("Lớp này đã có GVCN. Vui lòng chọn lớp khác.");
            }
            int assigned = teacherDAO.assignHomeroomTeacherToClass(homeroomClassId, teacherId);
            if (assigned <= 0) {
                throw new RuntimeException("Không thể gán GVCN cho lớp đã chọn.");
            }
        }
    }

    private String resolveSubjectId(String chuyenMon) {
        String normalized = normalize(chuyenMon);
        if (normalized == null) {
            return null;
        }
        String normalizedText = normalizeText(normalized);

        for (Subject subject : subjectDAO.findAll()) {
            String subjectId = normalize(subject.getIdMonHoc());
            String subjectName = normalize(subject.getTenMonHoc());
            String subjectIdText = normalizeText(subjectId);
            String subjectNameText = normalizeText(subjectName);

            if (normalized.equalsIgnoreCase(subjectId)
                    || normalized.equalsIgnoreCase(subjectName)
                    || (!isBlank(normalizedText) && normalizedText.equals(subjectIdText))
                    || (!isBlank(normalizedText) && normalizedText.equals(subjectNameText))) {
                return subject.getIdMonHoc();
            }
        }

        return null;
    }

    private String mapGenderForForm(String gender) {
        String normalized = normalize(gender);
        if (normalized == null) {
            return "Khac";
        }

        if ("nam".equalsIgnoreCase(normalized)) {
            return "Nam";
        }

        if ("nu".equalsIgnoreCase(normalized)) {
            return "Nu";
        }

        return "Khac";
    }

    private String mapGenderForDatabase(String gender) {
        String normalized = normalize(gender);
        if ("Nam".equalsIgnoreCase(normalized)) {
            return "Nam";
        }

        if ("Nu".equalsIgnoreCase(normalized)) {
            return "Nu";
        }

        return null;
    }

    private String mapDegreeCode(String degreeLabel) {
        String normalized = normalizeText(degreeLabel);
        if (normalized == null) {
            return "KHAC";
        }

        if (normalized.contains("cu") && normalized.contains("nhan")) {
            return "CU_NHAN";
        }

        if (normalized.contains("thac") && normalized.contains("si")) {
            return "THAC_SI";
        }

        if (normalized.contains("tien") && normalized.contains("si")) {
            return "TIEN_SI";
        }

        return "KHAC";
    }

    private String mapDegreeLabel(String degreeCode) {
        String normalized = normalize(degreeCode);
        if ("CU_NHAN".equalsIgnoreCase(normalized)) {
            return "Cử nhân";
        }

        if ("THAC_SI".equalsIgnoreCase(normalized)) {
            return "Thạc sĩ";
        }

        if ("TIEN_SI".equalsIgnoreCase(normalized)) {
            return "Tiến sĩ";
        }

        return "Khác";
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

    private String normalizeLower(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }

        return Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isWorkingStatus(String status) {
        return "dang_lam".equalsIgnoreCase(normalize(status));
    }

    private Teacher findTeacherOrThrow(String teacherId) {
        String normalizedId = normalize(teacherId);
        if (normalizedId == null) {
            throw new RuntimeException("Mã giáo viên không hợp lệ.");
        }

        return teacherDAO.findById(normalizedId.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên."));
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

        return row[index].toString();
    }
}
