package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ScoreDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Service
public class ScoreCreateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreCreateService.class);

    private static final String SEMESTER_ALL = "0";
    private static final String SEMESTER_1 = "1";
    private static final String SEMESTER_2 = "2";

    private static final LinkedHashMap<String, Integer> FREQUENT_SCORE_RULES = buildFrequentScoreRules();

    private final ScoreDAO scoreDAO;

    public ScoreCreateService(ScoreDAO scoreDAO) {
        this.scoreDAO = scoreDAO;
    }

    public ScoreCreatePageData getCreatePageData(ScoreCreateFilter rawFilter) {
        ScoreCreateFilter filter = normalizeFilter(rawFilter);

        List<OptionItem> schoolYears = safeListQuery("schoolYears", scoreDAO::findSchoolYearsForCreate).stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .map(value -> new OptionItem(value, value))
                .toList();

        List<OptionItem> courses = safeListQuery("courses", scoreDAO::findCoursesForCreate).stream()
                .map(this::mapOptionFromRow)
                .filter(Objects::nonNull)
                .toList();

        List<OptionItem> grades = safeListQuery("grades", scoreDAO::findGradesForCreate).stream()
                .filter(Objects::nonNull)
                .map(grade -> new OptionItem(String.valueOf(grade), "Khối " + grade))
                .toList();
        Integer gradeValue = parseInteger(filter.getKhoi());

        List<OptionItem> classes = safeListQuery("classes", () -> scoreDAO.findClassesForCreate(
                        gradeValue,
                        trimToNull(filter.getKhoa()),
                        trimToNull(filter.getNamHoc())
                )).stream()
                .map(this::mapOptionFromRow)
                .filter(Objects::nonNull)
                .toList();
        List<Object[]> subjectRows = safeListQuery("subjectsByGrade", () -> scoreDAO.findSubjectsForCreate(gradeValue));
        if (subjectRows.isEmpty()) {
            subjectRows = safeListQuery("subjectsAll", scoreDAO::findAllSubjectsForCreate);
        }
        List<OptionItem> subjects = subjectRows.stream()
                .map(this::mapOptionFromRow)
                .filter(Objects::nonNull)
                .toList();

        List<StudentItem> students = safeListQuery("students", () -> scoreDAO.findStudentsForCreate(
                        trimToNull(filter.getLop()),
                        trimToNull(filter.getQ())
                )).stream()
                .map(this::mapStudentFromRow)
                .filter(Objects::nonNull)
                .toList();

        StudentItem selectedStudent = null;
        String selectedStudentId = trimToNull(filter.getStudentId());
        if (selectedStudentId != null) {
            selectedStudent = students.stream()
                    .filter(item -> item.getId().equalsIgnoreCase(selectedStudentId))
                    .findFirst()
                    .orElseGet(() -> safeListQuery("studentById", () -> scoreDAO.findStudentForCreateById(selectedStudentId)).stream()
                            .map(this::mapStudentFromRow)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null));
        }

        if (selectedStudent != null) {
            filter.setStudentId(selectedStudent.getId());
        }

        String subjectName = subjects.stream()
                .filter(item -> item.getId().equalsIgnoreCase(defaultIfBlank(filter.getMon(), "")))
                .map(OptionItem::getName)
                .findFirst()
                .orElse("");
        if (isBlank(subjectName) && !isBlank(filter.getMon())) {
            try {
                subjectName = defaultIfBlank(trimToNull(scoreDAO.findSubjectNameById(filter.getMon())), "");
            } catch (RuntimeException ex) {
                LOGGER.error("Loi tai du lieu 'subjectNameById'", ex);
                subjectName = "";
            }
        }

        int frequentColumns = resolveFrequentColumns(subjectName);
        SemesterInput hk1Input = SemesterInput.blank(frequentColumns);
        SemesterInput hk2Input = SemesterInput.blank(frequentColumns);

        boolean hasRequiredSelection = selectedStudent != null
                && !isBlank(filter.getMon())
                && !isBlank(filter.getNamHoc());

        if (hasRequiredSelection) {
            String selectedStudentCode = selectedStudent.getId();
            List<Object[]> entries = safeListQuery("rawScoreEntries", () -> scoreDAO.findRawScoreEntriesForCreate(
                    selectedStudentCode,
                    filter.getMon(),
                    filter.getNamHoc()
            ));
            applyRowsToSemester(hk1Input, entries, 1, frequentColumns);
            applyRowsToSemester(hk2Input, entries, 2, frequentColumns);
        }

        hk1Input.setAverage(calculateSemesterAverage(hk1Input, frequentColumns));
        hk2Input.setAverage(calculateSemesterAverage(hk2Input, frequentColumns));

        Double yearAverage = null;
        if (hk1Input.getAverage() != null && hk2Input.getAverage() != null) {
            yearAverage = roundOneDecimal((hk1Input.getAverage() + 2 * hk2Input.getAverage()) / 3.0);
        }

        return new ScoreCreatePageData(
                filter,
                schoolYears,
                courses,
                grades,
                classes,
                subjects,
                students,
                selectedStudent,
                subjectName,
                frequentColumns,
                hk1Input,
                hk2Input,
                yearAverage,
                shouldShowSemester(filter.getHocKy(), 1),
                shouldShowSemester(filter.getHocKy(), 2),
                buildFrequentRuleItems(),
                "ĐTBmhk = (Tổng điểm TX + 2 × GK + 3 × CK) / (Số cột TX + 5)"
        );
    }

    @Transactional
    public void save(ScoreSaveRequest request) {
        String namHoc = trimToNull(request.getNamHoc());
        String hocKy = normalizeSemester(request.getHocKy());
        String subjectId = trimToNull(request.getMon());
        String studentId = trimToNull(request.getStudentId());

        if (namHoc == null || subjectId == null || studentId == null) {
            throw new RuntimeException("Thiếu thông tin bắt buộc để lưu điểm.");
        }

        String subjectName = trimToNull(scoreDAO.findSubjectNameById(subjectId));
        int frequentColumns = resolveFrequentColumns(defaultIfBlank(subjectName, ""));
        ScoreTypeMapping scoreTypeMapping = resolveScoreTypeMapping();

        List<Integer> targetSemesters = resolveTargetSemesters(hocKy);
        for (Integer semester : targetSemesters) {
            SemesterPayload payload = semester == 1
                    ? new SemesterPayload(request.getHk1Tx(), request.getHk1Midterm(), request.getHk1Final())
                    : new SemesterPayload(request.getHk2Tx(), request.getHk2Midterm(), request.getHk2Final());

            List<BigDecimal> frequentScores = parseFrequentScores(payload.frequentScores(), frequentColumns, semester);
            BigDecimal midtermScore = parseRequiredScore(payload.midterm(), "điểm giữa kỳ", semester);
            BigDecimal finalScore = parseRequiredScore(payload.finalScore(), "điểm cuối kỳ", semester);

            scoreDAO.deleteScoresByGroupAndSemester(studentId, subjectId, namHoc, semester);

            int index = 1;
            for (BigDecimal frequentScore : frequentScores) {
                scoreDAO.insertScoreEntry(
                        studentId,
                        subjectId,
                        scoreTypeMapping.frequentTypeId(),
                        namHoc,
                        semester,
                        frequentScore,
                        "TX" + index
                );
                index++;
            }

            scoreDAO.insertScoreEntry(
                    studentId,
                    subjectId,
                    scoreTypeMapping.midtermTypeId(),
                    namHoc,
                    semester,
                    midtermScore,
                    "Giữa kỳ"
            );

            scoreDAO.insertScoreEntry(
                    studentId,
                    subjectId,
                    scoreTypeMapping.finalTypeId(),
                    namHoc,
                    semester,
                    finalScore,
                    "Cuối kỳ"
            );
        }
    }

    public List<StudentItem> suggestStudents(String classId, String q) {
        return scoreDAO.findStudentsForCreate(trimToNull(classId), trimToNull(q)).stream()
                .map(this::mapStudentFromRow)
                .filter(Objects::nonNull)
                .limit(15)
                .toList();
    }

    public List<OptionItem> suggestCourses(String q) {
        return scoreDAO.findCourseSuggestionsForCreate(trimToNull(q)).stream()
                .map(this::mapOptionFromRow)
                .filter(Objects::nonNull)
                .toList();
    }

    private <T> List<T> safeListQuery(String queryName, Supplier<List<T>> supplier) {
        try {
            List<T> rows = supplier.get();
            return rows == null ? List.of() : rows;
        } catch (RuntimeException ex) {
            LOGGER.error("Loi tai du lieu '{}'", queryName, ex);
            return List.of();
        }
    }

    private ScoreCreateFilter normalizeFilter(ScoreCreateFilter rawFilter) {
        ScoreCreateFilter filter = rawFilter == null ? new ScoreCreateFilter() : rawFilter;
        filter.setNamHoc(trimToNull(filter.getNamHoc()));
        filter.setHocKy(normalizeSemester(filter.getHocKy()));
        filter.setKhoi(trimToNull(filter.getKhoi()));
        filter.setKhoa(trimToNull(filter.getKhoa()));
        filter.setLop(trimToNull(filter.getLop()));
        filter.setMon(trimToNull(filter.getMon()));
        filter.setQ(trimToNull(filter.getQ()));
        filter.setStudentId(trimToNull(filter.getStudentId()));
        return filter;
    }

    private String normalizeSemester(String hocKy) {
        String normalized = trimToNull(hocKy);
        if (SEMESTER_1.equals(normalized) || SEMESTER_2.equals(normalized)) {
            return normalized;
        }
        return SEMESTER_ALL;
    }

    private List<Integer> resolveTargetSemesters(String hocKy) {
        if (SEMESTER_1.equals(hocKy)) {
            return List.of(1);
        }
        if (SEMESTER_2.equals(hocKy)) {
            return List.of(2);
        }
        return List.of(1, 2);
    }

    private boolean shouldShowSemester(String hocKy, int semester) {
        if (SEMESTER_ALL.equals(hocKy)) {
            return true;
        }
        return String.valueOf(semester).equals(hocKy);
    }

    private OptionItem mapOptionFromRow(Object[] row) {
        if (row == null || row.length < 2) {
            return null;
        }
        String id = trimToNull(row[0] == null ? null : row[0].toString());
        String name = trimToNull(row[1] == null ? null : row[1].toString());
        if (id == null) {
            return null;
        }
        return new OptionItem(id, defaultIfBlank(name, id));
    }

    private StudentItem mapStudentFromRow(Object[] row) {
        if (row == null || row.length < 3) {
            return null;
        }
        String id = trimToNull(row[0] == null ? null : row[0].toString());
        String name = trimToNull(row[1] == null ? null : row[1].toString());
        String className = trimToNull(row[2] == null ? null : row[2].toString());
        if (id == null) {
            return null;
        }
        return new StudentItem(id, defaultIfBlank(name, id), defaultIfBlank(className, "-"));
    }
    private void applyRowsToSemester(SemesterInput semesterInput,
                                     List<Object[]> rows,
                                     int semester,
                                     int frequentColumns) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        List<String> frequentScores = new ArrayList<>();
        String midterm = "";
        String finalScore = "";

        for (Object[] row : rows) {
            Integer hocKy = asInteger(row, 0);
            Integer typeId = asInteger(row, 1);
            BigDecimal value = asBigDecimal(row, 2);
            if (hocKy == null || !hocKy.equals(semester) || value == null || typeId == null) {
                continue;
            }

            String display = formatScore(value);
            if (typeId == 4) {
                midterm = display;
                continue;
            }
            if (typeId == 5) {
                finalScore = display;
                continue;
            }
            if (frequentScores.size() < frequentColumns) {
                frequentScores.add(display);
            }
        }

        while (frequentScores.size() < frequentColumns) {
            frequentScores.add("");
        }

        semesterInput.setFrequentScores(frequentScores);
        semesterInput.setMidterm(midterm);
        semesterInput.setFinalScore(finalScore);
    }

    private Double calculateSemesterAverage(SemesterInput semesterInput, int frequentColumns) {
        if (semesterInput == null) {
            return null;
        }

        List<BigDecimal> frequentScores = parseOptionalFrequentScores(semesterInput.getFrequentScores(), frequentColumns);
        BigDecimal midterm = parseOptionalScore(semesterInput.getMidterm());
        BigDecimal finalScore = parseOptionalScore(semesterInput.getFinalScore());

        if (frequentScores == null || midterm == null || finalScore == null) {
            return null;
        }

        BigDecimal frequentTotal = frequentScores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal numerator = frequentTotal
                .add(midterm.multiply(BigDecimal.valueOf(2)))
                .add(finalScore.multiply(BigDecimal.valueOf(3)));
        BigDecimal denominator = BigDecimal.valueOf(frequentColumns + 5L);

        return numerator.divide(denominator, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private List<BigDecimal> parseOptionalFrequentScores(List<String> rawScores, int frequentColumns) {
        if (rawScores == null || rawScores.size() < frequentColumns) {
            return null;
        }

        List<BigDecimal> parsed = new ArrayList<>(frequentColumns);
        for (int index = 0; index < frequentColumns; index++) {
            BigDecimal score = parseOptionalScore(rawScores.get(index));
            if (score == null) {
                return null;
            }
            parsed.add(score);
        }
        return parsed;
    }

    private List<BigDecimal> parseFrequentScores(List<String> rawScores, int frequentColumns, int semester) {
        if (rawScores == null || rawScores.size() < frequentColumns) {
            throw new RuntimeException("Thiếu điểm thường xuyên của học kỳ " + semester + ".");
        }

        List<BigDecimal> parsed = new ArrayList<>(frequentColumns);
        for (int index = 0; index < frequentColumns; index++) {
            String label = "điểm thường xuyên cột " + (index + 1);
            parsed.add(parseRequiredScore(rawScores.get(index), label, semester));
        }
        return parsed;
    }

    private BigDecimal parseRequiredScore(String raw, String label, int semester) {
        String value = trimToNull(raw);
        if (value == null) {
            throw new RuntimeException("Thiếu " + label + " của học kỳ " + semester + ".");
        }

        try {
            BigDecimal score = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
            if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0) {
                throw new RuntimeException("Giá trị " + label + " của học kỳ " + semester + " phải từ 0 đến 10.");
            }
            return score;
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Giá trị " + label + " của học kỳ " + semester + " không hợp lệ.");
        }
    }

    private BigDecimal parseOptionalScore(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        try {
            BigDecimal score = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
            if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0) {
                return null;
            }
            return score;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ScoreTypeMapping resolveScoreTypeMapping() {
        List<Object[]> rows = scoreDAO.findScoreTypeDefinitions();
        if (rows.isEmpty()) {
            throw new RuntimeException("Thiếu cấu hình bảng loại điểm.");
        }

        Integer midtermType = null;
        Integer finalType = null;
        Integer frequentType = null;

        for (Object[] row : rows) {
            Integer id = asInteger(row, 0);
            Integer weight = asInteger(row, 1);
            if (id == null) {
                continue;
            }

            if (id == 4) {
                midtermType = id;
            }
            if (id == 5) {
                finalType = id;
            }

            if (weight != null && weight == 1 && frequentType == null) {
                frequentType = id;
            }
            if (weight != null && weight == 2 && midtermType == null) {
                midtermType = id;
            }
            if (weight != null && weight == 3 && finalType == null) {
                finalType = id;
            }
        }

        if (midtermType == null || finalType == null || frequentType == null) {
            throw new RuntimeException("Không xác định được loại điểm TX/Giữa kỳ/Cuối kỳ.");
        }

        if (frequentType.equals(midtermType) || frequentType.equals(finalType)) {
            final Integer currentMidtermType = midtermType;
            final Integer currentFinalType = finalType;
            Integer alternativeFrequent = rows.stream()
                    .map(row -> asInteger(row, 0))
                    .filter(Objects::nonNull)
                    .filter(id -> !id.equals(currentMidtermType) && !id.equals(currentFinalType))
                    .findFirst()
                    .orElse(null);
            if (alternativeFrequent != null) {
                frequentType = alternativeFrequent;
            }
        }

        if (frequentType.equals(midtermType) || frequentType.equals(finalType)) {
            throw new RuntimeException("Không tìm được loại điểm thường xuyên phù hợp.");
        }

        return new ScoreTypeMapping(frequentType, midtermType, finalType);
    }

    private int resolveFrequentColumns(String subjectName) {
        String normalized = normalizeKey(subjectName);
        for (Map.Entry<String, Integer> entry : FREQUENT_SCORE_RULES.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 3;
    }

    private List<FrequentRuleItem> buildFrequentRuleItems() {
        List<FrequentRuleItem> items = new ArrayList<>();
        items.add(new FrequentRuleItem("Ngữ văn", 4));
        items.add(new FrequentRuleItem("Toán", 4));
        items.add(new FrequentRuleItem("Ngoại ngữ 1", 4));
        items.add(new FrequentRuleItem("Ngoại ngữ 2", 4));
        items.add(new FrequentRuleItem("Lịch sử", 3));
        items.add(new FrequentRuleItem("Địa lí", 3));
        items.add(new FrequentRuleItem("Giáo dục kinh tế và pháp luật", 3));
        items.add(new FrequentRuleItem("Vật lí", 3));
        items.add(new FrequentRuleItem("Hóa học", 3));
        items.add(new FrequentRuleItem("Sinh học", 3));
        items.add(new FrequentRuleItem("Công nghệ", 3));
        items.add(new FrequentRuleItem("Tin học", 3));
        items.add(new FrequentRuleItem("Giáo dục quốc phòng và an ninh", 2));
        items.add(new FrequentRuleItem("Âm nhạc", 2));
        items.add(new FrequentRuleItem("Mĩ thuật", 2));
        items.add(new FrequentRuleItem("Hoạt động trải nghiệm, hướng nghiệp", 2));
        items.add(new FrequentRuleItem("Nội dung giáo dục của địa phương", 2));
        return items;
    }

    private static LinkedHashMap<String, Integer> buildFrequentScoreRules() {
        LinkedHashMap<String, Integer> rules = new LinkedHashMap<>();
        rules.put("ngu van", 4);
        rules.put("toan", 4);
        rules.put("ngoai ngu 1", 4);
        rules.put("ngoai ngu 2", 4);
        rules.put("lich su", 3);
        rules.put("giao duc quoc phong va an ninh", 2);
        rules.put("dia li", 3);
        rules.put("giao duc kinh te va phap luat", 3);
        rules.put("vat li", 3);
        rules.put("hoa hoc", 3);
        rules.put("sinh hoc", 3);
        rules.put("cong nghe", 3);
        rules.put("tin hoc", 3);
        rules.put("am nhac", 2);
        rules.put("mi thuat", 2);
        rules.put("hoat dong trai nghiem huong nghiep", 2);
        rules.put("noi dung giao duc cua dia phuong", 2);
        return rules;
    }
    private Integer parseInteger(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer asInteger(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }

        Object value = row[index];
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal asBigDecimal(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return null;
        }

        Object value = row[index];
        if (value instanceof BigDecimal number) {
            return number;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String formatScore(BigDecimal score) {
        if (score == null) {
            return "";
        }
        return score.setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultIfBlank(String value, String fallback) {
        String normalized = trimToNull(value);
        return normalized == null ? fallback : normalized;
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return decomposed.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private boolean isBlank(String value) {
        return trimToNull(value) == null;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static class OptionItem {
        private final String id;
        private final String name;

        public OptionItem(String id, String name) {
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

    public static class StudentItem {
        private final String id;
        private final String name;
        private final String className;

        public StudentItem(String id, String name, String className) {
            this.id = id;
            this.name = name;
            this.className = className;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }
    }

    private record SemesterPayload(List<String> frequentScores, String midterm, String finalScore) {
    }

    private record ScoreTypeMapping(Integer frequentTypeId, Integer midtermTypeId, Integer finalTypeId) {
    }

    public static class FrequentRuleItem {
        private final String subjectName;
        private final int frequentColumns;

        public FrequentRuleItem(String subjectName, int frequentColumns) {
            this.subjectName = subjectName;
            this.frequentColumns = frequentColumns;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public int getFrequentColumns() {
            return frequentColumns;
        }
    }

    public static class ScoreCreateFilter {
        private String namHoc;
        private String hocKy;
        private String khoi;
        private String khoa;
        private String lop;
        private String mon;
        private String q;
        private String studentId;

        public String getNamHoc() {
            return namHoc;
        }

        public void setNamHoc(String namHoc) {
            this.namHoc = namHoc;
        }

        public String getHocKy() {
            return hocKy;
        }

        public void setHocKy(String hocKy) {
            this.hocKy = hocKy;
        }

        public String getKhoi() {
            return khoi;
        }

        public void setKhoi(String khoi) {
            this.khoi = khoi;
        }

        public String getKhoa() {
            return khoa;
        }

        public void setKhoa(String khoa) {
            this.khoa = khoa;
        }

        public String getLop() {
            return lop;
        }

        public void setLop(String lop) {
            this.lop = lop;
        }

        public String getMon() {
            return mon;
        }

        public void setMon(String mon) {
            this.mon = mon;
        }

        public String getQ() {
            return q;
        }

        public void setQ(String q) {
            this.q = q;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }
    }
    public static class SemesterInput {
        private List<String> frequentScores;
        private String midterm;
        private String finalScore;
        private Double average;

        private SemesterInput(List<String> frequentScores,
                              String midterm,
                              String finalScore,
                              Double average) {
            this.frequentScores = frequentScores;
            this.midterm = midterm;
            this.finalScore = finalScore;
            this.average = average;
        }

        public static SemesterInput blank(int frequentColumns) {
            List<String> frequentScores = new ArrayList<>();
            for (int index = 0; index < frequentColumns; index++) {
                frequentScores.add("");
            }
            return new SemesterInput(frequentScores, "", "", null);
        }

        public List<String> getFrequentScores() {
            return frequentScores;
        }

        public void setFrequentScores(List<String> frequentScores) {
            this.frequentScores = frequentScores;
        }

        public String getMidterm() {
            return midterm;
        }

        public void setMidterm(String midterm) {
            this.midterm = midterm;
        }

        public String getFinalScore() {
            return finalScore;
        }

        public void setFinalScore(String finalScore) {
            this.finalScore = finalScore;
        }

        public Double getAverage() {
            return average;
        }

        public void setAverage(Double average) {
            this.average = average;
        }

        public String getAverageDisplay() {
            if (average == null) {
                return "--";
            }
            BigDecimal value = BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
            return value.stripTrailingZeros().toPlainString();
        }
    }

    public static class ScoreCreatePageData {
        private final ScoreCreateFilter filter;
        private final List<OptionItem> schoolYears;
        private final List<OptionItem> courses;
        private final List<OptionItem> grades;
        private final List<OptionItem> classes;
        private final List<OptionItem> subjects;
        private final List<StudentItem> students;
        private final StudentItem selectedStudent;
        private final String selectedSubjectName;
        private final int frequentColumns;
        private final SemesterInput hk1Input;
        private final SemesterInput hk2Input;
        private final Double yearAverage;
        private final boolean showSemester1;
        private final boolean showSemester2;
        private final List<FrequentRuleItem> frequentRuleItems;
        private final String formulaText;

        public ScoreCreatePageData(ScoreCreateFilter filter,
                                   List<OptionItem> schoolYears,
                                   List<OptionItem> courses,
                                   List<OptionItem> grades,
                                   List<OptionItem> classes,
                                   List<OptionItem> subjects,
                                   List<StudentItem> students,
                                   StudentItem selectedStudent,
                                   String selectedSubjectName,
                                   int frequentColumns,
                                   SemesterInput hk1Input,
                                   SemesterInput hk2Input,
                                   Double yearAverage,
                                   boolean showSemester1,
                                   boolean showSemester2,
                                   List<FrequentRuleItem> frequentRuleItems,
                                   String formulaText) {
            this.filter = filter;
            this.schoolYears = schoolYears;
            this.courses = courses;
            this.grades = grades;
            this.classes = classes;
            this.subjects = subjects;
            this.students = students;
            this.selectedStudent = selectedStudent;
            this.selectedSubjectName = selectedSubjectName;
            this.frequentColumns = frequentColumns;
            this.hk1Input = hk1Input;
            this.hk2Input = hk2Input;
            this.yearAverage = yearAverage;
            this.showSemester1 = showSemester1;
            this.showSemester2 = showSemester2;
            this.frequentRuleItems = frequentRuleItems;
            this.formulaText = formulaText;
        }

        public ScoreCreateFilter getFilter() {
            return filter;
        }

        public List<OptionItem> getSchoolYears() {
            return schoolYears;
        }

        public List<OptionItem> getCourses() {
            return courses;
        }

        public List<OptionItem> getGrades() {
            return grades;
        }

        public List<OptionItem> getClasses() {
            return classes;
        }

        public List<OptionItem> getSubjects() {
            return subjects;
        }

        public List<StudentItem> getStudents() {
            return students;
        }

        public StudentItem getSelectedStudent() {
            return selectedStudent;
        }

        public String getSelectedSubjectName() {
            return selectedSubjectName;
        }

        public int getFrequentColumns() {
            return frequentColumns;
        }

        public SemesterInput getHk1Input() {
            return hk1Input;
        }

        public SemesterInput getHk2Input() {
            return hk2Input;
        }

        public Double getYearAverage() {
            return yearAverage;
        }

        public String getYearAverageDisplay() {
            if (yearAverage == null) {
                return "--";
            }
            BigDecimal value = BigDecimal.valueOf(yearAverage).setScale(1, RoundingMode.HALF_UP);
            return value.stripTrailingZeros().toPlainString();
        }

        public boolean isShowSemester1() {
            return showSemester1;
        }

        public boolean isShowSemester2() {
            return showSemester2;
        }

        public List<FrequentRuleItem> getFrequentRuleItems() {
            return frequentRuleItems;
        }

        public String getFormulaText() {
            return formulaText;
        }

        public boolean isReadyForInput() {
            return selectedStudent != null && !isBlank(filter.getMon()) && !isBlank(filter.getNamHoc());
        }

        public String getRequiredTxMessage() {
            return "Số cột điểm thường xuyên cho môn đang chọn: " + frequentColumns;
        }

        private boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }
    }
    public static class ScoreSaveRequest {
        private String namHoc;
        private String hocKy;
        private String khoi;
        private String khoa;
        private String lop;
        private String mon;
        private String q;
        private String studentId;

        private List<String> hk1Tx;
        private String hk1Midterm;
        private String hk1Final;

        private List<String> hk2Tx;
        private String hk2Midterm;
        private String hk2Final;

        public String getNamHoc() {
            return namHoc;
        }

        public void setNamHoc(String namHoc) {
            this.namHoc = namHoc;
        }

        public String getHocKy() {
            return hocKy;
        }

        public void setHocKy(String hocKy) {
            this.hocKy = hocKy;
        }

        public String getKhoi() {
            return khoi;
        }

        public void setKhoi(String khoi) {
            this.khoi = khoi;
        }

        public String getKhoa() {
            return khoa;
        }

        public void setKhoa(String khoa) {
            this.khoa = khoa;
        }

        public String getLop() {
            return lop;
        }

        public void setLop(String lop) {
            this.lop = lop;
        }

        public String getMon() {
            return mon;
        }

        public void setMon(String mon) {
            this.mon = mon;
        }

        public String getQ() {
            return q;
        }

        public void setQ(String q) {
            this.q = q;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public List<String> getHk1Tx() {
            return hk1Tx;
        }

        public void setHk1Tx(List<String> hk1Tx) {
            this.hk1Tx = hk1Tx;
        }

        public String getHk1Midterm() {
            return hk1Midterm;
        }

        public void setHk1Midterm(String hk1Midterm) {
            this.hk1Midterm = hk1Midterm;
        }

        public String getHk1Final() {
            return hk1Final;
        }

        public void setHk1Final(String hk1Final) {
            this.hk1Final = hk1Final;
        }

        public List<String> getHk2Tx() {
            return hk2Tx;
        }

        public void setHk2Tx(List<String> hk2Tx) {
            this.hk2Tx = hk2Tx;
        }

        public String getHk2Midterm() {
            return hk2Midterm;
        }

        public void setHk2Midterm(String hk2Midterm) {
            this.hk2Midterm = hk2Midterm;
        }

        public String getHk2Final() {
            return hk2Final;
        }

        public void setHk2Final(String hk2Final) {
            this.hk2Final = hk2Final;
        }
    }
}
