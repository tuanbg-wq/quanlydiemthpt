package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ConductDAO;
import com.quanly.webdiem.model.search.ConductSearch;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ConductManagementService {

    public static final String LOAI_KHEN_THUONG = "KHEN_THUONG";
    public static final String LOAI_KY_LUAT = "KY_LUAT";
    private static final int PAGE_SIZE = 6;

    private static final String CREATE_CONDUCT_EVENT_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS conduct_events (
                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                id_hoc_sinh VARCHAR(10) NOT NULL,
                loai ENUM('KHEN_THUONG','KY_LUAT') NOT NULL,
                loai_chi_tiet VARCHAR(120) NULL,
                so_quyet_dinh VARCHAR(120) NULL,
                noi_dung TEXT NOT NULL,
                ngay_ban_hanh DATE NULL,
                ghi_chu TEXT NULL,
                nam_hoc VARCHAR(20) NULL,
                hoc_ky INT NULL,
                ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                ngay_cap_nhat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_conduct_events_student (id_hoc_sinh),
                INDEX idx_conduct_events_type (loai),
                INDEX idx_conduct_events_date (ngay_ban_hanh)
            )
            """;

    private final ConductDAO conductDAO;
    private final JdbcTemplate jdbcTemplate;
    private final Object schemaLock = new Object();
    private volatile boolean schemaReady;

    public ConductManagementService(ConductDAO conductDAO, JdbcTemplate jdbcTemplate) {
        this.conductDAO = conductDAO;
        this.jdbcTemplate = jdbcTemplate;
    }

    public ConductPageResult search(ConductSearch search) {
        List<ConductRow> rows = findRowsBySearch(search);
        int requestedPage = normalizePage(search == null ? null : search.getPage());
        return paginate(rows, requestedPage);
    }

    public ConductStats getStats(ConductSearch search) {
        List<ConductRow> rows = findRowsBySearch(search);
        long totalReward = rows.stream().filter(ConductRow::isKhenThuong).count();
        long totalDiscipline = rows.stream().filter(item -> !item.isKhenThuong()).count();
        long total = totalReward + totalDiscipline;
        double rewardRate = total == 0 ? 0.0 : (totalReward * 100.0 / total);
        double disciplineRate = total == 0 ? 0.0 : (totalDiscipline * 100.0 / total);
        return new ConductStats(totalReward, totalDiscipline, total, rewardRate, disciplineRate);
    }

    public List<String> getGrades() {
        List<String> grades = new ArrayList<>(conductDAO.findDistinctGrades().stream().map(String::valueOf).toList());
        if (!grades.contains("10")) {
            grades.add("10");
        }
        if (!grades.contains("11")) {
            grades.add("11");
        }
        if (!grades.contains("12")) {
            grades.add("12");
        }
        return grades.stream()
                .distinct()
                .sorted((left, right) -> {
                    Integer leftValue = parseInteger(left);
                    Integer rightValue = parseInteger(right);
                    if (leftValue != null && rightValue != null) {
                        return Integer.compare(leftValue, rightValue);
                    }
                    if (leftValue != null) {
                        return -1;
                    }
                    if (rightValue != null) {
                        return 1;
                    }
                    return left.compareTo(right);
                })
                .toList();
    }

    public List<FilterOption> getClasses() {
        return conductDAO.findDistinctClassesForFilter().stream()
                .map(this::mapClassOption)
                .toList();
    }

    public List<FilterOption> getCourses() {
        return conductDAO.findDistinctCoursesForFilter().stream()
                .map(this::mapCourseOption)
                .toList();
    }

    public List<ConductRow> getRowsForExport(ConductSearch search) {
        List<ConductRow> rows = new ArrayList<>(findRowsBySearch(search));
        rows.sort(buildExportComparator());
        return rows;
    }

    public ConductRewardCreatePageData getRewardCreatePageData(ConductRewardCreateFilter filter) {
        ensureSchemaReady();
        ConductRewardCreateFilter resolvedFilter = filter == null ? new ConductRewardCreateFilter() : filter;
        Integer khoi = parseInteger(resolvedFilter.getKhoi());
        String classId = normalize(resolvedFilter.getLop());
        String courseId = normalize(resolvedFilter.getKhoa());
        String q = normalize(resolvedFilter.getQ());
        List<ConductStudentCandidate> candidates = conductDAO.findStudentsForRewardForm(khoi, classId, courseId, q).stream()
                .map(this::mapStudentCandidate)
                .toList();

        ConductStudentCandidate selectedStudent = null;
        String studentId = safeTrim(resolvedFilter.getStudentId());
        if (studentId != null) {
            selectedStudent = conductDAO.findStudentSnapshot(studentId).stream()
                    .findFirst()
                    .map(this::mapStudentCandidate)
                    .orElse(null);
        }

        return new ConductRewardCreatePageData(
                resolvedFilter,
                getGrades(),
                getClasses(),
                getCourses(),
                candidates,
                selectedStudent
        );
    }

    public List<ConductStudentCandidate> suggestStudentsForReward(String q,
                                                                  String khoi,
                                                                  String lop,
                                                                  String khoa) {
        ensureSchemaReady();
        if (safeTrim(q) == null) {
            return List.of();
        }
        Integer resolvedKhoi = parseInteger(khoi);
        String resolvedLop = normalize(lop);
        String resolvedKhoa = normalize(khoa);
        String resolvedQ = normalize(q);
        return conductDAO.findStudentsForRewardForm(resolvedKhoi, resolvedLop, resolvedKhoa, resolvedQ).stream()
                .map(this::mapStudentCandidate)
                .limit(15)
                .toList();
    }

    public void createReward(ConductRewardCreateRequest request) {
        ensureSchemaReady();
        String studentId = safeTrim(request == null ? null : request.getStudentId());
        if (studentId == null) {
            throw new RuntimeException("Vui lòng chọn học sinh trước khi lưu.");
        }
        String noiDung = safeTrim(request.getNoiDung());
        if (noiDung == null) {
            throw new RuntimeException("Nội dung khen thưởng không được để trống.");
        }
        String ngayBanHanh = safeTrim(request.getNgayBanHanh());
        if (ngayBanHanh == null) {
            throw new RuntimeException("Vui lòng chọn ngày ban hành.");
        }
        String loaiChiTiet = firstNonBlank(request.getLoaiChiTiet(), "Khác");
        String soQuyetDinh = safeTrim(request.getSoQuyetDinh());
        String ghiChu = safeTrim(request.getGhiChu());
        String namHoc = firstNonBlank(request.getNamHoc(), defaultSchoolYear());
        Integer hocKy = request.getHocKy() == null ? 0 : request.getHocKy();

        int inserted = conductDAO.insertEvent(
                studentId,
                LOAI_KHEN_THUONG,
                loaiChiTiet,
                soQuyetDinh,
                noiDung,
                ngayBanHanh,
                ghiChu,
                namHoc,
                hocKy
        );
        if (inserted <= 0) {
            throw new RuntimeException("Không thể lưu dữ liệu khen thưởng.");
        }
    }

    public ConductRow getEventDetail(Long eventId) {
        ensureSchemaReady();
        if (eventId == null || eventId <= 0) {
            throw new RuntimeException("Không tìm thấy bản ghi.");
        }
        return conductDAO.findEventDetail(eventId).stream()
                .findFirst()
                .map(this::mapRow)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi."));
    }

    public ConductEventUpsertRequest getEditData(Long eventId) {
        ConductRow row = getEventDetail(eventId);
        ConductEventUpsertRequest request = new ConductEventUpsertRequest();
        request.setEventId(row.getEventId());
        request.setStudentId(row.getIdHocSinh());
        request.setLoai(row.getLoai());
        request.setLoaiChiTiet(row.getLoaiChiTiet());
        request.setSoQuyetDinh(row.getSoQuyetDinh());
        request.setNgayBanHanh(row.getNgayBanHanhIso());
        request.setNoiDung(row.getNoiDungChiTiet());
        request.setGhiChu(row.getGhiChu());
        request.setNamHoc(row.getNamHoc());
        request.setHocKy(row.getHocKy());
        return request;
    }

    public void updateEvent(ConductEventUpsertRequest request) {
        ensureSchemaReady();
        Long eventId = request == null ? null : request.getEventId();
        if (eventId == null || eventId <= 0) {
            throw new RuntimeException("Thiếu thông tin bản ghi để cập nhật.");
        }
        String loai = normalizeLoai(request.getLoai());
        String loaiChiTiet = firstNonBlank(request.getLoaiChiTiet(), "Khác");
        String noiDung = safeTrim(request.getNoiDung());
        if (noiDung == null) {
            throw new RuntimeException("Nội dung không được để trống.");
        }
        String ngayBanHanh = safeTrim(request.getNgayBanHanh());
        if (ngayBanHanh == null) {
            throw new RuntimeException("Vui lòng chọn ngày ban hành.");
        }
        int updated = conductDAO.updateEvent(
                eventId,
                loai,
                loaiChiTiet,
                safeTrim(request.getSoQuyetDinh()),
                noiDung,
                ngayBanHanh,
                safeTrim(request.getGhiChu()),
                safeTrim(request.getNamHoc()),
                request.getHocKy() == null ? 0 : request.getHocKy()
        );
        if (updated <= 0) {
            throw new RuntimeException("Không thể cập nhật bản ghi.");
        }
    }

    public void deleteEvent(Long eventId) {
        ensureSchemaReady();
        if (eventId == null || eventId <= 0) {
            throw new RuntimeException("Thiếu mã bản ghi để xóa.");
        }
        int deleted = conductDAO.deleteEvent(eventId);
        if (deleted <= 0) {
            throw new RuntimeException("Không thể xóa bản ghi hoặc dữ liệu không còn tồn tại.");
        }
    }

    private List<ConductRow> findRowsBySearch(ConductSearch search) {
        ensureSchemaReady();
        String q = normalize(search == null ? null : search.getQ());
        Integer khoi = parseInteger(search == null ? null : search.getKhoi());
        String lop = normalize(search == null ? null : search.getLop());
        String khoa = normalize(search == null ? null : search.getKhoa());
        return conductDAO.searchEventsForManagement(q, khoi, lop, khoa).stream()
                .map(this::mapRow)
                .toList();
    }

    private ConductRow mapRow(Object[] row) {
        return new ConductRow(
                asLong(row, 0, null),
                asString(row, 1, "-"),
                asString(row, 2, "-"),
                asString(row, 3, "-"),
                asString(row, 4, ""),
                asString(row, 5, ""),
                asString(row, 6, "-"),
                normalizeLoai(asString(row, 7, LOAI_KY_LUAT)),
                asString(row, 8, ""),
                asString(row, 9, ""),
                asString(row, 10, ""),
                asString(row, 11, ""),
                asString(row, 12, "-"),
                asString(row, 13, ""),
                asInteger(row, 14, 0)
        );
    }

    private ConductStudentCandidate mapStudentCandidate(Object[] row) {
        return new ConductStudentCandidate(
                asString(row, 0, ""),
                asString(row, 1, ""),
                asString(row, 2, ""),
                asString(row, 3, ""),
                asString(row, 4, ""),
                asString(row, 5, ""),
                asString(row, 6, "")
        );
    }

    private ConductPageResult paginate(List<ConductRow> rows, int requestedPage) {
        int totalItems = rows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        int fromIndex = Math.max(0, (page - 1) * PAGE_SIZE);
        int toIndex = Math.min(totalItems, fromIndex + PAGE_SIZE);

        List<ConductRow> items = totalItems == 0 ? Collections.emptyList() : rows.subList(fromIndex, toIndex);
        int fromRecord = totalItems == 0 ? 0 : fromIndex + 1;
        int toRecord = totalItems == 0 ? 0 : toIndex;
        return new ConductPageResult(items, page, totalPages, totalItems, fromRecord, toRecord);
    }

    private Comparator<ConductRow> buildExportComparator() {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        collator.setStrength(Collator.PRIMARY);
        return (left, right) -> {
            int byDate = safeText(right == null ? null : right.getNgayBanHanhIso())
                    .compareTo(safeText(left == null ? null : left.getNgayBanHanhIso()));
            if (byDate != 0) {
                return byDate;
            }
            int byClass = collator.compare(safeText(left == null ? null : left.getTenLop()), safeText(right == null ? null : right.getTenLop()));
            if (byClass != 0) {
                return byClass;
            }
            return collator.compare(safeText(left == null ? null : left.getTenHocSinh()), safeText(right == null ? null : right.getTenHocSinh()));
        };
    }

    private FilterOption mapClassOption(Object[] row) {
        String id = asString(row, 0, "");
        String tenLop = asString(row, 1, id);
        String khoi = asString(row, 2, "");
        String label = khoi.isBlank() ? tenLop : tenLop + " (Khối " + khoi + ")";
        return new FilterOption(id, label);
    }

    private FilterOption mapCourseOption(Object[] row) {
        String id = asString(row, 0, "");
        String name = asString(row, 1, id);
        return new FilterOption(id, id + " (" + name + ")");
    }

    private String normalizeLoai(String value) {
        String trimmed = safeTrim(value);
        if (trimmed == null) {
            return LOAI_KY_LUAT;
        }
        if (LOAI_KHEN_THUONG.equalsIgnoreCase(trimmed)) {
            return LOAI_KHEN_THUONG;
        }
        if (LOAI_KY_LUAT.equalsIgnoreCase(trimmed)) {
            return LOAI_KY_LUAT;
        }
        return LOAI_KY_LUAT;
    }

    private String normalize(String value) {
        String trimmed = safeTrim(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private String firstNonBlank(String first, String fallback) {
        String trimmed = safeTrim(first);
        return trimmed != null ? trimmed : fallback;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private String defaultSchoolYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        if (now.getMonthValue() >= 8) {
            return year + "-" + (year + 1);
        }
        return (year - 1) + "-" + year;
    }

    private String asString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        String value = row[index].toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private Integer asInteger(Object[] row, int index, Integer fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        Object value = row[index];
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private Long asLong(Object[] row, int index, Long fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        Object value = row[index];
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private void ensureSchemaReady() {
        if (schemaReady) {
            return;
        }
        synchronized (schemaLock) {
            if (schemaReady) {
                return;
            }
            jdbcTemplate.execute(CREATE_CONDUCT_EVENT_TABLE_SQL);
            schemaReady = true;
        }
    }

    public static class ConductRow {
        private final Long eventId;
        private final String idHocSinh;
        private final String tenHocSinh;
        private final String tenLop;
        private final String khoi;
        private final String idKhoa;
        private final String khoaHoc;
        private final String loai;
        private final String loaiChiTiet;
        private final String soQuyetDinh;
        private final String noiDungChiTiet;
        private final String ghiChu;
        private final String ngayBanHanh;
        private final String namHoc;
        private final Integer hocKy;

        public ConductRow(Long eventId, String idHocSinh, String tenHocSinh, String tenLop, String khoi,
                          String idKhoa, String khoaHoc, String loai, String loaiChiTiet, String soQuyetDinh,
                          String noiDungChiTiet, String ghiChu, String ngayBanHanh, String namHoc, Integer hocKy) {
            this.eventId = eventId;
            this.idHocSinh = idHocSinh;
            this.tenHocSinh = tenHocSinh;
            this.tenLop = tenLop;
            this.khoi = khoi;
            this.idKhoa = idKhoa;
            this.khoaHoc = khoaHoc;
            this.loai = loai;
            this.loaiChiTiet = loaiChiTiet;
            this.soQuyetDinh = soQuyetDinh;
            this.noiDungChiTiet = noiDungChiTiet;
            this.ghiChu = ghiChu;
            this.ngayBanHanh = ngayBanHanh;
            this.namHoc = namHoc;
            this.hocKy = hocKy;
        }

        public Long getEventId() { return eventId; }
        public String getIdHocSinh() { return idHocSinh; }
        public String getTenHocSinh() { return tenHocSinh; }
        public String getTenLop() { return tenLop; }
        public String getKhoi() { return khoi; }
        public String getIdKhoa() { return idKhoa; }
        public String getKhoaHoc() { return khoaHoc; }
        public String getLoai() { return loai; }
        public String getLoaiChiTiet() { return loaiChiTiet; }
        public String getSoQuyetDinh() { return soQuyetDinh; }
        public String getNoiDungChiTiet() { return noiDungChiTiet; }
        public String getGhiChu() { return ghiChu; }
        public String getNgayBanHanh() { return ngayBanHanh; }
        public String getNamHoc() { return namHoc; }
        public Integer getHocKy() { return hocKy; }

        public boolean isKhenThuong() { return LOAI_KHEN_THUONG.equals(loai); }

        public String getLoaiDisplay() {
            return isKhenThuong() ? "Khen thưởng" : "Kỷ luật";
        }

        public String getLoaiBadgeClass() {
            return isKhenThuong() ? "badge-khen" : "badge-ky-luat";
        }

        public String getHocKyDisplay() {
            if (hocKy == null) return "-";
            if (hocKy == 0) return "Cả năm";
            if (hocKy == 1) return "Học kỳ I";
            if (hocKy == 2) return "Học kỳ II";
            return "Học kỳ " + hocKy;
        }

        public String getNgayBanHanhIso() {
            if (ngayBanHanh == null || ngayBanHanh.isBlank() || ngayBanHanh.length() != 10) {
                return "";
            }
            String[] parts = ngayBanHanh.split("/");
            if (parts.length != 3) {
                return "";
            }
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        }
    }

    public static class ConductPageResult {
        private final List<ConductRow> items;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final int fromRecord;
        private final int toRecord;

        public ConductPageResult(List<ConductRow> items, int page, int totalPages, int totalItems, int fromRecord, int toRecord) {
            this.items = items;
            this.page = page;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.fromRecord = fromRecord;
            this.toRecord = toRecord;
        }

        public List<ConductRow> getItems() { return items; }
        public int getPage() { return page; }
        public int getTotalPages() { return totalPages; }
        public int getTotalItems() { return totalItems; }
        public int getFromRecord() { return fromRecord; }
        public int getToRecord() { return toRecord; }
    }

    public static class ConductStats {
        private final long totalReward;
        private final long totalDiscipline;
        private final long totalRecords;
        private final double rewardRate;
        private final double disciplineRate;

        public ConductStats(long totalReward, long totalDiscipline, long totalRecords, double rewardRate, double disciplineRate) {
            this.totalReward = totalReward;
            this.totalDiscipline = totalDiscipline;
            this.totalRecords = totalRecords;
            this.rewardRate = rewardRate;
            this.disciplineRate = disciplineRate;
        }

        public long getTotalReward() { return totalReward; }
        public long getTotalDiscipline() { return totalDiscipline; }
        public long getTotalRecords() { return totalRecords; }
        public String getRewardRateDisplay() { return String.format(Locale.US, "%.1f%%", rewardRate); }
        public String getDisciplineRateDisplay() { return String.format(Locale.US, "%.1f%%", disciplineRate); }
        public String getRewardRateValue() { return String.format(Locale.US, "%.2f", rewardRate); }
        public String getDisciplineRateValue() { return String.format(Locale.US, "%.2f", disciplineRate); }
    }

    public static class FilterOption {
        private final String id;
        private final String name;

        public FilterOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
    }
}
