package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.TeacherRoleDAO;
import com.quanly.webdiem.model.entity.Subject;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.TeacherCreateForm;
import com.quanly.webdiem.model.entity.TeacherRole;
import com.quanly.webdiem.model.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TeacherEditService {

    private static final String EDIT_ROLE_NOTE = "C\u1eadp nh\u1eadt vai tr\u00f2 t\u1eeb m\u00e0n h\u00ecnh ch\u1ec9nh s\u1eeda gi\u00e1o vi\u00ean";

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
        form.setTrangThai(normalize(teacher.getTrangThai()));
        form.setGhiChu(teacher.getGhiChu());

        applyRoleInformation(teacher.getIdGiaoVien(), form);

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
        Teacher teacher = findTeacherOrThrow(teacherId);

        Subject subject = subjectDAO.findById(form.getMonHocId())
                .orElseThrow(() -> new RuntimeException("M\u00f4n d\u1ea1y kh\u00f4ng t\u1ed3n t\u1ea1i."));

        teacher.setHoTen(normalizeFullName(form.getHoTen()));
        teacher.setNgaySinh(form.getNgaySinh());
        teacher.setGioiTinh(mapGenderForDatabase(form.getGioiTinh()));
        teacher.setSoDienThoai(normalize(form.getSoDienThoai()));
        teacher.setEmail(normalizeLower(form.getEmail()));
        teacher.setDiaChi(normalize(form.getDiaChi()));
        teacher.setChuyenMon(subject.getTenMonHoc());
        teacher.setTrinhDo(mapDegreeLabel(form.getTrinhDo()));
        teacher.setNgayVaoLam(form.getNgayBatDauCongTac());
        teacher.setTrangThai(normalize(form.getTrangThai()));
        teacher.setGhiChu(normalize(form.getGhiChu()));

        MultipartFile avatar = form.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            String avatarPath = fileStorageService.saveTeacherAvatar(teacher.getIdGiaoVien(), avatar);
            teacher.setAnh(avatarPath);
        }

        teacherDAO.save(teacher);
        upsertTeacherRole(teacher.getIdGiaoVien(), form);
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
            throw new RuntimeException("N\u0103m h\u1ecdc \u00e1p d\u1ee5ng vai tr\u00f2 l\u00e0 b\u1eaft bu\u1ed9c.");
        }

        String roleCode = normalizeSelectedRoleCode(form.getVaiTroMa());
        if (roleCode == null) {
            throw new RuntimeException("Vui l\u00f2ng ch\u1ecdn 1 vai tr\u00f2 gi\u00e1o vi\u00ean.");
        }

        Integer roleTypeId = resolveRoleTypeId(roleCode);
        if (roleTypeId == null) {
            throw new RuntimeException("Vai tr\u00f2 gi\u00e1o vi\u00ean kh\u00f4ng h\u1ee3p l\u1ec7.");
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
            return "C\u1eed nh\u00e2n";
        }

        if ("THAC_SI".equalsIgnoreCase(normalized)) {
            return "Th\u1ea1c s\u0129";
        }

        if ("TIEN_SI".equalsIgnoreCase(normalized)) {
            return "Ti\u1ebfn s\u0129";
        }

        return "Kh\u00e1c";
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

    private Teacher findTeacherOrThrow(String teacherId) {
        String normalizedId = normalize(teacherId);
        if (normalizedId == null) {
            throw new RuntimeException("M\u00e3 gi\u00e1o vi\u00ean kh\u00f4ng h\u1ee3p l\u1ec7.");
        }

        return teacherDAO.findById(normalizedId.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new RuntimeException("Kh\u00f4ng t\u00ecm th\u1ea5y gi\u00e1o vi\u00ean."));
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
