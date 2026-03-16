package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.entity.ScoreSearch;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class ScoreManagementService {

    private final ScoreQueryService queryService;

    public ScoreManagementService(ScoreQueryService queryService) {
        this.queryService = queryService;
    }

    public ScorePageResult search(ScoreSearch search) {
        return queryService.search(search);
    }

    public ScoreStats getStats() {
        return queryService.getStats();
    }

    public List<String> getGrades() {
        return queryService.getGrades();
    }

    public List<FilterOption> getClasses() {
        return queryService.getClasses();
    }

    public List<FilterOption> getSubjects() {
        return queryService.getSubjects();
    }

    public List<FilterOption> getCourses() {
        return queryService.getCourses();
    }

    public static class ScoreRow {
        private final String idHocSinh;
        private final String tenHocSinh;
        private final String tenLop;
        private final String tenMon;
        private final Double diemMieng;
        private final Double diem15Phut;
        private final Double diem1Tiet;
        private final Double diemGiuaKy;
        private final Double diemCuoiKy;
        private final Double tongKet;
        private final String hanhKiem;
        private final Integer hocKy;
        private final String namHoc;

        public ScoreRow(String idHocSinh,
                        String tenHocSinh,
                        String tenLop,
                        String tenMon,
                        Double diemMieng,
                        Double diem15Phut,
                        Double diem1Tiet,
                        Double diemGiuaKy,
                        Double diemCuoiKy,
                        Double tongKet,
                        String hanhKiem,
                        Integer hocKy,
                        String namHoc) {
            this.idHocSinh = idHocSinh;
            this.tenHocSinh = tenHocSinh;
            this.tenLop = tenLop;
            this.tenMon = tenMon;
            this.diemMieng = diemMieng;
            this.diem15Phut = diem15Phut;
            this.diem1Tiet = diem1Tiet;
            this.diemGiuaKy = diemGiuaKy;
            this.diemCuoiKy = diemCuoiKy;
            this.tongKet = tongKet;
            this.hanhKiem = hanhKiem;
            this.hocKy = hocKy;
            this.namHoc = namHoc;
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

        public String getTenMon() {
            return tenMon;
        }

        public String getDiemMiengDisplay() {
            return formatScore(diemMieng);
        }

        public String getDiem15PhutDisplay() {
            return formatScore(diem15Phut);
        }

        public String getDiem1TietDisplay() {
            return formatScore(diem1Tiet);
        }

        public String getDiemGiuaKyDisplay() {
            return formatScore(diemGiuaKy);
        }

        public String getDiemCuoiKyDisplay() {
            return formatScore(diemCuoiKy);
        }

        public String getTongKetDisplay() {
            return formatScore(tongKet);
        }

        public String getHanhKiem() {
            return hanhKiem;
        }

        public String getHanhKiemBadgeClass() {
            String normalized = normalizeAsciiLower(hanhKiem);
            if (normalized.contains("gioi") || normalized.contains("tot")) {
                return "hk-good";
            }
            if (normalized.contains("kha")) {
                return "hk-fair";
            }
            if (normalized.contains("trung")) {
                return "hk-average";
            }
            if (normalized.contains("yeu") || normalized.contains("kem")) {
                return "hk-weak";
            }
            return "hk-default";
        }

        public String getHocKyDisplay() {
            if (hocKy == null) {
                return "-";
            }
            if (hocKy == 1) {
                return "H\u1ecdc k\u1ef3 1";
            }
            if (hocKy == 2) {
                return "H\u1ecdc k\u1ef3 2";
            }
            return "H\u1ecdc k\u1ef3 " + hocKy;
        }

        public String getNamHocDisplay() {
            if (namHoc == null || namHoc.isBlank()) {
                return "-";
            }
            return namHoc;
        }

        private String formatScore(Double value) {
            if (value == null) {
                return "-";
            }

            BigDecimal number = BigDecimal.valueOf(value)
                    .setScale(1, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            return number.toPlainString();
        }

        private String normalizeAsciiLower(String value) {
            if (value == null) {
                return "";
            }
            String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
            String ascii = decomposed.replaceAll("\\p{M}+", "");
            return ascii.toLowerCase(Locale.ROOT);
        }
    }

    public static class ScorePageResult {
        private final List<ScoreRow> items;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final int fromRecord;
        private final int toRecord;

        public ScorePageResult(List<ScoreRow> items,
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

        public List<ScoreRow> getItems() {
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

    public static class ScoreStats {
        private final long totalStudentsWithScores;
        private final double schoolAverage;
        private final double goodRate;

        public ScoreStats(long totalStudentsWithScores,
                          double schoolAverage,
                          double goodRate) {
            this.totalStudentsWithScores = totalStudentsWithScores;
            this.schoolAverage = schoolAverage;
            this.goodRate = goodRate;
        }

        public long getTotalStudentsWithScores() {
            return totalStudentsWithScores;
        }

        public String getSchoolAverageDisplay() {
            return String.format(Locale.US, "%.1f", schoolAverage);
        }

        public String getGoodRateDisplay() {
            return String.format(Locale.US, "%.1f%%", goodRate);
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
