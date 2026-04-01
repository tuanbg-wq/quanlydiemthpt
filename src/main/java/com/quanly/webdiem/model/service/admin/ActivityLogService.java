package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ActivityLogDAO;
import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.ActivityLog;
import com.quanly.webdiem.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ActivityLogService {

    private static final String STUDENTS_TABLE = "students";
    private static final String ACTION_CREATE_STUDENT = "THEM_HOC_SINH";
    private static final String ACTION_UPDATE_STUDENT = "CAP_NHAT_HOC_SINH";
    private static final String ACTION_DELETE_STUDENT = "XOA_HOC_SINH";
    private static final String CONDUCT_EVENTS_TABLE = "conduct_events";
    private static final String ACTION_CREATE_REWARD = "THEM_KHEN_THUONG";
    private static final String ACTION_CREATE_DISCIPLINE = "THEM_KY_LUAT";
    private static final String ACTION_UPDATE_REWARD = "SUA_KHEN_THUONG";
    private static final String ACTION_UPDATE_DISCIPLINE = "SUA_KY_LUAT";
    private static final String ACTION_DELETE_REWARD = "XOA_KHEN_THUONG";
    private static final String ACTION_DELETE_DISCIPLINE = "XOA_KY_LUAT";
    private static final String CONDUCT_TYPE_REWARD = "KHEN_THUONG";
    private static final String CONDUCT_TYPE_DISCIPLINE = "KY_LUAT";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final String[] MOJIBAKE_MARKERS = {
            "\u00C3", "\u00C2", "\u00C4", "\u00C6", "\u00E1\u00BB", "\u00E1\u00BA", "\u00E2\u20AC", "\uFFFD"
    };

    private final ActivityLogDAO activityLogDAO;
    private final UserDAO userDAO;

    public ActivityLogService(ActivityLogDAO activityLogDAO, UserDAO userDAO) {
        this.activityLogDAO = activityLogDAO;
        this.userDAO = userDAO;
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> getStudentEditLogs(String studentId) {
        List<ActivityLog> logs = activityLogDAO.findByBangTacDongAndIdBanGhiOrderByThoiGianDesc(STUDENTS_TABLE, studentId);
        for (ActivityLog log : logs) {
            log.setNoiDung(normalizeMojibake(log.getNoiDung()));
        }
        return logs;
    }

    @Transactional
    public void logStudentUpdate(String studentId, String username, String summary, String ipAddress) {
        if (studentId == null || studentId.isBlank() || username == null || username.isBlank()) {
            return;
        }

        User actor = userDAO.findByTenDangNhap(username).orElse(null);
        if (actor == null || actor.getIdTaiKhoan() == null) {
            return;
        }

        ActivityLog log = new ActivityLog();
        log.setIdTaiKhoan(actor.getIdTaiKhoan());
        log.setHanhDong(ACTION_UPDATE_STUDENT);
        log.setBangTacDong(STUDENTS_TABLE);
        log.setIdBanGhi(studentId);
        log.setNoiDung(summary == null || summary.isBlank() ? "Cập nhật hồ sơ học sinh." : summary);
        log.setDiaChiIp(ipAddress);

        activityLogDAO.save(log);
    }

    @Transactional
    public void logStudentCreate(String studentId, String username, String summary, String ipAddress) {
        logStudentAction(studentId, username, summary, ipAddress, ACTION_CREATE_STUDENT, "Thêm học sinh mới.");
    }

    @Transactional
    public void logStudentDelete(String studentId, String username, String summary, String ipAddress) {
        logStudentAction(studentId, username, summary, ipAddress, ACTION_DELETE_STUDENT, "Xóa học sinh.");
    }

    private void logStudentAction(String studentId,
                                  String username,
                                  String summary,
                                  String ipAddress,
                                  String action,
                                  String fallbackSummary) {
        if (studentId == null || studentId.isBlank() || username == null || username.isBlank()) {
            return;
        }

        User actor = userDAO.findByTenDangNhap(username).orElse(null);
        if (actor == null || actor.getIdTaiKhoan() == null) {
            return;
        }

        ActivityLog log = new ActivityLog();
        log.setIdTaiKhoan(actor.getIdTaiKhoan());
        log.setHanhDong(action);
        log.setBangTacDong(STUDENTS_TABLE);
        log.setIdBanGhi(studentId);
        log.setNoiDung(summary == null || summary.isBlank() ? fallbackSummary : summary);
        log.setDiaChiIp(ipAddress);
        activityLogDAO.save(log);
    }

    @Transactional
    public void rebindStudentRecordId(String oldStudentId, String newStudentId) {
        if (oldStudentId == null || newStudentId == null || oldStudentId.equals(newStudentId)) {
            return;
        }

        activityLogDAO.rebindRecordId(STUDENTS_TABLE, oldStudentId, newStudentId);
    }

    @Transactional(readOnly = true)
    public List<ConductActivityItem> getRecentConductActivities(String q, int limit) {
        return getRecentConductActivities(q, null, limit);
    }

    @Transactional(readOnly = true)
    public List<ConductActivityItem> getRecentConductActivities(String q, String loai, int limit) {
        int resolvedLimit = limit <= 0 ? 10 : Math.min(limit, 50);
        String keyword = safeTrim(q);
        String normalizedLoai = normalizeConductType(loai);
        List<Object[]> rows = activityLogDAO.findRecentActivitiesByTable(
                CONDUCT_EVENTS_TABLE,
                keyword,
                normalizedLoai,
                resolvedLimit
        );
        List<ConductActivityItem> activities = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            String actorName = normalizeMojibake(asString(row, 0, "Hệ thống"));
            String actorRole = normalizeMojibake(asString(row, 1, "Tài khoản"));
            String actionCode = asString(row, 2, "");
            String detail = normalizeMojibake(asString(row, 3, ""));
            LocalDateTime actionTime = asLocalDateTime(row, 4);

            String displayText = safeTrim(detail);
            if (displayText == null) {
                displayText = defaultActionText(actionCode);
            }

            activities.add(new ConductActivityItem(
                    actorName,
                    actorRole,
                    displayText,
                    resolveKind(actionCode),
                    formatActivityTime(actionTime)
            ));
        }
        return activities;
    }

    @Transactional
    public void logConductCreated(String loai,
                                  Long eventId,
                                  String studentId,
                                  String studentName,
                                  String decisionNumber,
                                  String username,
                                  String ipAddress) {
        String actionCode = isReward(loai) ? ACTION_CREATE_REWARD : ACTION_CREATE_DISCIPLINE;
        String detail = "Đã tạo mới " + (isReward(loai) ? "Khen thưởng" : "Kỷ luật")
                + " cho học sinh " + resolveStudentText(studentId, studentName)
                + resolveDecisionText(decisionNumber) + ".";
        logConductAction(actionCode, eventId, detail, username, ipAddress);
    }

    @Transactional
    public void logConductUpdated(String loai,
                                  Long eventId,
                                  String studentId,
                                  String studentName,
                                  String decisionNumber,
                                  String username,
                                  String ipAddress) {
        String actionCode = isReward(loai) ? ACTION_UPDATE_REWARD : ACTION_UPDATE_DISCIPLINE;
        String detail = "Đã chỉnh sửa " + (isReward(loai) ? "Khen thưởng" : "Kỷ luật")
                + " của học sinh " + resolveStudentText(studentId, studentName)
                + resolveDecisionText(decisionNumber) + ".";
        logConductAction(actionCode, eventId, detail, username, ipAddress);
    }

    @Transactional
    public void logConductDeleted(String loai,
                                  Long eventId,
                                  String studentId,
                                  String studentName,
                                  String decisionNumber,
                                  String username,
                                  String ipAddress) {
        String actionCode = isReward(loai) ? ACTION_DELETE_REWARD : ACTION_DELETE_DISCIPLINE;
        String detail = "Đã xóa " + (isReward(loai) ? "Khen thưởng" : "Kỷ luật")
                + " của học sinh " + resolveStudentText(studentId, studentName)
                + resolveDecisionText(decisionNumber) + ".";
        logConductAction(actionCode, eventId, detail, username, ipAddress);
    }

    private void logConductAction(String actionCode,
                                  Long eventId,
                                  String detail,
                                  String username,
                                  String ipAddress) {
        String resolvedUsername = safeTrim(username);
        if (resolvedUsername == null) {
            return;
        }

        User actor = userDAO.findByTenDangNhap(resolvedUsername).orElse(null);
        if (actor == null || actor.getIdTaiKhoan() == null) {
            return;
        }

        ActivityLog log = new ActivityLog();
        log.setIdTaiKhoan(actor.getIdTaiKhoan());
        log.setHanhDong(actionCode);
        log.setBangTacDong(CONDUCT_EVENTS_TABLE);
        log.setIdBanGhi(eventId == null ? null : String.valueOf(eventId));
        log.setNoiDung(detail);
        log.setDiaChiIp(ipAddress);
        activityLogDAO.save(log);
    }

    private String resolveStudentText(String studentId, String studentName) {
        String name = safeTrim(studentName);
        String id = safeTrim(studentId);
        if (name != null && id != null) {
            return name + " (" + id + ")";
        }
        return firstNonBlank(name, firstNonBlank(id, "không rõ"));
    }

    private String resolveDecisionText(String decisionNumber) {
        String value = safeTrim(decisionNumber);
        if (value == null) {
            return "";
        }
        return " (Số quyết định: " + value + ")";
    }

    private boolean isReward(String loai) {
        return CONDUCT_TYPE_REWARD.equalsIgnoreCase(safeTrim(loai));
    }

    private String resolveKind(String actionCode) {
        String code = safeTrim(actionCode);
        if (code == null) {
            return "other";
        }
        String upper = code.toUpperCase(Locale.ROOT);
        if (upper.contains("KHEN_THUONG")) {
            return "reward";
        }
        if (upper.contains("KY_LUAT")) {
            return "discipline";
        }
        return "other";
    }

    private String defaultActionText(String actionCode) {
        String code = safeTrim(actionCode);
        if (code == null) {
            return "Đã cập nhật hoạt động.";
        }
        return switch (code.toUpperCase(Locale.ROOT)) {
            case ACTION_CREATE_REWARD -> "Đã tạo mới khen thưởng.";
            case ACTION_CREATE_DISCIPLINE -> "Đã tạo mới kỷ luật.";
            case ACTION_UPDATE_REWARD -> "Đã chỉnh sửa khen thưởng.";
            case ACTION_UPDATE_DISCIPLINE -> "Đã chỉnh sửa kỷ luật.";
            case ACTION_DELETE_REWARD -> "Đã xóa khen thưởng.";
            case ACTION_DELETE_DISCIPLINE -> "Đã xóa kỷ luật.";
            default -> "Đã cập nhật hoạt động.";
        };
    }

    private String formatActivityTime(LocalDateTime value) {
        if (value == null) {
            return "--:--";
        }
        LocalDate today = LocalDate.now();
        LocalDate date = value.toLocalDate();
        if (date.equals(today)) {
            return TIME_FORMATTER.format(value) + " • Hôm nay";
        }
        if (date.equals(today.minusDays(1))) {
            return TIME_FORMATTER.format(value) + " • Hôm qua";
        }
        return DATE_TIME_FORMATTER.format(value);
    }

    private LocalDateTime asLocalDateTime(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }
        Object value = row[index];
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        try {
            return LocalDateTime.parse(value.toString().trim().replace(" ", "T"));
        } catch (Exception ex) {
            return null;
        }
    }

    private String asString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        String value = row[index].toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private String firstNonBlank(String first, String second) {
        String firstTrimmed = safeTrim(first);
        if (firstTrimmed != null) {
            return firstTrimmed;
        }
        return safeTrim(second);
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeConductType(String loai) {
        String trimmed = safeTrim(loai);
        if (trimmed == null) {
            return null;
        }
        if (CONDUCT_TYPE_REWARD.equalsIgnoreCase(trimmed)) {
            return CONDUCT_TYPE_REWARD;
        }
        if (CONDUCT_TYPE_DISCIPLINE.equalsIgnoreCase(trimmed)) {
            return CONDUCT_TYPE_DISCIPLINE;
        }
        return null;
    }

    private String normalizeMojibake(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        if (!looksLikeMojibake(input)) {
            return input;
        }

        String best = input;
        best = pickBetter(best, decode(input, StandardCharsets.ISO_8859_1));
        best = pickBetter(best, decode(input, WINDOWS_1252));
        best = pickBetter(best, decode(best, StandardCharsets.ISO_8859_1));
        best = pickBetter(best, decode(best, WINDOWS_1252));
        return cleanupKnownCorruptedVietnamese(best);
    }

    private String decode(String input, Charset sourceCharset) {
        return new String(input.getBytes(sourceCharset), StandardCharsets.UTF_8);
    }

    private String pickBetter(String original, String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return original;
        }

        int originalScore = mojibakeScore(original) - vietnameseScore(original);
        int candidateScore = mojibakeScore(candidate) - vietnameseScore(candidate);
        if (candidateScore < originalScore) {
            return candidate;
        }

        return original;
    }

    private boolean looksLikeMojibake(String value) {
        return mojibakeScore(value) > 0;
    }

    private int mojibakeScore(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }

        int score = 0;
        for (String marker : MOJIBAKE_MARKERS) {
            score += countOccurrences(value, marker);
        }
        return score;
    }

    private int countOccurrences(String input, String marker) {
        if (input == null || marker == null || marker.isEmpty()) {
            return 0;
        }

        int count = 0;
        int from = 0;
        while (true) {
            int index = input.indexOf(marker, from);
            if (index < 0) {
                break;
            }
            count++;
            from = index + marker.length();
        }
        return count;
    }

    private int vietnameseScore(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int score = 0;
        for (char ch : value.toCharArray()) {
            if ("ăâđêôơưáàảãạấầẩẫậắằẳẵặéèẻẽẹếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵ".indexOf(Character.toLowerCase(ch)) >= 0) {
                score++;
            }
        }
        return score;
    }

    private String cleanupKnownCorruptedVietnamese(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String cleaned = value;

        cleaned = cleaned.replace("CÃ¡c thay Ä‘á»•i:", "Các thay đổi:");
        cleaned = cleaned.replace("C�c thay d?i:", "Các thay đổi:");
        cleaned = cleaned.replace("C?c thay ??i:", "Các thay đổi:");

        cleaned = cleaned.replace("MÃ£ khÃ³a", "Mã khóa");
        cleaned = cleaned.replace("TÃªn khÃ³a", "Tên khóa");

        cleaned = cleaned.replace("Ảnh h?c sinh: d?? cập nhật", "Ảnh học sinh: đã cập nhật");
        cleaned = cleaned.replace("Ảnh h?c sinh: d? c?p nh?t", "Ảnh học sinh: đã cập nhật");
        cleaned = cleaned.replace("?nh h?c sinh: d?? c?p nh?t", "Ảnh học sinh: đã cập nhật");

        cleaned = cleaned.replace("h?c", "học");
        cleaned = cleaned.replace("c?p nh?t", "cập nhật");
        cleaned = cleaned.replace("d? ", "đã ");
        cleaned = cleaned.replace(" d?? ", " đã ");
        cleaned = cleaned.replace("kh?ng", "không");
        cleaned = cleaned.replace("khóa", "khóa");
        cleaned = cleaned.replace("mã", "mã");

        return cleaned;
    }

    public static class ConductActivityItem {
        private final String actorName;
        private final String actorRole;
        private final String actionDetail;
        private final String actionKind;
        private final String actionTime;

        public ConductActivityItem(String actorName,
                                   String actorRole,
                                   String actionDetail,
                                   String actionKind,
                                   String actionTime) {
            this.actorName = actorName;
            this.actorRole = actorRole;
            this.actionDetail = actionDetail;
            this.actionKind = actionKind;
            this.actionTime = actionTime;
        }

        public String getActorName() {
            return actorName;
        }

        public String getActorRole() {
            return actorRole;
        }

        public String getActionDetail() {
            return actionDetail;
        }

        public String getActionKind() {
            return actionKind;
        }

        public String getActionTime() {
            return actionTime;
        }
    }
}

