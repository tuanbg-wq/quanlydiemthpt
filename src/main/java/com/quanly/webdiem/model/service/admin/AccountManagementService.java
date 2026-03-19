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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AccountManagementService {

    private static final int PAGE_SIZE = 10;
    private static final String ROLE_TEACHER = "Giao_vien";
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
        String vaiTro = normalize(search == null ? null : search.getVaiTro());
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

    public List<RoleOption> getRoleSelections() {
        return roleDAO.findAll().stream()
                .sorted(Comparator.comparing(Role::getTenVaiTro, String.CASE_INSENSITIVE_ORDER))
                .map(role -> new RoleOption(role.getIdVaiTro(), role.getTenVaiTro()))
                .toList();
    }

    public List<String> getRoleFilters() {
        return userDAO.findRoleNames();
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
        return form;
    }

    public AccountUpsertForm getEditForm(Integer accountId) {
        User user = findUserOrThrow(accountId);

        AccountUpsertForm form = new AccountUpsertForm();
        form.setTenDangNhap(user.getTenDangNhap());
        form.setEmail(user.getEmail());
        form.setIdVaiTro(user.getVaiTro() == null ? null : user.getVaiTro().getIdVaiTro());
        form.setTrangThai(normalizeStatus(user.getTrangThai()));

        List<Teacher> linkedTeachers = teacherDAO.findByIdTaiKhoan(user.getIdTaiKhoan());
        if (!linkedTeachers.isEmpty()) {
            form.setIdGiaoVien(linkedTeachers.get(0).getIdGiaoVien());
        }

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

        Role role = findRoleOrThrow(form.getIdVaiTro());

        User user = new User();
        user.setTenDangNhap(username);
        user.setMatKhau(passwordHasher.encode(rawPassword));
        user.setEmail(email);
        user.setVaiTro(role);
        user.setTrangThai(normalizeStatus(form.getTrangThai()));
        userDAO.save(user);

        syncTeacherLink(user.getIdTaiKhoan(), username, normalize(form.getIdGiaoVien()), role.getTenVaiTro());
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

        Role role = findRoleOrThrow(form.getIdVaiTro());

        user.setTenDangNhap(username);
        user.setEmail(email);
        user.setVaiTro(role);
        user.setTrangThai(normalizeStatus(form.getTrangThai()));
        userDAO.save(user);

        syncTeacherLink(user.getIdTaiKhoan(), username, normalize(form.getIdGiaoVien()), role.getTenVaiTro());
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

    private AccountRow mapRow(Object[] row) {
        return new AccountRow(
                asInt(row, 0),
                asString(row, 1),
                asString(row, 2),
                asString(row, 3),
                normalizeStatus(asString(row, 4)),
                asString(row, 5),
                defaultIfBlank(asString(row, 6), "-")
        );
    }

    private Role findRoleOrThrow(Integer roleId) {
        if (roleId == null) {
            throw new RuntimeException("Vai tro la bat buoc.");
        }
        return roleDAO.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Vai tro khong ton tai."));
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

    private void syncTeacherLink(Integer accountId, String username, String inputTeacherId, String roleName) {
        clearTeacherLinks(accountId);

        if (!ROLE_TEACHER.equalsIgnoreCase(defaultIfBlank(roleName, ""))) {
            return;
        }

        String teacherId = inputTeacherId;
        if (teacherId == null) {
            teacherId = normalize(username);
        }
        if (teacherId == null) {
            return;
        }

        String normalizedTeacherId = teacherId.toUpperCase(Locale.ROOT);
        Teacher teacher = teacherDAO.findById(normalizedTeacherId).orElse(null);
        if (teacher == null) {
            if (inputTeacherId != null) {
                throw new RuntimeException("Ma giao vien khong ton tai.");
            }
            return;
        }

        Integer linkedAccountId = teacher.getIdTaiKhoan();
        if (linkedAccountId != null && !linkedAccountId.equals(accountId)) {
            throw new RuntimeException("Giao vien nay da duoc lien ket voi tai khoan khac.");
        }

        teacher.setIdTaiKhoan(accountId);
        teacherDAO.save(teacher);
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

    private String normalizeStatus(String status) {
        String normalized = normalize(status);
        if (STATUS_LOCKED.equalsIgnoreCase(normalized)) {
            return STATUS_LOCKED;
        }
        return STATUS_ACTIVE;
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

    public static class RoleOption {
        private final Integer id;
        private final String name;

        public RoleOption(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
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
