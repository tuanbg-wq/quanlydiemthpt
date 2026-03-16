package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.TeacherRoleDAO;
import com.quanly.webdiem.model.entity.Subject;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.TeacherCreateForm;
import com.quanly.webdiem.model.entity.TeacherRole;
import org.springframework.dao.DataIntegrityViolationException;
import com.quanly.webdiem.model.service.FileStorageService;
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
        if (!isWorkingStatus(currentStatus)) {
            form.setVaiTroMa(List.of());
        }

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

        if (isWorkingStatus(updatedStatus)) {
            upsertTeacherRole(currentTeacherId, form);
        }

        if (!currentTeacherId.equalsIgnoreCase(requestedTeacherId)) {
            renameTeacherWithReferences(currentTeacherId, requestedTeacherId, updatedStatus);
        }

        assignPrimaryTeacherForSubject(subject.getIdMonHoc(), requestedTeacherId);
    }

    private void validateTargetTeacherId(String currentTeacherId, String requestedTeacherId) {
        if (requestedTeacherId == null) {
            throw new RuntimeException("MĂ£ giĂ¡o viĂªn khĂ´ng há»£p lá»‡.");
        }

        if (requestedTeacherId.equalsIgnoreCase(currentTeacherId)) {
            return;
        }

        if (teacherDAO.existsById(requestedTeacherId)) {
            throw new RuntimeException("MĂ£ giĂ¡o viĂªn Ä‘Ă£ tá»“n táº¡i.");
        }

        if (subjectDAO.existsById(requestedTeacherId)) {
            throw new RuntimeException("MĂ£ giĂ¡o viĂªn khĂ´ng Ä‘Æ°á»£c trĂ¹ng mĂ£ mĂ´n há»c.");
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
                throw new RuntimeException("KhĂ´ng thá»ƒ chuáº©n bá»‹ dá»¯ liá»‡u Ä‘á»ƒ Ä‘á»•i mĂ£ giĂ¡o viĂªn.");
            }

            reassignTeacherReferences(oldTeacherId, temporaryTeacherId);

            int updated = teacherDAO.renameTeacherId(oldTeacherId, newTeacherId);
            if (updated != 1) {
                throw new RuntimeException("KhĂ´ng thá»ƒ cáº­p nháº­t mĂ£ giĂ¡o viĂªn.");
            }

            reassignTeacherReferences(temporaryTeacherId, newTeacherId);

            int removed = teacherDAO.deleteByTeacherIdIgnoreCase(temporaryTeacherId);
            if (removed != 1) {
                throw new RuntimeException("KhĂ´ng thá»ƒ hoĂ n táº¥t Ä‘á»•i mĂ£ giĂ¡o viĂªn.");
            }

            if (requiresStatusRestore) {
                teacherDAO.updateTeacherStatusById(newTeacherId, finalStatus);
            }
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("KhĂ´ng thá»ƒ Ä‘á»•i mĂ£ giĂ¡o viĂªn do cĂ³ dá»¯ liá»‡u liĂªn quan.");
        } catch (RuntimeException ex) {
            throw new RuntimeException("KhĂ´ng thá»ƒ Ä‘á»•i mĂ£ giĂ¡o viĂªn do cĂ³ dá»¯ liá»‡u liĂªn quan.");
        }
    }

    private void reassignTeacherReferences(String sourceTeacherId, String targetTeacherId) {
        teacherDAO.reassignTeacherIdInClasses(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInTeachingAssignments(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInSubjects(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInScores(sourceTeacherId, targetTeacherId);
        teacherDAO.reassignTeacherIdInConducts(sourceTeacherId, targetTeacherId);
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

        throw new RuntimeException("KhĂ´ng thá»ƒ táº¡o mĂ£ táº¡m Ä‘á»ƒ Ä‘á»•i mĂ£ giĂ¡o viĂªn.");
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
            throw new RuntimeException("KhĂ´ng thá»ƒ cáº­p nháº­t giĂ¡o viĂªn phá»¥ trĂ¡ch cho mĂ´n há»c.");
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

        List<TeacherRole> currentYearRoles = teacherRoleDAO.findByIdGiaoVienAndNamHocOrderByIdDesc(teacherId, schoolYear);
        TeacherRole teacherRole = currentYearRoles.isEmpty() ? new TeacherRole() : currentYearRoles.get(0);

        teacherRole.setIdGiaoVien(teacherId);
        teacherRole.setNamHoc(schoolYear);
        teacherRole.setIdLoaiVaiTro(roleTypeId);
        teacherRole.setGhiChu(EDIT_ROLE_NOTE);

        teacherRoleDAO.save(teacherRole);
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

    private String resolveSubjectId(String chuyenMon) {
        String normalized = normalize(chuyenMon);
        if (normalized == null) {
            return null;
        }

        for (Subject subject : subjectDAO.findAll()) {
            String subjectId = normalize(subject.getIdMonHoc());
            String subjectName = normalize(subject.getTenMonHoc());

            if (normalized.equalsIgnoreCase(subjectId) || normalized.equalsIgnoreCase(subjectName)) {
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

        String ascii = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);

        return ascii;
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
