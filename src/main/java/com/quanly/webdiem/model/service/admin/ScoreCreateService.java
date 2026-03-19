package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ScoreDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScoreCreateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreCreateService.class);

    private static final String SEMESTER_ALL = "0";
    private static final String SEMESTER_1 = "1";
    private static final String SEMESTER_2 = "2";
    private static final int CONDUCT_YEAR = 0;
    private static final int CONDUCT_SEMESTER_1 = 1;
    private static final int CONDUCT_SEMESTER_2 = 2;
    private static final String CONDUCT_TOT = "Tot";
    private static final String CONDUCT_KHA = "Kha";
    private static final String CONDUCT_TRUNG_BINH = "Trung_binh";
    private static final String CONDUCT_YEU = "Yeu";
    private static final String ROLE_ADMIN = "ROLE_Admin";
    private static final Pattern TEACHER_ID_IN_PAREN_PATTERN = Pattern.compile("\\(([^)]+)\\)\\s*$");

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
        grades = ensureDefaultGrades(grades);
        Integer gradeValue = parseInteger(filter.getKhoi());

        List<OptionItem> classes = new ArrayList<>(safeListQuery("classes", () -> scoreDAO.findClassesForCreate(
                        gradeValue,
                        trimToNull(filter.getKhoa()),
                        trimToNull(filter.getNamHoc())
                )).stream()
                .map(this::mapOptionFromRow)
                .filter(Objects::nonNull)
                .toList());
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
            if (!isBlank(selectedStudent.getClassId())) {
                filter.setLop(selectedStudent.getClassId());
            }
            if (!isBlank(selectedStudent.getGrade())) {
                filter.setKhoi(selectedStudent.getGrade());
            }
            if (!isBlank(selectedStudent.getCourseId())) {
                filter.setKhoa(selectedStudent.getCourseId());
            }

            Integer syncedGrade = parseInteger(filter.getKhoi());
            classes = new ArrayList<>(safeListQuery("classesSynced", () -> scoreDAO.findClassesForCreate(
                            syncedGrade,
                            trimToNull(filter.getKhoa()),
                            trimToNull(filter.getNamHoc())
                    )).stream()
                    .map(this::mapOptionFromRow)
                    .filter(Objects::nonNull)
                    .toList());
            ensureClassOption(classes, selectedStudent);
        }

        String consistencyError = validateSelectedStudentConsistency(
                selectedStudent,
                filter.getLop(),
                filter.getKhoi(),
                filter.getKhoa()
        );
        String filterValidationMessage = buildFilterValidationMessage(filter, selectedStudent);

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
        ConductInput hk1Conduct = new ConductInput(CONDUCT_TOT);
        ConductInput hk2Conduct = new ConductInput(CONDUCT_TOT);
        ConductInput yearConduct = new ConductInput(CONDUCT_TOT);
        String hk1Teacher = trimToNull(filter.getTeacherHk1());
        String hk2Teacher = trimToNull(filter.getTeacherHk2());
        boolean hasScoresSemester1 = false;
        boolean hasScoresSemester2 = false;

        boolean hasRequiredSelection = selectedStudent != null
                && !isBlank(filter.getMon())
                && !isBlank(filter.getNamHoc())
                && isBlank(consistencyError);

        if (hasRequiredSelection) {
            String selectedStudentCode = selectedStudent.getId();
            List<Object[]> entries = safeListQuery("rawScoreEntries", () -> scoreDAO.findRawScoreEntriesForCreate(
                    selectedStudentCode,
                    filter.getMon(),
                    filter.getNamHoc()
            ));
            applyRowsToSemester(hk1Input, entries, 1, frequentColumns);
            applyRowsToSemester(hk2Input, entries, 2, frequentColumns);
            hasScoresSemester1 = hasAnyScoreInSemester(entries, 1);
            hasScoresSemester2 = hasAnyScoreInSemester(entries, 2);
            Map<Integer, String> teacherIdsBySemester = extractTeacherIdsBySemester(entries);
            if (isBlank(hk1Teacher)) {
                hk1Teacher = toTeacherDisplay(teacherIdsBySemester.get(1));
            }
            if (isBlank(hk2Teacher)) {
                hk2Teacher = toTeacherDisplay(teacherIdsBySemester.get(2));
            }

            List<Object[]> conductRows = safeListQuery("rawConductEntries", () -> scoreDAO.findConductsForCreate(
                    selectedStudentCode,
                    filter.getNamHoc()
            ));
            applyRowsToConducts(hk1Conduct, hk2Conduct, yearConduct, conductRows);
        }

        String classIdForTeacher = trimToNull(selectedStudent == null ? filter.getLop() : selectedStudent.getClassId());
        String subjectIdForTeacher = trimToNull(filter.getMon());
        String schoolYearForTeacher = trimToNull(filter.getNamHoc());
        boolean canResolveTeacherByAssignment = !isBlank(subjectIdForTeacher)
                && !isBlank(classIdForTeacher)
                && !isBlank(schoolYearForTeacher);
        if (isBlank(hk1Teacher) && canResolveTeacherByAssignment) {
            hk1Teacher = toTeacherDisplay(scoreDAO.findFirstAssignedTeacherForScore(
                    subjectIdForTeacher,
                    classIdForTeacher,
                    schoolYearForTeacher,
                    1
            ));
        }
        if (isBlank(hk2Teacher) && canResolveTeacherByAssignment) {
            hk2Teacher = toTeacherDisplay(scoreDAO.findFirstAssignedTeacherForScore(
                    subjectIdForTeacher,
                    classIdForTeacher,
                    schoolYearForTeacher,
                    2
            ));
        }
        filter.setTeacherHk1(hk1Teacher);
        filter.setTeacherHk2(hk2Teacher);

        List<OptionItem> teacherOptionsHk1 = buildTeacherOptions(
                subjectIdForTeacher,
                classIdForTeacher,
                schoolYearForTeacher,
                1,
                hk1Teacher
        );
        List<OptionItem> teacherOptionsHk2 = buildTeacherOptions(
                subjectIdForTeacher,
                classIdForTeacher,
                schoolYearForTeacher,
                2,
                hk2Teacher
        );

        hk1Input.setAverage(calculateSemesterAverage(hk1Input, frequentColumns));
        hk2Input.setAverage(calculateSemesterAverage(hk2Input, frequentColumns));

        Double yearAverage = null;
        if (hk1Input.getAverage() != null && hk2Input.getAverage() != null) {
            yearAverage = roundOneDecimal((hk1Input.getAverage() + 2 * hk2Input.getAverage()) / 3.0);
        }
        String existingScoreNotice = buildExistingScoreNotice(filter.getHocKy(), hasScoresSemester1, hasScoresSemester2);

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
                consistencyError,
                filterValidationMessage,
                existingScoreNotice,
                "ĐTBmhk = (Tổng điểm TX + 2 × GK + 3 × CK) / (Số cột TX + 5)",
                hk1Conduct,
                hk2Conduct,
                yearConduct,
                buildConductOptions(),
                teacherOptionsHk1,
                teacherOptionsHk2
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

        StudentItem selectedStudent = safeListQuery("studentByIdForSave", () -> scoreDAO.findStudentForCreateById(studentId)).stream()
                .map(this::mapStudentFromRow)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (selectedStudent == null) {
            throw new RuntimeException("Không tìm thấy học sinh đã chọn.");
        }

        String consistencyError = validateSelectedStudentConsistency(
                selectedStudent,
                request.getLop(),
                request.getKhoi(),
                request.getKhoa()
        );
        if (!isBlank(consistencyError)) {
            throw new RuntimeException(consistencyError);
        }

        String subjectName = trimToNull(scoreDAO.findSubjectNameById(subjectId));
        int frequentColumns = resolveFrequentColumns(defaultIfBlank(subjectName, ""));
        ScoreTypeMapping scoreTypeMapping = resolveScoreTypeMapping();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean currentUserIsAdmin = isAdmin(authentication);
        String accountTeacherId = resolveCurrentTeacherId(authentication);
        String classId = trimToNull(selectedStudent.getClassId());
        if (classId == null) {
            throw new RuntimeException("Không xác định được lớp của học sinh đã chọn.");
        }

        List<Integer> targetSemesters = resolveTargetSemesters(hocKy);
        Map<Integer, String> semesterTeacherIds = new TreeMap<>();
        boolean adminBypassFlagApplied = false;
        try {
            if (currentUserIsAdmin) {
                scoreDAO.setAdminBypassFlag(1);
                adminBypassFlagApplied = true;
            }
            for (Integer semester : targetSemesters) {
                String selectedTeacherId = resolveTeacherForSemester(
                        request,
                        semester,
                        currentUserIsAdmin,
                        accountTeacherId,
                        subjectId,
                        classId,
                        namHoc
                );
                if (currentUserIsAdmin) {
                    ensureAssignmentForAdmin(selectedTeacherId, subjectId, classId, namHoc, semester);
                }
                if (!currentUserIsAdmin) {
                    validateTeacherAssignment(selectedTeacherId, subjectId, namHoc, semester, classId);
                }
                semesterTeacherIds.put(semester, selectedTeacherId);

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
                            selectedTeacherId,
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
                        selectedTeacherId,
                        "Giữa kỳ"
                );

                scoreDAO.insertScoreEntry(
                        studentId,
                        subjectId,
                        scoreTypeMapping.finalTypeId(),
                        namHoc,
                        semester,
                        finalScore,
                        selectedTeacherId,
                        "Cuối kỳ"
                );
            }
            saveConducts(request, studentId, namHoc, hocKy, semesterTeacherIds, accountTeacherId);
            cleanupSourceScopeAfterMove(
                    request,
                    studentId,
                    subjectId,
                    namHoc,
                    classId,
                    targetSemesters,
                    currentUserIsAdmin,
                    accountTeacherId
            );
        } catch (RuntimeException ex) {
            String friendlyMessage = buildFriendlySaveError(ex, selectedStudent, subjectName, targetSemesters);
            throw new RuntimeException(friendlyMessage, ex);
        } finally {
            if (adminBypassFlagApplied) {
                try {
                    scoreDAO.clearAdminBypassFlag();
                } catch (RuntimeException clearEx) {
                    LOGGER.warn("Khong the reset bien session @app_is_admin sau khi luu diem", clearEx);
                }
            }
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

    public List<TeacherItem> suggestTeachers(String subjectId,
                                             String classId,
                                             String namHoc,
                                             String hocKy,
                                             String q) {
        Integer semester = parseInteger(trimToNull(hocKy));
        if (semester == null || semester <= 0) {
            semester = null;
        }
        return lookupTeacherSuggestions(
                trimToNull(subjectId),
                trimToNull(classId),
                trimToNull(namHoc),
                semester,
                trimToNull(q)
        );
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

    private String validateSelectedStudentConsistency(StudentItem selectedStudent,
                                                      String classId,
                                                      String grade,
                                                      String courseId) {
        if (selectedStudent == null) {
            return null;
        }

        if (!isBlank(classId) && !equalsTrimIgnoreCase(classId, selectedStudent.getClassId())) {
            return "Thông tin lớp không khớp học sinh đã chọn. Lớp đúng: "
                    + defaultIfBlank(selectedStudent.getClassName(), "-");
        }
        if (!isBlank(grade) && !equalsTrimIgnoreCase(grade, selectedStudent.getGrade())) {
            return "Thông tin khối không khớp học sinh đã chọn. Khối đúng: "
                    + defaultIfBlank(selectedStudent.getGrade(), "-");
        }
        if (!isBlank(courseId) && !equalsTrimIgnoreCase(courseId, selectedStudent.getCourseId())) {
            return "Thông tin khóa học không khớp học sinh đã chọn. Khóa đúng: "
                    + defaultIfBlank(selectedStudent.getCourseId(), "-");
        }
        return null;
    }

    private void ensureClassOption(List<OptionItem> classes, StudentItem selectedStudent) {
        if (classes == null || selectedStudent == null || isBlank(selectedStudent.getClassId())) {
            return;
        }
        boolean exists = classes.stream()
                .anyMatch(item -> equalsTrimIgnoreCase(item.getId(), selectedStudent.getClassId()));
        if (!exists) {
            classes.add(new OptionItem(
                    selectedStudent.getClassId(),
                    defaultIfBlank(selectedStudent.getClassName(), selectedStudent.getClassId())
            ));
        }
    }

    private List<OptionItem> ensureDefaultGrades(List<OptionItem> sourceGrades) {
        Map<Integer, OptionItem> gradeMap = new TreeMap<>();
        if (sourceGrades != null) {
            for (OptionItem item : sourceGrades) {
                Integer grade = parseInteger(item == null ? null : item.getId());
                if (grade == null) {
                    continue;
                }
                gradeMap.putIfAbsent(grade, new OptionItem(String.valueOf(grade), "Khối " + grade));
            }
        }
        for (int mandatoryGrade : new int[] {10, 11, 12}) {
            gradeMap.putIfAbsent(mandatoryGrade, new OptionItem(String.valueOf(mandatoryGrade), "Khối " + mandatoryGrade));
        }
        return new ArrayList<>(gradeMap.values());
    }

    private String buildFilterValidationMessage(ScoreCreateFilter filter, StudentItem selectedStudent) {
        if (filter == null || !"1".equals(trimToNull(filter.getApplyFilter()))) {
            return null;
        }

        List<String> missingFields = new ArrayList<>();
        if (isBlank(filter.getNamHoc())) {
            missingFields.add("Năm học");
        }
        if (isBlank(filter.getLop())) {
            missingFields.add("Lớp");
        }
        if (isBlank(filter.getMon())) {
            missingFields.add("Môn học");
        }
        if (selectedStudent == null) {
            if (isBlank(filter.getQ())) {
                missingFields.add("Học sinh");
            } else {
                missingFields.add("Học sinh hợp lệ (chọn từ gợi ý)");
            }
        }

        if (missingFields.isEmpty()) {
            return null;
        }
        return "Vui lòng nhập đầy đủ thông tin bộ lọc: " + String.join(", ", missingFields) + ".";
    }

    private boolean equalsTrimIgnoreCase(String left, String right) {
        String normalizedLeft = trimToNull(left);
        String normalizedRight = trimToNull(right);
        if (normalizedLeft == null || normalizedRight == null) {
            return false;
        }
        return normalizedLeft.equalsIgnoreCase(normalizedRight);
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
        filter.setTeacherHk1(trimToNull(filter.getTeacherHk1()));
        filter.setTeacherHk2(trimToNull(filter.getTeacherHk2()));
        filter.setApplyFilter(trimToNull(filter.getApplyFilter()));
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

    private boolean hasAnyScoreInSemester(List<Object[]> rows, int semester) {
        if (rows == null || rows.isEmpty()) {
            return false;
        }
        for (Object[] row : rows) {
            Integer hocKy = asInteger(row, 0);
            BigDecimal scoreValue = asBigDecimal(row, 2);
            if (hocKy != null && hocKy == semester && scoreValue != null) {
                return true;
            }
        }
        return false;
    }

    private String buildExistingScoreNotice(String hocKy, boolean hasSemester1, boolean hasSemester2) {
        if (!hasSemester1 && !hasSemester2) {
            return null;
        }
        if (hasSemester1 && hasSemester2) {
            return "Học sinh đã có điểm cả 2 học kỳ.";
        }
        if (SEMESTER_1.equals(hocKy) && hasSemester1) {
            return "Học sinh đã có điểm học kỳ I.";
        }
        if (SEMESTER_2.equals(hocKy) && hasSemester2) {
            return "Học sinh đã có điểm học kỳ II.";
        }
        if (SEMESTER_ALL.equals(hocKy)) {
            if (hasSemester1) {
                return "Học sinh đã có điểm học kỳ I.";
            }
            return "Học sinh đã có điểm học kỳ II.";
        }
        return null;
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
        String classId = row.length > 3 ? trimToNull(row[3] == null ? null : row[3].toString()) : null;
        String grade = row.length > 4 ? trimToNull(row[4] == null ? null : row[4].toString()) : null;
        String courseId = row.length > 5 ? trimToNull(row[5] == null ? null : row[5].toString()) : null;
        if (id == null) {
            return null;
        }
        return new StudentItem(
                id,
                defaultIfBlank(name, id),
                defaultIfBlank(className, "-"),
                classId,
                grade,
                courseId
        );
    }

    private TeacherItem mapTeacherFromRow(Object[] row) {
        if (row == null || row.length < 2) {
            return null;
        }
        String id = trimToNull(row[0] == null ? null : row[0].toString());
        String name = trimToNull(row[1] == null ? null : row[1].toString());
        if (id == null) {
            return null;
        }
        return new TeacherItem(id.toUpperCase(Locale.ROOT), defaultIfBlank(name, id));
    }

    private List<TeacherItem> lookupTeacherSuggestions(String subjectId,
                                                       String classId,
                                                       String namHoc,
                                                       Integer hocKy,
                                                       String keyword) {
        List<TeacherItem> primary = mapTeacherRows(safeListQuery(
                "teacherSuggestPrimary",
                () -> scoreDAO.suggestTeachingTeachersForScore(subjectId, classId, namHoc, hocKy, keyword)
        ));

        if (!primary.isEmpty()) {
            return primary;
        }

        if (!isBlank(subjectId)) {
            List<TeacherItem> bySubject = mapTeacherRows(safeListQuery(
                    "teacherSuggestBySubject",
                    () -> scoreDAO.suggestTeachersBySubjectForScore(subjectId, keyword)
            ));
            if (!bySubject.isEmpty()) {
                return bySubject;
            }
        }

        return mapTeacherRows(safeListQuery(
                "teacherSuggestGlobal",
                () -> scoreDAO.suggestAllTeachersForScore(keyword)
        ));
    }

    private List<TeacherItem> mapTeacherRows(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        Map<String, TeacherItem> deduplicated = new LinkedHashMap<>();
        for (Object[] row : rows) {
            TeacherItem item = mapTeacherFromRow(row);
            if (item == null) {
                continue;
            }
            deduplicated.putIfAbsent(item.getId().toUpperCase(Locale.ROOT), item);
            if (deduplicated.size() >= 20) {
                break;
            }
        }
        return new ArrayList<>(deduplicated.values());
    }

    private Map<Integer, String> extractTeacherIdsBySemester(List<Object[]> rows) {
        Map<Integer, String> result = new TreeMap<>();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        for (Object[] row : rows) {
            Integer semester = asInteger(row, 0);
            if (semester == null || result.containsKey(semester)) {
                continue;
            }
            String teacherId = row != null && row.length > 4 && row[4] != null
                    ? trimToNull(row[4].toString())
                    : null;
            if (teacherId == null) {
                continue;
            }
            result.put(semester, teacherId.toUpperCase(Locale.ROOT));
        }
        return result;
    }

    private String toTeacherDisplay(String teacherId) {
        String normalizedTeacherId = trimToNull(teacherId);
        if (normalizedTeacherId == null) {
            return null;
        }
        String upperTeacherId = normalizedTeacherId.toUpperCase(Locale.ROOT);
        String teacherName = trimToNull(scoreDAO.findTeacherNameById(upperTeacherId));
        if (teacherName == null) {
            return upperTeacherId;
        }
        return teacherName + " (" + upperTeacherId + ")";
    }

    private List<OptionItem> buildTeacherOptions(String subjectId,
                                                 String classId,
                                                 String namHoc,
                                                 int hocKy,
                                                 String currentTeacherInput) {
        if (isBlank(subjectId) || isBlank(namHoc)) {
            return List.of();
        }
        List<OptionItem> options = new ArrayList<>(lookupTeacherSuggestions(
                subjectId,
                classId,
                namHoc,
                hocKy,
                null
        ).stream()
                .filter(Objects::nonNull)
                .map(item -> new OptionItem(item.getId(), item.toDisplay()))
                .toList());

        String selectedId = resolveTeacherIdFromInput(currentTeacherInput);
        if (selectedId != null) {
            boolean exists = options.stream().anyMatch(item -> equalsTrimIgnoreCase(item.getId(), selectedId));
            if (!exists) {
                options.add(new OptionItem(selectedId, defaultIfBlank(toTeacherDisplay(selectedId), selectedId)));
            }
        }
        return options;
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

    private void applyRowsToConducts(ConductInput hk1Conduct,
                                     ConductInput hk2Conduct,
                                     ConductInput yearConduct,
                                     List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (Object[] row : rows) {
            Integer hocKy = asInteger(row, 0);
            String xepLoai = trimToNull(row != null && row.length > 1 && row[1] != null ? row[1].toString() : null);
            if (hocKy == null || xepLoai == null) {
                continue;
            }
            String normalized = normalizeConductValue(xepLoai);
            if (normalized == null) {
                continue;
            }
            if (hocKy == CONDUCT_SEMESTER_1) {
                hk1Conduct.setValue(normalized);
                continue;
            }
            if (hocKy == CONDUCT_SEMESTER_2) {
                hk2Conduct.setValue(normalized);
                continue;
            }
            if (hocKy == CONDUCT_YEAR) {
                yearConduct.setValue(normalized);
            }
        }
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

    private String resolveCurrentTeacherId() {
        return resolveCurrentTeacherId(SecurityContextHolder.getContext().getAuthentication());
    }

    private String resolveCurrentTeacherId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = trimToNull(authentication.getName());
        if (username == null || "anonymousUser".equalsIgnoreCase(username)) {
            return null;
        }

        String teacherId = trimToNull(scoreDAO.findTeacherIdByUsername(username));
        if (teacherId != null) {
            return teacherId.toUpperCase(Locale.ROOT);
        }

        if (username.matches("(?i)^gv[0-9]+$")) {
            return username.toUpperCase(Locale.ROOT);
        }
        return null;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        String username = trimToNull(authentication.getName());
        if ("admin".equalsIgnoreCase(username)) {
            return true;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) {
                continue;
            }
            String authorityValue = authority.getAuthority();
            if (ROLE_ADMIN.equalsIgnoreCase(authorityValue)) {
                return true;
            }
            if ("ROLE_ADMIN".equalsIgnoreCase(authorityValue)) {
                return true;
            }
            if ("ADMIN".equalsIgnoreCase(authorityValue)) {
                return true;
            }
        }
        return false;
    }

    private void ensureAssignmentForAdmin(String teacherId,
                                          String subjectId,
                                          String classId,
                                          String namHoc,
                                          Integer hocKy) {
        try {
            scoreDAO.ensureTeachingAssignmentForScore(teacherId, subjectId, classId, namHoc, hocKy);
        } catch (RuntimeException ex) {
            throw new RuntimeException(
                    "Không thể tự động gán phân công cho giáo viên chấm. "
                            + "Vui lòng kiểm tra trạng thái giáo viên và dữ liệu lớp/môn.",
                    ex
            );
        }
    }

    private String resolveTeacherForSemester(ScoreSaveRequest request,
                                             int semester,
                                             boolean currentUserIsAdmin,
                                             String accountTeacherId,
                                             String subjectId,
                                             String classId,
                                             String namHoc) {
        String rawInput = semester == 1 ? request.getHk1Teacher() : request.getHk2Teacher();
        String selectedTeacherId = resolveTeacherIdFromInput(rawInput);

        if (selectedTeacherId == null && accountTeacherId != null && !currentUserIsAdmin) {
            selectedTeacherId = accountTeacherId;
        }

        if (selectedTeacherId == null && currentUserIsAdmin) {
            selectedTeacherId = trimToNull(scoreDAO.findFirstAssignedTeacherForScore(subjectId, classId, namHoc, semester));
        }

        if (selectedTeacherId == null) {
            throw new RuntimeException("Vui lòng chọn giáo viên chấm cho học kỳ " + (semester == 1 ? "I" : "II") + ".");
        }

        if (!currentUserIsAdmin && accountTeacherId != null && !accountTeacherId.equalsIgnoreCase(selectedTeacherId)) {
            throw new RuntimeException("Bạn chỉ được lưu điểm bằng mã giáo viên của tài khoản đang đăng nhập.");
        }

        return selectedTeacherId.toUpperCase(Locale.ROOT);
    }

    private String resolveTeacherIdFromInput(String rawInput) {
        String value = trimToNull(rawInput);
        if (value == null) {
            return null;
        }

        if (value.matches("(?i)^gv[0-9a-z_-]+$")) {
            return value.toUpperCase(Locale.ROOT);
        }

        Matcher matcher = TEACHER_ID_IN_PAREN_PATTERN.matcher(value);
        if (matcher.find()) {
            String candidate = trimToNull(matcher.group(1));
            if (candidate != null && candidate.matches("(?i)^gv[0-9a-z_-]+$")) {
                return candidate.toUpperCase(Locale.ROOT);
            }
        }
        return null;
    }

    private void validateTeacherAssignment(String teacherId,
                                           String subjectId,
                                           String namHoc,
                                           Integer hocKy,
                                           String classId) {
        long assignedCount = scoreDAO.countTeachingAssignmentForScore(teacherId, subjectId, namHoc, hocKy, classId);
        if (assignedCount > 0) {
            return;
        }
        String semesterLabel = hocKy != null && hocKy == 1 ? "học kỳ I" : "học kỳ II";
        throw new RuntimeException("Bạn chưa được phân công dạy môn này cho lớp của học sinh ở " + semesterLabel + ".");
    }

    private void saveConducts(ScoreSaveRequest request,
                              String studentId,
                              String namHoc,
                              String hocKy,
                              Map<Integer, String> semesterTeacherIds,
                              String fallbackTeacherId) {
        Set<Integer> allowedSemesters = Set.copyOf(resolveTargetSemesters(hocKy));
        upsertConductValue(
                studentId,
                namHoc,
                resolveTeacherForConduct(semesterTeacherIds, CONDUCT_SEMESTER_1, fallbackTeacherId),
                CONDUCT_SEMESTER_1,
                request.getHk1Conduct(),
                allowedSemesters
        );
        upsertConductValue(
                studentId,
                namHoc,
                resolveTeacherForConduct(semesterTeacherIds, CONDUCT_SEMESTER_2, fallbackTeacherId),
                CONDUCT_SEMESTER_2,
                request.getHk2Conduct(),
                allowedSemesters
        );
        upsertConductValue(
                studentId,
                namHoc,
                resolveTeacherForConduct(semesterTeacherIds, CONDUCT_YEAR, fallbackTeacherId),
                CONDUCT_YEAR,
                request.getYearConduct(),
                allowedSemesters
        );
    }

    private void cleanupSourceScopeAfterMove(ScoreSaveRequest request,
                                             String studentId,
                                             String targetSubjectId,
                                             String targetNamHoc,
                                             String classId,
                                             List<Integer> targetSemesters,
                                             boolean currentUserIsAdmin,
                                             String fallbackTeacherId) {
        String sourceSubjectId = trimToNull(request.getSourceMon());
        String sourceNamHoc = trimToNull(request.getSourceNamHoc());
        if (sourceSubjectId == null || sourceNamHoc == null) {
            return;
        }

        boolean sameSubject = sourceSubjectId.equalsIgnoreCase(targetSubjectId);
        boolean sameYear = sourceNamHoc.equalsIgnoreCase(targetNamHoc);
        if (sameSubject && sameYear) {
            return;
        }

        List<Object[]> sourceRows = safeListQuery("sourceRawScoreEntriesForMove", () -> scoreDAO.findRawScoreEntriesForCreate(
                studentId,
                sourceSubjectId,
                sourceNamHoc
        ));
        List<Object[]> targetRows = safeListQuery("targetRawScoreEntriesForMove", () -> scoreDAO.findRawScoreEntriesForCreate(
                studentId,
                targetSubjectId,
                targetNamHoc
        ));
        Set<Integer> selectedSemesters = Set.copyOf(targetSemesters);
        List<Integer> allSemesters = List.of(1, 2);

        for (Integer semester : allSemesters) {
            if (selectedSemesters.contains(semester)) {
                continue;
            }
            if (hasAnyScoreInSemester(targetRows, semester)) {
                continue;
            }
            copySemesterScoresToTarget(
                    studentId,
                    sourceRows,
                    targetSubjectId,
                    targetNamHoc,
                    classId,
                    semester,
                    currentUserIsAdmin,
                    fallbackTeacherId
            );
        }

        for (Integer semester : allSemesters) {
            scoreDAO.deleteScoresByGroupAndSemester(studentId, sourceSubjectId, sourceNamHoc, semester);
            scoreDAO.deleteAverageScoresByGroupAndSemester(studentId, sourceSubjectId, sourceNamHoc, semester);
        }
    }

    private void copySemesterScoresToTarget(String studentId,
                                            List<Object[]> sourceRows,
                                            String targetSubjectId,
                                            String targetNamHoc,
                                            String classId,
                                            int semester,
                                            boolean currentUserIsAdmin,
                                            String fallbackTeacherId) {
        if (sourceRows == null || sourceRows.isEmpty()) {
            return;
        }
        List<Object[]> rowsToCopy = sourceRows.stream()
                .filter(Objects::nonNull)
                .filter(row -> {
                    Integer hocKy = asInteger(row, 0);
                    Integer typeId = asInteger(row, 1);
                    BigDecimal scoreValue = asBigDecimal(row, 2);
                    return hocKy != null && hocKy == semester && typeId != null && scoreValue != null;
                })
                .toList();
        if (rowsToCopy.isEmpty()) {
            return;
        }

        String teacherId = resolveTeacherForCopiedSemester(
                rowsToCopy,
                targetSubjectId,
                targetNamHoc,
                classId,
                semester,
                currentUserIsAdmin,
                fallbackTeacherId
        );

        scoreDAO.deleteScoresByGroupAndSemester(studentId, targetSubjectId, targetNamHoc, semester);
        scoreDAO.deleteAverageScoresByGroupAndSemester(studentId, targetSubjectId, targetNamHoc, semester);
        for (Object[] row : rowsToCopy) {
            Integer typeId = asInteger(row, 1);
            BigDecimal scoreValue = asBigDecimal(row, 2);
            String note = row != null && row.length > 3 && row[3] != null ? row[3].toString() : "";
            String rowTeacherId = trimToNull(row != null && row.length > 4 && row[4] != null ? row[4].toString() : null);
            if (rowTeacherId == null) {
                rowTeacherId = teacherId;
            }
            scoreDAO.insertScoreEntry(
                    studentId,
                    targetSubjectId,
                    typeId,
                    targetNamHoc,
                    semester,
                    scoreValue,
                    rowTeacherId,
                    note
            );
        }
    }

    private String resolveTeacherForCopiedSemester(List<Object[]> rowsToCopy,
                                                   String targetSubjectId,
                                                   String targetNamHoc,
                                                   String classId,
                                                   int semester,
                                                   boolean currentUserIsAdmin,
                                                   String fallbackTeacherId) {
        String teacherId = null;
        if (rowsToCopy != null) {
            for (Object[] row : rowsToCopy) {
                String rawTeacherId = trimToNull(row != null && row.length > 4 && row[4] != null ? row[4].toString() : null);
                if (rawTeacherId != null) {
                    teacherId = rawTeacherId.toUpperCase(Locale.ROOT);
                    break;
                }
            }
        }
        if (teacherId == null) {
            teacherId = trimToNull(scoreDAO.findFirstAssignedTeacherForScore(targetSubjectId, classId, targetNamHoc, semester));
        }
        if (teacherId == null) {
            teacherId = trimToNull(fallbackTeacherId);
        }
        if (teacherId == null) {
            throw new RuntimeException("Không xác định được giáo viên chấm cho học kỳ " + (semester == 1 ? "I" : "II") + ".");
        }

        if (currentUserIsAdmin) {
            ensureAssignmentForAdmin(teacherId, targetSubjectId, classId, targetNamHoc, semester);
        } else {
            validateTeacherAssignment(teacherId, targetSubjectId, targetNamHoc, semester, classId);
        }
        return teacherId;
    }

    private String resolveTeacherForConduct(Map<Integer, String> semesterTeacherIds,
                                            int conductSemester,
                                            String fallbackTeacherId) {
        if (semesterTeacherIds == null || semesterTeacherIds.isEmpty()) {
            return fallbackTeacherId;
        }
        if (conductSemester == CONDUCT_YEAR) {
            String teacherHk2 = semesterTeacherIds.get(CONDUCT_SEMESTER_2);
            if (!isBlank(teacherHk2)) {
                return teacherHk2;
            }
            String teacherHk1 = semesterTeacherIds.get(CONDUCT_SEMESTER_1);
            if (!isBlank(teacherHk1)) {
                return teacherHk1;
            }
            return fallbackTeacherId;
        }
        String teacher = semesterTeacherIds.get(conductSemester);
        return isBlank(teacher) ? fallbackTeacherId : teacher;
    }

    private void upsertConductValue(String studentId,
                                    String namHoc,
                                    String teacherId,
                                    int hocKy,
                                    String rawValue,
                                    Set<Integer> allowedSemesters) {
        String value = normalizeConductValue(rawValue);
        if (value == null) {
            return;
        }
        if (hocKy != CONDUCT_YEAR && !allowedSemesters.contains(hocKy)) {
            return;
        }
        if (isBlank(teacherId)) {
            return;
        }
        scoreDAO.upsertConduct(studentId, namHoc, hocKy, value, "", teacherId);
    }

    private String normalizeConductValue(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        String lower = normalizeAsciiLower(normalized);
        if (lower.equals("tot")) {
            return CONDUCT_TOT;
        }
        if (lower.equals("kha")) {
            return CONDUCT_KHA;
        }
        if (lower.equals("trung binh") || lower.equals("trung_binh") || lower.equals("dat")) {
            return CONDUCT_TRUNG_BINH;
        }
        if (lower.equals("yeu") || lower.equals("chua dat")) {
            return CONDUCT_YEU;
        }
        return null;
    }

    private List<OptionItem> buildConductOptions() {
        return List.of(
                new OptionItem(CONDUCT_TOT, "Tốt"),
                new OptionItem(CONDUCT_KHA, "Khá"),
                new OptionItem(CONDUCT_TRUNG_BINH, "Trung bình"),
                new OptionItem(CONDUCT_YEU, "Yếu")
        );
    }

    private String buildFriendlySaveError(RuntimeException ex,
                                          StudentItem selectedStudent,
                                          String subjectName,
                                          List<Integer> targetSemesters) {
        String rootMessage = extractRootMessage(ex);
        String normalized = normalizeAsciiLower(rootMessage);

        if (normalized.contains("phan cong day mon nay cho lop cua hoc sinh")
                || normalized.contains("before score insert check permission")
                || normalized.contains("ban chua duoc phan cong day mon nay")) {
            String className = defaultIfBlank(selectedStudent == null ? null : selectedStudent.getClassName(), "-");
            String subjectDisplay = defaultIfBlank(subjectName, "môn đã chọn");
            String semesterLabel = targetSemesters.size() == 2
                    ? "học kỳ I/II"
                    : ("học kỳ " + (targetSemesters.contains(1) ? "I" : "II"));
            return "Không thể lưu điểm: bạn chưa được phân công dạy " + subjectDisplay
                    + " cho lớp " + className + " ở " + semesterLabel + ".";
        }

        if (normalized.contains("diem phai nam trong khoang tu 0 den 10")) {
            return "Không thể lưu điểm: điểm phải nằm trong khoảng từ 0 đến 10.";
        }

        if ((normalized.contains("constraint_1") || normalized.contains("id_gvcn"))
                && normalized.contains("conduct")) {
            return "Không thể cập nhật hạnh kiểm do ràng buộc giáo viên chủ nhiệm (conducts). "
                    + "Vui lòng kiểm tra lại giáo viên/GVCN hoặc để trống hạnh kiểm khi lưu điểm môn.";
        }
        if (normalized.contains("jdbc exception executing sql")) {
            return "Không thể lưu điểm do lỗi dữ liệu phát sinh từ hệ thống. Vui lòng thử lại hoặc liên hệ quản trị.";
        }

        if (rootMessage != null && !rootMessage.isBlank()) {
            return rootMessage;
        }
        return "Không thể lưu điểm. Vui lòng kiểm tra lại dữ liệu và phân công giảng dạy.";
    }

    private String extractRootMessage(Throwable throwable) {
        Throwable current = throwable;
        String last = null;
        while (current != null) {
            String message = trimToNull(current.getMessage());
            if (message != null) {
                last = message;
            }
            current = current.getCause();
        }
        return last;
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

    private String normalizeAsciiLower(String value) {
        return normalizeKey(value).replace('_', ' ');
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
        private final String classId;
        private final String grade;
        private final String courseId;

        public StudentItem(String id,
                           String name,
                           String className,
                           String classId,
                           String grade,
                           String courseId) {
            this.id = id;
            this.name = name;
            this.className = className;
            this.classId = classId;
            this.grade = grade;
            this.courseId = courseId;
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

        public String getClassId() {
            return classId;
        }

        public String getGrade() {
            return grade;
        }

        public String getCourseId() {
            return courseId;
        }
    }

    public static class TeacherItem {
        private final String id;
        private final String name;

        public TeacherItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String toDisplay() {
            return defaultDisplay(name, id);
        }

        private String defaultDisplay(String teacherName, String teacherId) {
            String safeName = teacherName == null || teacherName.isBlank() ? teacherId : teacherName;
            return safeName + " (" + teacherId + ")";
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
        private String teacherHk1;
        private String teacherHk2;
        private String applyFilter;

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

        public String getTeacherHk1() {
            return teacherHk1;
        }

        public void setTeacherHk1(String teacherHk1) {
            this.teacherHk1 = teacherHk1;
        }

        public String getTeacherHk2() {
            return teacherHk2;
        }

        public void setTeacherHk2(String teacherHk2) {
            this.teacherHk2 = teacherHk2;
        }

        public String getApplyFilter() {
            return applyFilter;
        }

        public void setApplyFilter(String applyFilter) {
            this.applyFilter = applyFilter;
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

    public static class ConductInput {
        private String value;

        public ConductInput(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
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
        private final String consistencyError;
        private final String filterValidationMessage;
        private final String existingScoreNotice;
        private final String formulaText;
        private final ConductInput hk1Conduct;
        private final ConductInput hk2Conduct;
        private final ConductInput yearConduct;
        private final List<OptionItem> conductOptions;
        private final List<OptionItem> teacherOptionsHk1;
        private final List<OptionItem> teacherOptionsHk2;

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
                                   String consistencyError,
                                   String filterValidationMessage,
                                   String existingScoreNotice,
                                   String formulaText,
                                   ConductInput hk1Conduct,
                                   ConductInput hk2Conduct,
                                   ConductInput yearConduct,
                                   List<OptionItem> conductOptions,
                                   List<OptionItem> teacherOptionsHk1,
                                   List<OptionItem> teacherOptionsHk2) {
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
            this.consistencyError = consistencyError;
            this.filterValidationMessage = filterValidationMessage;
            this.existingScoreNotice = existingScoreNotice;
            this.formulaText = formulaText;
            this.hk1Conduct = hk1Conduct;
            this.hk2Conduct = hk2Conduct;
            this.yearConduct = yearConduct;
            this.conductOptions = conductOptions;
            this.teacherOptionsHk1 = teacherOptionsHk1;
            this.teacherOptionsHk2 = teacherOptionsHk2;
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

        public ConductInput getHk1Conduct() {
            return hk1Conduct;
        }

        public ConductInput getHk2Conduct() {
            return hk2Conduct;
        }

        public ConductInput getYearConduct() {
            return yearConduct;
        }

        public List<OptionItem> getConductOptions() {
            return conductOptions;
        }

        public List<OptionItem> getTeacherOptionsHk1() {
            return teacherOptionsHk1;
        }

        public List<OptionItem> getTeacherOptionsHk2() {
            return teacherOptionsHk2;
        }

        public String getConsistencyError() {
            return consistencyError;
        }

        public String getFilterValidationMessage() {
            return filterValidationMessage;
        }

        public String getExistingScoreNotice() {
            return existingScoreNotice;
        }

        public boolean isReadyForInput() {
            return selectedStudent != null
                    && !isBlank(filter.getMon())
                    && !isBlank(filter.getNamHoc())
                    && isBlank(consistencyError);
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
        private String sourceMon;
        private String sourceNamHoc;
        private String sourceHocKy;

        private List<String> hk1Tx;
        private String hk1Midterm;
        private String hk1Final;
        private String hk1Teacher;
        private String hk1Conduct;

        private List<String> hk2Tx;
        private String hk2Midterm;
        private String hk2Final;
        private String hk2Teacher;
        private String hk2Conduct;
        private String yearConduct;

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

        public String getSourceMon() {
            return sourceMon;
        }

        public void setSourceMon(String sourceMon) {
            this.sourceMon = sourceMon;
        }

        public String getSourceNamHoc() {
            return sourceNamHoc;
        }

        public void setSourceNamHoc(String sourceNamHoc) {
            this.sourceNamHoc = sourceNamHoc;
        }

        public String getSourceHocKy() {
            return sourceHocKy;
        }

        public void setSourceHocKy(String sourceHocKy) {
            this.sourceHocKy = sourceHocKy;
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

        public String getHk1Teacher() {
            return hk1Teacher;
        }

        public void setHk1Teacher(String hk1Teacher) {
            this.hk1Teacher = hk1Teacher;
        }

        public String getHk1Conduct() {
            return hk1Conduct;
        }

        public void setHk1Conduct(String hk1Conduct) {
            this.hk1Conduct = hk1Conduct;
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

        public String getHk2Teacher() {
            return hk2Teacher;
        }

        public void setHk2Teacher(String hk2Teacher) {
            this.hk2Teacher = hk2Teacher;
        }

        public String getHk2Conduct() {
            return hk2Conduct;
        }

        public void setHk2Conduct(String hk2Conduct) {
            this.hk2Conduct = hk2Conduct;
        }

        public String getYearConduct() {
            return yearConduct;
        }

        public void setYearConduct(String yearConduct) {
            this.yearConduct = yearConduct;
        }
    }
}
