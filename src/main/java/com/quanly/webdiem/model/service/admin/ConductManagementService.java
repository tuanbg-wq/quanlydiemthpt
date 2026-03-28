package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ConductDAO;
import com.quanly.webdiem.model.search.ConductSearch;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ConductManagementService {

    private static final int PAGE_SIZE = 6;

    private final ConductDAO conductDAO;

    public ConductManagementService(ConductDAO conductDAO) {
        this.conductDAO = conductDAO;
    }

    public ConductPageResult search(ConductSearch search) {
        List<ConductRow> rows = findRowsBySearch(search);
        int requestedPage = normalizePage(search == null ? null : search.getPage());
        return paginate(rows, requestedPage);
    }

    public ConductStats getStats(ConductSearch search) {
        List<ConductRow> rows = findRowsBySearch(search);
        long totalReward = rows.stream()
                .filter(ConductRow::isKhenThuong)
                .count();
        long totalDiscipline = rows.stream()
                .filter(row -> !row.isKhenThuong())
                .count();
        long total = totalReward + totalDiscipline;
        double rewardRate = total == 0 ? 0.0 : (totalReward * 100.0 / total);
        double disciplineRate = total == 0 ? 0.0 : (totalDiscipline * 100.0 / total);
        return new ConductStats(totalReward, totalDiscipline, total, rewardRate, disciplineRate);
    }

    public List<String> getGrades() {
        List<String> grades = new ArrayList<>(conductDAO.findDistinctGrades().stream()
                .map(String::valueOf)
                .toList());
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

    private List<ConductRow> findRowsBySearch(ConductSearch search) {
        String q = normalize(search == null ? null : search.getQ());
        Integer khoi = parseInteger(search == null ? null : search.getKhoi());
        String lop = normalize(search == null ? null : search.getLop());
        String khoa = normalize(search == null ? null : search.getKhoa());
        return conductDAO.searchForManagement(q, khoi, lop, khoa).stream()
                .map(this::mapRow)
                .toList();
    }

    private ConductRow mapRow(Object[] row) {
        String xepLoai = asString(row, 6, "");
        String nhanXet = asString(row, 7, "");
        String loai = classifyLoai(xepLoai, nhanXet);
        return new ConductRow(
                asString(row, 0, "-"),
                asString(row, 1, "-"),
                asString(row, 2, "-"),
                asString(row, 3, ""),
                asString(row, 4, ""),
                asString(row, 5, "-"),
                loai,
                resolveContent(loai, xepLoai, nhanXet),
                asString(row, 8, "-"),
                asString(row, 9, "-"),
                asInteger(row, 10, null)
        );
    }

    private ConductPageResult paginate(List<ConductRow> rows, int requestedPage) {
        int totalItems = rows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        int fromIndex = Math.max(0, (page - 1) * PAGE_SIZE);
        int toIndex = Math.min(totalItems, fromIndex + PAGE_SIZE);

        List<ConductRow> items = totalItems == 0
                ? Collections.emptyList()
                : rows.subList(fromIndex, toIndex);

        int fromRecord = totalItems == 0 ? 0 : fromIndex + 1;
        int toRecord = totalItems == 0 ? 0 : toIndex;

        return new ConductPageResult(items, page, totalPages, totalItems, fromRecord, toRecord);
    }

    private Comparator<ConductRow> buildExportComparator() {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        collator.setStrength(Collator.PRIMARY);
        return (left, right) -> {
            int byClass = collator.compare(
                    safeText(left == null ? null : left.getTenLop()),
                    safeText(right == null ? null : right.getTenLop())
            );
            if (byClass != 0) {
                return byClass;
            }
            int byName = collator.compare(
                    safeText(left == null ? null : left.getTenHocSinh()),
                    safeText(right == null ? null : right.getTenHocSinh())
            );
            if (byName != 0) {
                return byName;
            }
            return collator.compare(
                    safeText(left == null ? null : left.getIdHocSinh()),
                    safeText(right == null ? null : right.getIdHocSinh())
            );
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

    private String classifyLoai(String xepLoai, String nhanXet) {
        String normalized = normalizeAsciiLower(xepLoai);
        if (normalized.contains("tot")
                || normalized.contains("kha")
                || normalized.contains("gioi")
                || normalized.contains("xuat sac")) {
            return "KHEN_THUONG";
        }
        if (normalized.contains("yeu")
                || normalized.contains("kem")
                || normalized.contains("vi pham")
                || normalized.contains("ky luat")
                || normalized.contains("trung binh")) {
            return "KY_LUAT";
        }

        String normalizedComment = normalizeAsciiLower(nhanXet);
        if (normalizedComment.contains("khen")
                || normalizedComment.contains("thuong")
                || normalizedComment.contains("tich cuc")
                || normalizedComment.contains("guong mau")) {
            return "KHEN_THUONG";
        }
        if (normalizedComment.contains("ky luat")
                || normalizedComment.contains("vi pham")
                || normalizedComment.contains("nhac nho")) {
            return "KY_LUAT";
        }
        return "KY_LUAT";
    }

    private String resolveContent(String loai, String xepLoai, String nhanXet) {
        String comment = safeTrim(nhanXet);
        if (comment != null) {
            return comment;
        }

        String conductLabel = safeTrim(xepLoai);
        if (conductLabel != null) {
            if ("KHEN_THUONG".equals(loai)) {
                return "Đánh giá rèn luyện tốt (" + conductLabel + ").";
            }
            return "Cần chấn chỉnh rèn luyện (" + conductLabel + ").";
        }
        if ("KHEN_THUONG".equals(loai)) {
            return "Học sinh có biểu hiện rèn luyện tích cực.";
        }
        return "Học sinh cần theo dõi thêm về rèn luyện.";
    }

    private String asString(Object[] row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return fallback;
        }
        String value = row[index].toString().trim();
        if (value.isEmpty()) {
            return fallback;
        }
        return value;
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

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
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

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeAsciiLower(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        String ascii = decomposed.replaceAll("\\p{M}+", "");
        return ascii.toLowerCase(Locale.ROOT);
    }

    public static class ConductRow {
        private final String idHocSinh;
        private final String tenHocSinh;
        private final String tenLop;
        private final String khoi;
        private final String idKhoa;
        private final String khoaHoc;
        private final String loai;
        private final String noiDungChiTiet;
        private final String ngayQuyetDinh;
        private final String namHoc;
        private final Integer hocKy;

        public ConductRow(String idHocSinh,
                          String tenHocSinh,
                          String tenLop,
                          String khoi,
                          String idKhoa,
                          String khoaHoc,
                          String loai,
                          String noiDungChiTiet,
                          String ngayQuyetDinh,
                          String namHoc,
                          Integer hocKy) {
            this.idHocSinh = idHocSinh;
            this.tenHocSinh = tenHocSinh;
            this.tenLop = tenLop;
            this.khoi = khoi;
            this.idKhoa = idKhoa;
            this.khoaHoc = khoaHoc;
            this.loai = loai;
            this.noiDungChiTiet = noiDungChiTiet;
            this.ngayQuyetDinh = ngayQuyetDinh;
            this.namHoc = namHoc;
            this.hocKy = hocKy;
        }

        public String getIdHocSinh() {
            return idHocSinh;
        }

        public String getTenHocSinh() {
            return tenHocSinh;
        }

        public String getTenLop() {
            return tenLop;
        }

        public String getKhoi() {
            return khoi;
        }

        public String getIdKhoa() {
            return idKhoa;
        }

        public String getKhoaHoc() {
            return khoaHoc;
        }

        public String getLoai() {
            return loai;
        }

        public String getLoaiDisplay() {
            if ("KHEN_THUONG".equals(loai)) {
                return "Khen thưởng";
            }
            return "Kỷ luật";
        }

        public boolean isKhenThuong() {
            return "KHEN_THUONG".equals(loai);
        }

        public String getLoaiBadgeClass() {
            return isKhenThuong() ? "badge-khen" : "badge-ky-luat";
        }

        public String getNoiDungChiTiet() {
            return noiDungChiTiet;
        }

        public String getNgayQuyetDinh() {
            return ngayQuyetDinh;
        }

        public String getNamHoc() {
            return namHoc;
        }

        public Integer getHocKy() {
            return hocKy;
        }

        public String getHocKyDisplay() {
            if (hocKy == null) {
                return "-";
            }
            if (hocKy == 0) {
                return "Cả năm";
            }
            if (hocKy == 1) {
                return "Học kỳ I";
            }
            if (hocKy == 2) {
                return "Học kỳ II";
            }
            return "Học kỳ " + hocKy;
        }
    }

    public static class ConductPageResult {
        private final List<ConductRow> items;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final int fromRecord;
        private final int toRecord;

        public ConductPageResult(List<ConductRow> items,
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

        public List<ConductRow> getItems() {
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

    public static class ConductStats {
        private final long totalReward;
        private final long totalDiscipline;
        private final long totalRecords;
        private final double rewardRate;
        private final double disciplineRate;

        public ConductStats(long totalReward,
                            long totalDiscipline,
                            long totalRecords,
                            double rewardRate,
                            double disciplineRate) {
            this.totalReward = totalReward;
            this.totalDiscipline = totalDiscipline;
            this.totalRecords = totalRecords;
            this.rewardRate = rewardRate;
            this.disciplineRate = disciplineRate;
        }

        public long getTotalReward() {
            return totalReward;
        }

        public long getTotalDiscipline() {
            return totalDiscipline;
        }

        public long getTotalRecords() {
            return totalRecords;
        }

        public String getRewardRateDisplay() {
            return String.format(Locale.US, "%.1f%%", rewardRate);
        }

        public String getDisciplineRateDisplay() {
            return String.format(Locale.US, "%.1f%%", disciplineRate);
        }

        public String getRewardRateValue() {
            return String.format(Locale.US, "%.2f", rewardRate);
        }

        public String getDisciplineRateValue() {
            return String.format(Locale.US, "%.2f", disciplineRate);
        }
    }

    public static class FilterOption {
        private final String id;
        private final String name;

        public FilterOption(String id, String name) {
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
}
