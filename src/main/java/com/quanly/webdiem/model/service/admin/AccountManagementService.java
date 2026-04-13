package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.RoleDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.dto.SelectOption;
import com.quanly.webdiem.model.search.AccountSearch;
import com.quanly.webdiem.model.form.AccountUpsertForm;
import com.quanly.webdiem.model.entity.Role;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.User;
import com.quanly.webdiem.security.PasswordHasher;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AccountManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManagementService.class);

    private static final int PAGE_SIZE = 6;
    private static final int PASSWORD_HISTORY_LIMIT = 20;

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
    private final EntityManager entityManager;
    private final TransactionTemplate requiresNewTransactionTemplate;

    private volatile boolean passwordHistoryTableReady = false;

    public AccountManagementService(UserDAO userDAO,
                                    RoleDAO roleDAO,
                                    TeacherDAO teacherDAO,
                                    PasswordHasher passwordHasher,
                                    EntityManager entityManager,
                                    PlatformTransactionManager transactionManager) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.teacherDAO = teacherDAO;
        this.passwordHasher = passwordHasher;
        this.entityManager = entityManager;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
        form.setMatKhauHienTai(defaultIfBlank(loadCurrentPasswordPlain(user.getIdTaiKhoan()), "-"));

        List<Teacher> linkedTeachers = teacherDAO.findByIdTaiKhoan(user.getIdTaiKhoan());
        String linkedTeacherId = null;
        if (!linkedTeachers.isEmpty()) {
            linkedTeacherId = linkedTeachers.get(0).getIdGiaoVien();
            form.setIdGiaoVien(linkedTeacherId);
            applyTeacherProfileSnapshot(form, linkedTeacherId, accountId);
        }

        String roleName = user.getVaiTro() == null ? null : user.getVaiTro().getTenVaiTro();
        String resolvedRoleCode = linkedTeacherId == null
                ? resolveDisplayRoleCode(roleName)
                : resolveRoleCodeByTeacherId(linkedTeacherId);
        form.setVaiTroMa(resolvedRoleCode);

        return form;
    }

    @Transactional
    public void createAccount(AccountUpsertForm form, String actorUsername) {
        String username = normalize(form.getTenDangNhap());
        String rawPassword = normalize(form.getMatKhau());
        String email = normalize(form.getEmail());

        if (username == null || rawPassword == null) {
            throw new RuntimeException("Tên đăng nhập và mật khẩu là bắt buộc.");
        }

        if (userDAO.existsByTenDangNhap(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }

        validateEmailUnique(email, null);
        validatePasswordRule(rawPassword);

        String teacherId = normalize(form.getIdGiaoVien());
        RoleContext roleContext = resolveRoleContext(form.getVaiTroMa(), teacherId);
        Role role = findSystemRoleOrThrow(roleContext.roleCode());

        User user = new User();
        user.setTenDangNhap(username);
        user.setMatKhau(passwordHasher.encode(rawPassword));
        user.setEmail(email);
        user.setVaiTro(role);
        user.setTrangThai(normalizeStatus(form.getTrangThai()));
        userDAO.save(user);
        persistCurrentPasswordPlain(user.getIdTaiKhoan(), rawPassword);

        syncTeacherLink(user.getIdTaiKhoan(), roleContext.teacherId());
        recordPasswordHistory(
                user.getIdTaiKhoan(),
                actorUsername,
                "TAO_TAI_KHOAN",
                "Thiết lập mật khẩu ban đầu",
                null,
                rawPassword
        );
    }

    @Transactional
    public void updateAccount(Integer accountId, AccountUpsertForm form, String actorUsername) {
        User user = findUserOrThrow(accountId);

        String username = normalize(form.getTenDangNhap());
        String rawPassword = normalize(form.getMatKhau());
        String email = normalize(form.getEmail());

        if (username == null) {
            throw new RuntimeException("Tên đăng nhập là bắt buộc.");
        }

        if (!username.equalsIgnoreCase(user.getTenDangNhap()) && userDAO.existsByTenDangNhap(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }

        validateEmailUnique(email, user.getIdTaiKhoan());

        String oldPasswordPlain = defaultIfBlank(loadCurrentPasswordPlain(user.getIdTaiKhoan()), "-");
        boolean changedPassword = false;
        if (rawPassword != null) {
            validatePasswordRule(rawPassword);
            user.setMatKhau(passwordHasher.encode(rawPassword));
            changedPassword = true;
        }

        String teacherId = normalize(form.getIdGiaoVien());
        RoleContext roleContext = resolveRoleContext(form.getVaiTroMa(), teacherId);
        Role role = findSystemRoleOrThrow(roleContext.roleCode());

        user.setTenDangNhap(username);
        user.setEmail(email);
        user.setVaiTro(role);
        user.setTrangThai(normalizeStatus(form.getTrangThai()));
        userDAO.save(user);

        syncTeacherLink(user.getIdTaiKhoan(), roleContext.teacherId());
        if (changedPassword) {
            persistCurrentPasswordPlain(user.getIdTaiKhoan(), rawPassword);
            recordPasswordHistory(
                    user.getIdTaiKhoan(),
                    actorUsername,
                    "DOI_MAT_KHAU",
                    "Cập nhật từ trang chỉnh sửa tài khoản",
                    oldPasswordPlain,
                    rawPassword
            );
        }
    }

    @Transactional
    public void toggleLock(Integer accountId, String actorUsername) {
        User user = findUserOrThrow(accountId);

        if (actorUsername != null && actorUsername.equalsIgnoreCase(user.getTenDangNhap())) {
            throw new RuntimeException("Không thể tự khóa tài khoản đang đăng nhập.");
        }

        String currentStatus = normalizeStatus(user.getTrangThai());
        user.setTrangThai(STATUS_LOCKED.equalsIgnoreCase(currentStatus) ? STATUS_ACTIVE : STATUS_LOCKED);
        userDAO.save(user);
    }

    @Transactional
    public void deleteAccount(Integer accountId, String actorUsername) {
        User user = findUserOrThrow(accountId);

        if (actorUsername != null && actorUsername.equalsIgnoreCase(user.getTenDangNhap())) {
            throw new RuntimeException("Không thể xóa tài khoản đang đăng nhập.");
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

        String roleCode = resolveRoleCodeByTeacherId(normalizedTeacherId);
        return mapTeacherProfile(rows.get(0), roleCode);
    }

    @Transactional
    public AccountInfo getAccountInfo(Integer accountId) {
        User user = findUserOrThrow(accountId);

        TeacherProfile teacherProfile = null;
        String linkedTeacherId = null;
        List<Teacher> linkedTeachers = teacherDAO.findByIdTaiKhoan(user.getIdTaiKhoan());
        if (!linkedTeachers.isEmpty()) {
            linkedTeacherId = linkedTeachers.get(0).getIdGiaoVien();
            teacherProfile = getTeacherProfile(linkedTeacherId, accountId);
        }

        String roleName = user.getVaiTro() == null ? null : user.getVaiTro().getTenVaiTro();
        String resolvedRoleCode = linkedTeacherId == null
                ? resolveDisplayRoleCode(roleName)
                : resolveRoleCodeByTeacherId(linkedTeacherId);
        return new AccountInfo(
                user.getIdTaiKhoan(),
                user.getTenDangNhap(),
                defaultIfBlank(user.getEmail(), "-"),
                normalizeStatus(user.getTrangThai()),
                resolveDisplayRoleLabel(resolvedRoleCode),
                defaultIfBlank(loadCurrentPasswordPlain(user.getIdTaiKhoan()), "-"),
                teacherProfile,
                loadPasswordHistory(user.getIdTaiKhoan())
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

    private TeacherProfile mapTeacherProfile(Object[] row, String roleCode) {
        return new TeacherProfile(
                asString(row, 0),
                defaultIfBlank(asString(row, 1), "-"),
                defaultIfBlank(asString(row, 2), "-"),
                defaultIfBlank(asString(row, 3), "-"),
                defaultIfBlank(asString(row, 4), "-"),
                defaultIfBlank(asString(row, 5), "-"),
                defaultIfBlank(asString(row, 6), "-"),
                roleCode,
                resolveDisplayRoleLabel(roleCode)
        );
    }

    private PasswordHistoryItem mapPasswordHistory(Object[] row) {
        return new PasswordHistoryItem(
                defaultIfBlank(asString(row, 0), "-"),
                defaultIfBlank(asString(row, 1), "-"),
                resolvePasswordActionLabel(asString(row, 2)),
                normalizePasswordHistoryNote(defaultIfBlank(asString(row, 3), "")),
                defaultIfBlank(asString(row, 4), "-"),
                defaultIfBlank(asString(row, 5), "-")
        );
    }

    private String normalizePasswordHistoryNote(String note) {
        String normalized = normalize(note);
        if (normalized == null) {
            return "";
        }
        if ("Cap nhat tu trang chinh sua tai khoan".equalsIgnoreCase(normalized)) {
            return "Cập nhật từ trang chỉnh sửa tài khoản";
        }
        if ("Thiet lap mat khau ban dau".equalsIgnoreCase(normalized)) {
            return "Thiết lập mật khẩu ban đầu";
        }
        return normalized;
    }

    private String resolvePasswordActionLabel(String rawAction) {
        String normalizedAction = normalize(rawAction);
        if (normalizedAction == null) {
            return "-";
        }

        return switch (normalizedAction.toUpperCase(Locale.ROOT)) {
            case "TAO_TAI_KHOAN" -> "Tạo tài khoản";
            case "DOI_MAT_KHAU" -> "Đổi mật khẩu";
            default -> {
                if ("Tao tai khoan".equalsIgnoreCase(normalizedAction)) {
                    yield "Tạo tài khoản";
                }
                if ("Doi mat khau".equalsIgnoreCase(normalizedAction)) {
                    yield "Đổi mật khẩu";
                }
                yield normalizedAction;
            }
        };
    }

    private Role findSystemRoleOrThrow(String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        if (normalizedRoleCode == null) {
            throw new RuntimeException("Vai trò là bắt buộc.");
        }

        return switch (normalizedRoleCode) {
            case ROLE_CODE_ADMIN -> findRoleByNameIgnoreCase("Admin")
                    .orElseThrow(() -> new RuntimeException("Vai trò Admin không tồn tại."));
            case ROLE_CODE_GVCN -> findRoleByNameIgnoreCase("GVCN")
                    .or(() -> findRoleByNameIgnoreCase("Giao_vien"))
                    .orElseThrow(() -> new RuntimeException("Vai trò giáo viên không tồn tại."));
            case ROLE_CODE_GVBM -> findRoleByNameIgnoreCase("GVBM")
                    .or(() -> findRoleByNameIgnoreCase("Giao_vien"))
                    .orElseThrow(() -> new RuntimeException("Vai trò giáo viên không tồn tại."));
            default -> throw new RuntimeException("Vai trò không hợp lệ.");
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
            throw new RuntimeException("Email đã tồn tại.");
        }
    }

    private void validatePasswordRule(String password) {
        if (password == null || password.length() < 5 || !password.contains("@")) {
            throw new RuntimeException("Mật khẩu phải từ 5 ký tự và có ký tự @.");
        }

        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasDigit) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 1 chữ số.");
        }
    }

    private void syncTeacherLink(Integer accountId, String inputTeacherId) {
        clearTeacherLinks(accountId);

        String teacherId = normalize(inputTeacherId);
        if (teacherId == null) {
            return;
        }

        String normalizedTeacherId = teacherId.toUpperCase(Locale.ROOT);
        Teacher teacher = teacherDAO.findById(normalizedTeacherId).orElse(null);
        if (teacher == null) {
            throw new RuntimeException("Mã giáo viên không tồn tại.");
        }

        Integer linkedAccountId = teacher.getIdTaiKhoan();
        if (linkedAccountId != null && !linkedAccountId.equals(accountId)) {
            throw new RuntimeException("Giáo viên này đã được liên kết với tài khoản khác.");
        }

        teacher.setIdTaiKhoan(accountId);
        teacherDAO.save(teacher);
    }

    private RoleContext resolveRoleContext(String selectedRoleCode, String teacherId) {
        String normalizedTeacherId = normalizeTeacherId(teacherId);
        if (normalizedTeacherId != null) {
            return new RoleContext(resolveRoleCodeByTeacherId(normalizedTeacherId), normalizedTeacherId);
        }

        String normalizedRoleCode = normalizeRoleCode(selectedRoleCode);
        if (ROLE_CODE_GVCN.equals(normalizedRoleCode) || ROLE_CODE_GVBM.equals(normalizedRoleCode)) {
            throw new RuntimeException("Vui lòng chọn mã giáo viên để tạo tài khoản giáo viên.");
        }

        return new RoleContext(requireRoleCode(normalizedRoleCode), null);
    }

    private String requireRoleCode(String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        if (normalizedRoleCode == null) {
            throw new RuntimeException("Vai trò là bắt buộc.");
        }
        return normalizedRoleCode;
    }

    private String normalizeTeacherId(String teacherId) {
        String normalizedTeacherId = normalize(teacherId);
        if (normalizedTeacherId == null) {
            return null;
        }
        return normalizedTeacherId.toUpperCase(Locale.ROOT);
    }

    private String resolveRoleCodeByTeacherId(String teacherId) {
        String normalizedTeacherId = normalizeTeacherId(teacherId);
        if (normalizedTeacherId == null) {
            return ROLE_CODE_GVBM;
        }
        if (teacherDAO.countHomeroomClassReferences(normalizedTeacherId) > 0) {
            return ROLE_CODE_GVCN;
        }
        return ROLE_CODE_GVBM;
    }

    protected void ensurePasswordHistoryTable() {
        if (passwordHistoryTableReady) {
            return;
        }

        synchronized (this) {
            if (passwordHistoryTableReady) {
                return;
            }

            entityManager.createNativeQuery("""
                    CREATE TABLE IF NOT EXISTS account_password_history (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        id_tai_khoan INT NOT NULL,
                        changed_by VARCHAR(50) NULL,
                        change_action VARCHAR(50) NOT NULL,
                        change_note VARCHAR(255) NULL,
                        old_password_plain VARCHAR(72) NULL,
                        new_password_plain VARCHAR(72) NULL,
                        changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        INDEX idx_acc_pass_history_account_time (id_tai_khoan, changed_at),
                        CONSTRAINT fk_acc_pass_history_user
                            FOREIGN KEY (id_tai_khoan) REFERENCES users(id_tai_khoan)
                            ON DELETE CASCADE
                    )
                    """).executeUpdate();
            entityManager.createNativeQuery("""
                    ALTER TABLE users
                    ADD COLUMN IF NOT EXISTS mat_khau_hien_tai VARCHAR(72) NULL
                    """).executeUpdate();
            entityManager.createNativeQuery("""
                    ALTER TABLE account_password_history
                    ADD COLUMN IF NOT EXISTS old_password_plain VARCHAR(72) NULL
                    """).executeUpdate();
            entityManager.createNativeQuery("""
                    ALTER TABLE account_password_history
                    ADD COLUMN IF NOT EXISTS new_password_plain VARCHAR(72) NULL
                    """).executeUpdate();
            passwordHistoryTableReady = true;
        }
    }

    private String loadCurrentPasswordPlain(Integer accountId) {
        if (accountId == null) {
            return null;
        }

        try {
            String value = requiresNewTransactionTemplate.execute(status -> {
                ensurePasswordHistoryTable();
                Object queryResult = entityManager.createNativeQuery("""
                        SELECT mat_khau_hien_tai
                        FROM users
                        WHERE id_tai_khoan = :accountId
                        """)
                        .setParameter("accountId", accountId)
                        .getSingleResult();
                return queryResult == null ? null : queryResult.toString();
            });
            return normalize(value);
        } catch (Exception ex) {
            LOGGER.warn("Không thể đọc mật khẩu hiện tại cho tài khoản {}", accountId, ex);
            return null;
        }
    }

    private void persistCurrentPasswordPlain(Integer accountId, String currentPasswordPlain) {
        if (accountId == null) {
            return;
        }

        try {
            requiresNewTransactionTemplate.executeWithoutResult(status -> {
                ensurePasswordHistoryTable();
                entityManager.createNativeQuery("""
                        UPDATE users
                        SET mat_khau_hien_tai = :currentPassword
                        WHERE id_tai_khoan = :accountId
                        """)
                        .setParameter("accountId", accountId)
                        .setParameter("currentPassword", normalize(currentPasswordPlain))
                        .executeUpdate();
            });
        } catch (Exception ex) {
            LOGGER.warn("Không thể cập nhật mật khẩu hiện tại cho tài khoản {}", accountId, ex);
        }
    }

    private void recordPasswordHistory(Integer accountId,
                                       String actorUsername,
                                       String action,
                                       String note,
                                       String oldPasswordPlain,
                                       String newPasswordPlain) {
        if (accountId == null) {
            return;
        }

        try {
            requiresNewTransactionTemplate.executeWithoutResult(status -> {
                ensurePasswordHistoryTable();
                entityManager.createNativeQuery("""
                        INSERT INTO account_password_history (
                            id_tai_khoan,
                            changed_by,
                            change_action,
                            change_note,
                            old_password_plain,
                            new_password_plain,
                            changed_at
                        ) VALUES (
                            :accountId,
                            :changedBy,
                            :changeAction,
                            :changeNote,
                            :oldPassword,
                            :newPassword,
                            CURRENT_TIMESTAMP
                        )
                        """)
                        .setParameter("accountId", accountId)
                        .setParameter("changedBy", defaultIfBlank(normalize(actorUsername), "SYSTEM"))
                        .setParameter("changeAction", defaultIfBlank(normalize(action), "-"))
                        .setParameter("changeNote", normalize(note))
                        .setParameter("oldPassword", normalize(oldPasswordPlain))
                        .setParameter("newPassword", normalize(newPasswordPlain))
                        .executeUpdate();
            });
        } catch (Exception ex) {
            LOGGER.warn("Không thể ghi lịch sử đổi mật khẩu cho tài khoản {}", accountId, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private List<PasswordHistoryItem> loadPasswordHistory(Integer accountId) {
        if (accountId == null) {
            return List.of();
        }

        try {
            List<Object[]> rows = requiresNewTransactionTemplate.execute(status -> {
                ensurePasswordHistoryTable();
                return entityManager.createNativeQuery("""
                        SELECT
                            COALESCE(DATE_FORMAT(changed_at, '%d/%m/%Y %H:%i:%s'), '-') AS changedAt,
                            COALESCE(changed_by, '-') AS changedBy,
                            COALESCE(change_action, '-') AS changeAction,
                            COALESCE(change_note, '') AS changeNote,
                            COALESCE(old_password_plain, '-') AS oldPassword,
                            COALESCE(new_password_plain, '-') AS newPassword
                        FROM account_password_history
                        WHERE id_tai_khoan = :accountId
                        ORDER BY changed_at DESC, id DESC
                        LIMIT 20
                        """)
                        .setParameter("accountId", accountId)
                        .getResultList();
            });

            if (rows == null || rows.isEmpty()) {
                return List.of();
            }
            return rows.stream()
                    .limit(PASSWORD_HISTORY_LIMIT)
                    .map(this::mapPasswordHistory)
                    .toList();
        } catch (Exception ex) {
            LOGGER.warn("Không thể tải lịch sử đổi mật khẩu cho tài khoản {}", accountId, ex);
            return List.of();
        }
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
            throw new RuntimeException("Tài khoản không hợp lệ.");
        }

        return userDAO.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));
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
        return "Giáo viên bộ môn";
    }

    private List<SelectOption> buildRoleOptions() {
        return List.of(
                new SelectOption(ROLE_CODE_ADMIN, "Admin"),
                new SelectOption(ROLE_CODE_GVCN, "GVCN"),
                new SelectOption(ROLE_CODE_GVBM, "Giáo viên bộ môn")
        );
    }

    private String normalizeDisplayRoleLabel(String roleLabel) {
        String normalized = normalize(roleLabel);
        if (normalized == null) {
            return "-";
        }
        if ("Giao vien bo mon".equalsIgnoreCase(normalized)) {
            return "Giáo viên bộ môn";
        }
        return normalized;
    }

    private record RoleContext(String roleCode, String teacherId) {}

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
        private final String vaiTroMa;
        private final String vaiTro;

        public TeacherProfile(String idGiaoVien,
                              String hoTen,
                              String gioiTinh,
                              String ngaySinh,
                              String monDay,
                              String soDienThoai,
                              String email,
                              String vaiTroMa,
                              String vaiTro) {
            this.idGiaoVien = idGiaoVien;
            this.hoTen = hoTen;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.monDay = monDay;
            this.soDienThoai = soDienThoai;
            this.email = email;
            this.vaiTroMa = vaiTroMa;
            this.vaiTro = vaiTro;
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

        public String getVaiTroMa() {
            return vaiTroMa;
        }

        public String getVaiTro() {
            return vaiTro;
        }
    }

    public static class AccountInfo {
        private final Integer idTaiKhoan;
        private final String tenDangNhap;
        private final String email;
        private final String trangThai;
        private final String vaiTro;
        private final String matKhauHienTai;
        private final TeacherProfile teacherProfile;
        private final List<PasswordHistoryItem> passwordHistory;

        public AccountInfo(Integer idTaiKhoan,
                           String tenDangNhap,
                           String email,
                           String trangThai,
                           String vaiTro,
                           String matKhauHienTai,
                           TeacherProfile teacherProfile,
                           List<PasswordHistoryItem> passwordHistory) {
            this.idTaiKhoan = idTaiKhoan;
            this.tenDangNhap = tenDangNhap;
            this.email = email;
            this.trangThai = trangThai;
            this.vaiTro = vaiTro;
            this.matKhauHienTai = matKhauHienTai;
            this.teacherProfile = teacherProfile;
            this.passwordHistory = passwordHistory == null ? List.of() : passwordHistory;
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

        public String getMatKhauHienTai() {
            return matKhauHienTai;
        }

        public TeacherProfile getTeacherProfile() {
            return teacherProfile;
        }

        public List<PasswordHistoryItem> getPasswordHistory() {
            return passwordHistory;
        }
    }

    public static class PasswordHistoryItem {
        private final String thoiGian;
        private final String nguoiThayDoi;
        private final String hanhDong;
        private final String ghiChu;
        private final String matKhauCu;
        private final String matKhauMoi;

        public PasswordHistoryItem(String thoiGian,
                                   String nguoiThayDoi,
                                   String hanhDong,
                                   String ghiChu,
                                   String matKhauCu,
                                   String matKhauMoi) {
            this.thoiGian = thoiGian;
            this.nguoiThayDoi = nguoiThayDoi;
            this.hanhDong = hanhDong;
            this.ghiChu = ghiChu;
            this.matKhauCu = matKhauCu;
            this.matKhauMoi = matKhauMoi;
        }

        public String getThoiGian() {
            return thoiGian;
        }

        public String getNguoiThayDoi() {
            return nguoiThayDoi;
        }

        public String getHanhDong() {
            return hanhDong;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public String getMatKhauCu() {
            return matKhauCu;
        }

        public String getMatKhauMoi() {
            return matKhauMoi;
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
