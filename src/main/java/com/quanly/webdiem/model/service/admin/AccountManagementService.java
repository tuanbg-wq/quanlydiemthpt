package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.RoleDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.AccountSearch;
import com.quanly.webdiem.model.entity.AccountUpsertForm;
import com.quanly.webdiem.model.entity.Role;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.User;
import com.quanly.webdiem.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AccountManagementService {

    private static final int PAGE_SIZE = 6;

    private static final String ROLE_CODE_ADMIN = "ADMIN";
    private static final String ROLE_CODE_GVCN = "GVCN";
    private static final String ROLE_CODE_GVBM = "GVBM";

    private static final String ROLE_DB_ADMIN = "admin";
    private static final String ROLE_DB_GIAO_VIEN = "giao_vien";
    private static final String ROLE_DB_GVCN = "gvcn";
    private static final String ROLE_DB_GVBM = "gvbm";

    private static final String STATUS_ACTIVE = "hoat_dong";
    private static final String STATUS_LOCKED = "khoa";

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final TeacherDAO teacherDAO;
    private final PasswordHasher passwordHasher;

    public AccountManagementService(UserDAO userDAO,
                                    RoleDAO roleDAO,
                                    TeacherDAO teacherDAO,
                                    PasswordHasher passwordHasher) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.teacherDAO = teacherDAO;
        this.passwordHasher = passwordHasher;
    }

    public AccountPageResult search(AccountSearch search) {
        String q = normalize(search == null ? null : search.getQ());
        String vaiTro = normalizeRoleFilter(search == null ? null : search.getVaiTro());
        String trangThai = normalize(search == null ? null : search.getTrangThai());
        String khoi = normalize(search == null ? null : search.getKhoi());

        long totalItems = userDAO.countSearchAccounts(q, vaiTro, trangThai, khoi);
        int totalPages = totalItems <= 0 ? 1 : (int) Math.ceil((double) totalItems / PAGE_SIZE);
        int page = resolvePage(search == null ? null : search.getPage(), totalPages);
        int offset = (page - 1) * PAGE_SIZE;

        List<AccountRow> items = userDAO.searchAccounts(q, vaiTro, trangThai, khoi, PAGE_SIZE, offset).stream()
                .map(this::mapRow)
                .toList();

        int fromRecord = totalItems == 0 ? 0 : offset + 1;
        int toRecord = totalItems == 0 ? 0 : offset + items.size();

        return new AccountPageResult(items, page, totalPages, (int) totalItems, fromRecord, toRecord);
    }

    public AccountStats getStats() {
        long total = userDAO.countAllAccounts();
        long admins = userDAO.countAdminAccounts();
        long homeroom = userDAO.countActiveHomeroomTeacherAccounts();
        long subject = userDAO.countActiveSubjectTeacherAccounts();
        return new AccountStats(total, admins, homeroom, subject);
    }

    public List<SelectOption> getRoleSelections() {
        return buildRoleOptions();
    }

    public List<SelectOption> getRoleFilters() {
        return buildRoleOptions();
    }

    public List<String> getStatusFilters() {
        return List.of(STATUS_ACTIVE, STATUS_LOCKED);
    }

    public List<Integer> getGradeFilters() {
        return userDAO.findGradeOptions();
    }

    public AccountUpsertForm initCreateForm() {
        AccountUpsertForm form = new AccountUpsertForm();
        form.setTrangThai(STATUS_ACTIVE);
        form.setVaiTroMa(ROLE_CODE_ADMIN);
        return form;
    }

    public AccountUpsertForm getEditForm(Integer accountId) {
        User user = findUserOrThrow(accountId);

        AccountUpsertForm form = new AccountUpsertForm();
        form.setTenDangNhap(user.getTenDangNhap());
        form.setEmail(user.getEmail());
        form.setTrangThai(normalizeStatus(user.getTrangThai()));

        List<Teacher> linkedTeachers = teacherDAO.findByIdTaiKhoan(user.getIdTaiKhoan());
        String linkedTeacherId = null;
        if (!linkedTeachers.isEmpty()) {
            linkedTeacherId = linkedTeachers.get(0).getIdGiaoVien();
            form.setIdGiaoVien(linkedTeacherId);
            applyTeacherProfileSnapshot(form, linkedTeacherId, accountId);
        }

        String roleName = user.getVaiTro() == null ? null : user.getVaiTro().getTenVaiTro();
        form.setVaiTroMa(resolveDisplayRoleCode(roleName));

        return form;
    }

    @Transactional
    public void createAccount(AccountUpsertForm form) {
        String username = normalize(form.getTenDangNhap());
        String rawPassword = normalize(form.getMatKhau());
        String email = normalize(form.getEmail());

        if (username == null || rawPassword == null) {
            throw new RuntimeException("Ten dang nhap va mat khau la bat buoc.");
        }

        if (userDAO.existsByTenDangNhap(username)) {
            throw new RuntimeException("Ten dang nhap da ton tai.");
        }

        validateEmailUnique(email, null);
        validatePasswordRule(rawPassword);

        Role role = findSystemRoleOrThrow(form.getVaiTroMa());

        User user = new User();
        user.setTenDangNhap(username);
        user.setMatKhau(passwordHasher.encode(rawPassword));
        user.setEmail(email);
        user.setVaiTro(role);
        user.setTrangThai(normalizeStatus(form.getTrangThai()));
        userDAO.save(user);

        syncTeacherLink(
                user.getIdTaiKhoan(),
                normalize(form.getIdGiaoVien()),
                normalize(form.getVaiTroMa()),
                role.getTenVaiTro()
        );
    }

    @Transactional
    public void updateAccount(Integer accountId, AccountUpsertForm form) {
        User user = findUserOrThrow(accountId);

        String username = normalize(form.getTenDangNhap());
        String rawPassword = normalize(form.getMatKhau());
        String email = normalize(form.getEmail());

        if (username == null) {
            throw new RuntimeException("Ten dang nhap la bat buoc.");
        }

        if (!username.equalsIgnoreCase(user.getTenDangNhap()) && userDAO.existsByTenDangNhap(username)) {
            throw new RuntimeException("Ten dang nhap da ton tai.");
        }

        validateEmailUnique(email, user.getIdTaiKhoan());

        if (rawPassword != null) {
            validatePasswordRule(rawPassword);
            user.setMatKhau(passwordHasher.encode(rawPassword));
        }

        Role role = findSystemRoleOrThrow(form.getVaiTroMa());

        user.setTenDangNhap(username);
        user.setEmail(email);
        user.setVaiTro(role);
        user.setTrangThai(normalizeStatus(form.getTrangThai()));
        userDAO.save(user);

        syncTeacherLink(
                user.getIdTaiKhoan(),
                normalize(form.getIdGiaoVien()),
                normalize(form.getVaiTroMa()),
                role.getTenVaiTro()
        );
    }

    @Transactional
    public void toggleLock(Integer accountId, String actorUsername) {
        User user = findUserOrThrow(accountId);

        if (actorUsername != null && actorUsername.equalsIgnoreCase(user.getTenDangNhap())) {
            throw new RuntimeException("Khong the tu khoa tai khoan dang dang nhap.");
        }

        String currentStatus = normalizeStatus(user.getTrangThai());
        user.setTrangThai(STATUS_LOCKED.equalsIgnoreCase(currentStatus) ? STATUS_ACTIVE : STATUS_LOCKED);
        userDAO.save(user);
    }

    @Transactional
    public void deleteAccount(Integer accountId, String actorUsername) {
        User user = findUserOrThrow(accountId);

        if (actorUsername != null && actorUsername.equalsIgnoreCase(user.getTenDangNhap())) {
            throw new RuntimeException("Khong the xoa tai khoan dang dang nhap.");
        }

        clearTeacherLinks(user.getIdTaiKhoan());
        userDAO.delete(user);
    }

    public List<TeacherSuggestionItem> suggestTeachers(String query, Integer accountId) {
        return teacherDAO.suggestTeachersForAccount(normalize(query), accountId).stream()
                .map(this::mapTeacherSuggestion)
                .toList();
    }

    public TeacherProfile getTeacherProfile(String teacherId, Integer accountId) {
        String normalizedTeacherId = normalize(teacherId);
        if (normalizedTeacherId == null) {
            return null;
        }

        List<Object[]> rows = teacherDAO.findTeacherProfileForAccount(normalizedTeacherId, accountId);
        if (rows.isEmpty()) {
            return null;
        }

        return mapTeacherProfile(rows.get(0));
    }

    public AccountInfo getAccountInfo(Integer accountId) {
        User user = findUserOrThrow(accountId);

        TeacherProfile teacherProfile = null;
        List<Teacher> linkedTeachers = teacherDAO.findByIdTaiKhoan(user.getIdTaiKhoan());
        if (!linkedTeachers.isEmpty()) {
            teacherProfile = getTeacherProfile(linkedTeachers.get(0).getIdGiaoVien(), accountId);
        }

        String roleName = user.getVaiTro() == null ? null : user.getVaiTro().getTenVaiTro();
        return new AccountInfo(
                user.getIdTaiKhoan(),
                user.getTenDangNhap(),
                defaultIfBlank(user.getEmail(), "-"),
                normalizeStatus(user.getTrangThai()),
                resolveDisplayRoleLabel(resolveDisplayRoleCode(roleName)),
                teacherProfile
        );
    }

    private AccountRow mapRow(Object[] row) {
        return new AccountRow(
                asInt(row, 0),
                asString(row, 1),
                asString(row, 2),
                normalizeDisplayRoleLabel(asString(row, 3)),
                normalizeStatus(asString(row, 4)),
                asString(row, 5),
                defaultIfBlank(asString(row, 6), "-")
        );
    }

    private TeacherSuggestionItem mapTeacherSuggestion(Object[] row) {
        String id = asString(row, 0);
        String hoTen = defaultIfBlank(asString(row, 1), "-");
        String monDay = defaultIfBlank(asString(row, 2), "-");
        String label = id == null
                ? hoTen
                : id + " - " + hoTen + (monDay.equals("-") ? "" : " - " + monDay);
        return new TeacherSuggestionItem(id, hoTen, monDay, label);
    }

    private TeacherProfile mapTeacherProfile(Object[] row) {
        return new TeacherProfile(
                asString(row, 0),
                defaultIfBlank(asString(row, 1), "-"),
                defaultIfBlank(asString(row, 2), "-"),
                defaultIfBlank(asString(row, 3), "-"),
                defaultIfBlank(asString(row, 4), "-"),
                defaultIfBlank(asString(row, 5), "-"),
                defaultIfBlank(asString(row, 6), "-")
        );
    }

    private Role findSystemRoleOrThrow(String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        if (normalizedRoleCode == null) {
            throw new RuntimeException("Vai tro la bat buoc.");
        }

        return switch (normalizedRoleCode) {
            case ROLE_CODE_ADMIN -> findRoleByNameIgnoreCase("Admin")
                    .orElseThrow(() -> new RuntimeException("Vai tro Admin khong ton tai."));
            case ROLE_CODE_GVCN -> findRoleByNameIgnoreCase("GVCN")
                    .or(() -> findRoleByNameIgnoreCase("Giao_vien"))
                    .orElseThrow(() -> new RuntimeException("Vai tro giao vien khong ton tai."));
            case ROLE_CODE_GVBM -> findRoleByNameIgnoreCase("GVBM")
                    .or(() -> findRoleByNameIgnoreCase("Giao_vien"))
                    .orElseThrow(() -> new RuntimeException("Vai tro giao vien khong ton tai."));
            default -> throw new RuntimeException("Vai tro khong hop le.");
        };
    }

    private Optional<Role> findRoleByNameIgnoreCase(String roleName) {
        String normalizedRoleName = normalize(roleName);
        if (normalizedRoleName == null) {
            return Optional.empty();
        }

        return roleDAO.findAll().stream()
                .filter(role -> role != null && role.getTenVaiTro() != null)
                .filter(role -> role.getTenVaiTro().equalsIgnoreCase(normalizedRoleName))
                .findFirst();
    }

    private void validateEmailUnique(String email, Integer currentAccountId) {
        if (email == null) {
            return;
        }

        Optional<User> existing = userDAO.findByEmailIgnoreCase(email);
        if (existing.isPresent() && (currentAccountId == null || !existing.get().getIdTaiKhoan().equals(currentAccountId))) {
            throw new RuntimeException("Email da ton tai.");
        }
    }

    private void validatePasswordRule(String password) {
        if (password == null || password.length() < 6 || !password.contains("@")) {
            throw new RuntimeException("Mat khau phai tu 6 ky tu va co ky tu @.");
        }

        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasDigit) {
            throw new RuntimeException("Mat khau phai co it nhat 1 chu so.");
        }
    }

    private void syncTeacherLink(Integer accountId,
                                 String inputTeacherId,
                                 String roleCode,
                                 String systemRoleName) {
        clearTeacherLinks(accountId);

        if (!isTeacherRole(roleCode, systemRoleName)) {
            return;
        }

        String teacherId = inputTeacherId;
        if (teacherId == null) {
            throw new RuntimeException("Vui long chon ma giao vien hop le.");
        }

        String normalizedTeacherId = teacherId.toUpperCase(Locale.ROOT);
        Teacher teacher = teacherDAO.findById(normalizedTeacherId).orElse(null);
        if (teacher == null) {
            throw new RuntimeException("Ma giao vien khong ton tai.");
        }

        Integer linkedAccountId = teacher.getIdTaiKhoan();
        if (linkedAccountId != null && !linkedAccountId.equals(accountId)) {
            throw new RuntimeException("Giao vien nay da duoc lien ket voi tai khoan khac.");
        }

        teacher.setIdTaiKhoan(accountId);
        teacherDAO.save(teacher);
    }

    private boolean isTeacherRole(String roleCode, String systemRoleName) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        if (ROLE_CODE_GVCN.equals(normalizedRoleCode) || ROLE_CODE_GVBM.equals(normalizedRoleCode)) {
            return true;
        }

        String normalizedSystemRole = normalize(systemRoleName);
        if (normalizedSystemRole == null) {
            return false;
        }

        String role = normalizedSystemRole.toLowerCase(Locale.ROOT);
        return ROLE_DB_GIAO_VIEN.equals(role) || ROLE_DB_GVCN.equals(role) || ROLE_DB_GVBM.equals(role);
    }

    private void clearTeacherLinks(Integer accountId) {
        if (accountId == null) {
            return;
        }

        List<Teacher> linkedTeachers = teacherDAO.findByIdTaiKhoan(accountId);
        if (linkedTeachers.isEmpty()) {
            return;
        }

        for (Teacher teacher : linkedTeachers) {
            teacher.setIdTaiKhoan(null);
        }
        teacherDAO.saveAll(linkedTeachers);
    }

    private User findUserOrThrow(Integer accountId) {
        if (accountId == null) {
            throw new RuntimeException("Tai khoan khong hop le.");
        }

        return userDAO.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan."));
    }

    private int resolvePage(Integer rawPage, int totalPages) {
        int page = rawPage == null ? 1 : rawPage;
        if (page < 1) {
            return 1;
        }
        return Math.min(page, Math.max(1, totalPages));
    }

    private void applyTeacherProfileSnapshot(AccountUpsertForm form, String teacherId, Integer accountId) {
        if (form == null) {
            return;
        }

        TeacherProfile profile = getTeacherProfile(teacherId, accountId);
        if (profile == null) {
            return;
        }

        form.setHoTenGiaoVien(profile.getHoTen());
        form.setGioiTinhGiaoVien(profile.getGioiTinh());
        form.setNgaySinhGiaoVien(profile.getNgaySinh());
        form.setMonDayGiaoVien(profile.getMonDay());
        form.setSoDienThoaiGiaoVien(profile.getSoDienThoai());
    }

    private String normalizeStatus(String status) {
        String normalized = normalize(status);
        if (STATUS_LOCKED.equalsIgnoreCase(normalized)) {
            return STATUS_LOCKED;
        }
        return STATUS_ACTIVE;
    }

    private String normalizeRoleCode(String roleCode) {
        String normalized = normalize(roleCode);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeRoleFilter(String roleFilter) {
        String normalized = normalizeRoleCode(roleFilter);
        if (ROLE_CODE_ADMIN.equals(normalized) || ROLE_CODE_GVCN.equals(normalized) || ROLE_CODE_GVBM.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private String resolveDisplayRoleCode(String roleName) {
        String normalized = normalize(roleName);
        if (normalized == null) {
            return ROLE_CODE_ADMIN;
        }

        String normalizedRole = normalized.toLowerCase(Locale.ROOT);
        if (ROLE_DB_ADMIN.equals(normalizedRole)) {
            return ROLE_CODE_ADMIN;
        }
        if (ROLE_DB_GVCN.equals(normalizedRole)) {
            return ROLE_CODE_GVCN;
        }
        if (ROLE_DB_GVBM.equals(normalizedRole)) {
            return ROLE_CODE_GVBM;
        }
        if (!ROLE_DB_GIAO_VIEN.equals(normalizedRole)) {
            return ROLE_CODE_ADMIN;
        }
        return ROLE_CODE_GVBM;
    }

    private String resolveDisplayRoleLabel(String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        if (ROLE_CODE_ADMIN.equals(normalizedRoleCode)) {
            return "Admin";
        }
        if (ROLE_CODE_GVCN.equals(normalizedRoleCode)) {
            return "GVCN";
        }
        return "Gi\u00e1o vi\u00ean b\u1ed9 m\u00f4n";
    }

    private List<SelectOption> buildRoleOptions() {
        return List.of(
                new SelectOption(ROLE_CODE_ADMIN, "Admin"),
                new SelectOption(ROLE_CODE_GVCN, "GVCN"),
                new SelectOption(ROLE_CODE_GVBM, "Gi\u00e1o vi\u00ean b\u1ed9 m\u00f4n")
        );
    }

    private String normalizeDisplayRoleLabel(String roleLabel) {
        String normalized = normalize(roleLabel);
        if (normalized == null) {
            return "-";
        }
        if ("Giao vien bo mon".equalsIgnoreCase(normalized)) {
            return "Gi\u00e1o vi\u00ean b\u1ed9 m\u00f4n";
        }
        return normalized;
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

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static class AccountRow {
        private final Integer idTaiKhoan;
        private final String tenDangNhap;
        private final String hoTen;
        private final String vaiTro;
        private final String trangThai;
        private final String email;
        private final String khoiLop;

        public AccountRow(Integer idTaiKhoan,
                          String tenDangNhap,
                          String hoTen,
                          String vaiTro,
                          String trangThai,
                          String email,
                          String khoiLop) {
            this.idTaiKhoan = idTaiKhoan;
            this.tenDangNhap = tenDangNhap;
            this.hoTen = hoTen;
            this.vaiTro = vaiTro;
            this.trangThai = trangThai;
            this.email = email;
            this.khoiLop = khoiLop;
        }

        public Integer getIdTaiKhoan() {
            return idTaiKhoan;
        }

        public String getTenDangNhap() {
            return tenDangNhap;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getVaiTro() {
            return vaiTro;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public String getEmail() {
            return email;
        }

        public String getKhoiLop() {
            return khoiLop;
        }
    }

    public static class AccountPageResult {
        private final List<AccountRow> items;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final int fromRecord;
        private final int toRecord;

        public AccountPageResult(List<AccountRow> items,
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

        public List<AccountRow> getItems() {
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

    public static class SelectOption {
        private final String value;
        private final String label;

        public SelectOption(String value, String label) {
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

    public static class TeacherSuggestionItem {
        private final String idGiaoVien;
        private final String hoTen;
        private final String monDay;
        private final String label;

        public TeacherSuggestionItem(String idGiaoVien, String hoTen, String monDay, String label) {
            this.idGiaoVien = idGiaoVien;
            this.hoTen = hoTen;
            this.monDay = monDay;
            this.label = label;
        }

        public String getIdGiaoVien() {
            return idGiaoVien;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getMonDay() {
            return monDay;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class TeacherProfile {
        private final String idGiaoVien;
        private final String hoTen;
        private final String gioiTinh;
        private final String ngaySinh;
        private final String monDay;
        private final String soDienThoai;
        private final String email;

        public TeacherProfile(String idGiaoVien,
                              String hoTen,
                              String gioiTinh,
                              String ngaySinh,
                              String monDay,
                              String soDienThoai,
                              String email) {
            this.idGiaoVien = idGiaoVien;
            this.hoTen = hoTen;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.monDay = monDay;
            this.soDienThoai = soDienThoai;
            this.email = email;
        }

        public String getIdGiaoVien() {
            return idGiaoVien;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getGioiTinh() {
            return gioiTinh;
        }

        public String getNgaySinh() {
            return ngaySinh;
        }

        public String getMonDay() {
            return monDay;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class AccountInfo {
        private final Integer idTaiKhoan;
        private final String tenDangNhap;
        private final String email;
        private final String trangThai;
        private final String vaiTro;
        private final TeacherProfile teacherProfile;

        public AccountInfo(Integer idTaiKhoan,
                           String tenDangNhap,
                           String email,
                           String trangThai,
                           String vaiTro,
                           TeacherProfile teacherProfile) {
            this.idTaiKhoan = idTaiKhoan;
            this.tenDangNhap = tenDangNhap;
            this.email = email;
            this.trangThai = trangThai;
            this.vaiTro = vaiTro;
            this.teacherProfile = teacherProfile;
        }

        public Integer getIdTaiKhoan() {
            return idTaiKhoan;
        }

        public String getTenDangNhap() {
            return tenDangNhap;
        }

        public String getEmail() {
            return email;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public String getVaiTro() {
            return vaiTro;
        }

        public TeacherProfile getTeacherProfile() {
            return teacherProfile;
        }
    }

    public static class AccountStats {
        private final long totalAccounts;
        private final long adminCount;
        private final long homeroomTeacherCount;
        private final long subjectTeacherCount;

        public AccountStats(long totalAccounts,
                            long adminCount,
                            long homeroomTeacherCount,
                            long subjectTeacherCount) {
            this.totalAccounts = totalAccounts;
            this.adminCount = adminCount;
            this.homeroomTeacherCount = homeroomTeacherCount;
            this.subjectTeacherCount = subjectTeacherCount;
        }

        public long getTotalAccounts() {
            return totalAccounts;
        }

        public long getAdminCount() {
            return adminCount;
        }

        public long getHomeroomTeacherCount() {
            return homeroomTeacherCount;
        }

        public long getSubjectTeacherCount() {
            return subjectTeacherCount;
        }
    }
}
