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
    private final ScoreUpdateService updateService;
    private final ScoreDeleteService deleteService;

    public ScoreManagementService(ScoreQueryService queryService,
                                  ScoreUpdateService updateService,
                                  ScoreDeleteService deleteService) {
        this.queryService = queryService;
        this.updateService = updateService;
        this.deleteService = deleteService;
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

    public ScoreGroupSummary getScoreGroupSummary(String studentId, String subjectId, String namHoc) {
        return queryService.getScoreGroupSummary(studentId, subjectId, namHoc);
    }

    public List<ScoreEntry> getScoreEntries(String studentId, String subjectId, String namHoc) {
        return queryService.getScoreEntries(studentId, subjectId, namHoc);
    }

    public void updateScoreEntries(String studentId,
                                   String subjectId,
                                   String namHoc,
                                   List<ScoreEntryUpdate> updates) {
        updateService.updateScoreEntries(studentId, subjectId, namHoc, updates);
    }

    public void deleteScoreGroup(String studentId, String subjectId, String namHoc) {
        deleteService.deleteScoreGroup(studentId, subjectId, namHoc);
    }

    public static class ScoreRow {
        private final String idHocSinh;
        private final String tenHocSinh;
        private final String tenLop;
        private final String idMon;
        private final String tenMon;
        private final Double diemGiuaKy;
        private final Double diemCuoiKy;
        private final Double tongKet;
        private final String hanhKiem;
        private final String namHoc;

        public ScoreRow(String idHocSinh,
                        String tenHocSinh,
                        String tenLop,
                        String idMon,
                        String tenMon,
                        Double diemGiuaKy,
                        Double diemCuoiKy,
                        Double tongKet,
                        String hanhKiem,
                        String namHoc) {
            this.idHocSinh = idHocSinh;
            this.tenHocSinh = tenHocSinh;
            this.tenLop = tenLop;
            this.idMon = idMon;
            this.tenMon = tenMon;
            this.diemGiuaKy = diemGiuaKy;
            this.diemCuoiKy = diemCuoiKy;
            this.tongKet = tongKet;
            this.hanhKiem = hanhKiem;
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

        public String getIdMon() {
            return idMon;
        }

        public String getTenMon() {
            return tenMon;
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

        public String getNamHocDisplay() {
            if (namHoc == null || namHoc.isBlank()) {
                return "-";
            }
            return namHoc;
        }

        public String getNamHoc() {
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

    public static class ScoreGroupSummary {
        private final String studentId;
        private final String studentName;
        private final String subjectId;
        private final String subjectName;
        private final String className;
        private final String namHoc;

        public ScoreGroupSummary(String studentId,
                                 String studentName,
                                 String subjectId,
                                 String subjectName,
                                 String className,
                                 String namHoc) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.className = className;
            this.namHoc = namHoc;
        }

        public String getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getSubjectId() {
            return subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getClassName() {
            return className;
        }

        public String getNamHoc() {
            return namHoc;
        }
    }

    public static class ScoreEntry {
        private final Integer scoreId;
        private final Integer hocKy;
        private final Integer scoreTypeId;
        private final String scoreTypeName;
        private final Double scoreValue;
        private final String ngayNhap;
        private final String ghiChu;

        public ScoreEntry(Integer scoreId,
                          Integer hocKy,
                          Integer scoreTypeId,
                          String scoreTypeName,
                          Double scoreValue,
                          String ngayNhap,
                          String ghiChu) {
            this.scoreId = scoreId;
            this.hocKy = hocKy;
            this.scoreTypeId = scoreTypeId;
            this.scoreTypeName = scoreTypeName;
            this.scoreValue = scoreValue;
            this.ngayNhap = ngayNhap;
            this.ghiChu = ghiChu;
        }

        public Integer getScoreId() {
            return scoreId;
        }

        public Integer getHocKy() {
            return hocKy;
        }

        public String getHocKyDisplay() {
            if (hocKy == null) {
                return "-";
            }
            if (hocKy == 1) {
                return "Học kỳ 1";
            }
            if (hocKy == 2) {
                return "Học kỳ 2";
            }
            return "Học kỳ " + hocKy;
        }

        public Integer getScoreTypeId() {
            return scoreTypeId;
        }

        public String getScoreTypeName() {
            return scoreTypeName;
        }

        public Double getScoreValue() {
            return scoreValue;
        }

        public String getScoreValueDisplay() {
            if (scoreValue == null) {
                return "-";
            }
            BigDecimal number = BigDecimal.valueOf(scoreValue)
                    .setScale(2, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            return number.toPlainString();
        }

        public String getNgayNhap() {
            return ngayNhap;
        }

        public String getGhiChu() {
            return ghiChu;
        }
    }

    public static class ScoreEntryUpdate {
        private final Integer scoreId;
        private final String scoreValue;
        private final String scoreNote;

        public ScoreEntryUpdate(Integer scoreId, String scoreValue, String scoreNote) {
            this.scoreId = scoreId;
            this.scoreValue = scoreValue;
            this.scoreNote = scoreNote;
        }

        public Integer getScoreId() {
            return scoreId;
        }

        public String getScoreValue() {
            return scoreValue;
        }

        public String getScoreNote() {
            return scoreNote;
        }
    }
}
