package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.ConductDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.ConductRecord;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.entity.StudentClassHistory;
import com.quanly.webdiem.model.service.shared.ClassCodeSupport;
import com.quanly.webdiem.model.service.FileStorageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class StudentService {

    private static final int HOC_KY_CA_NAM = 0;
    private static final int HOC_KY_1 = 1;
    private static final int HOC_KY_2 = 2;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StudentClassHistoryService historyService;
    private final StudentDAO studentDAO;
    private final ConductDAO conductDAO;
    private final ClassDAO classDAO;
    private final CourseDAO courseDAO;
    private final FileStorageService fileStorageService;
    private final ActivityLogService activityLogService;

    public StudentService(StudentDAO studentDAO,
                          ConductDAO conductDAO,
                          ClassDAO classDAO,
                          CourseDAO courseDAO,
                          FileStorageService fileStorageService,
                          StudentClassHistoryService historyService,
                          ActivityLogService activityLogService) {
        this.studentDAO = studentDAO;
        this.conductDAO = conductDAO;
        this.classDAO = classDAO;
        this.courseDAO = courseDAO;
        this.fileStorageService = fileStorageService;
        this.historyService = historyService;
        this.activityLogService = activityLogService;
    }

    private String norm(String s) {
        if (s == null) {
            return null;
        }

        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String buildNamHoc(LocalDate ngayNhapHoc) {
        int year = ngayNhapHoc.getYear();
        int month = ngayNhapHoc.getMonthValue();

        if (month >= 8) {
            return year + "-" + (year + 1);
        }

        return (year - 1) + "-" + year;
    }

    // ================================
    // SEARCH
    // ================================
    public List<Student> search(StudentSearch search) {
        String q = search == null ? null : norm(search.getQ());
        String courseId = search == null ? null : norm(search.getCourseId());
        String khoi = search == null ? null : norm(search.getKhoi());
        String classId = search == null ? null : norm(search.getClassId());
        String historyType = search == null ? null : norm(search.getHistoryType());

        List<Student> students = studentDAO.search(q, courseId, khoi, classId);

        for (Student student : students) {
            applyHistoryDisplay(student, historyType);
            populateConductForStudent(student);
        }

        if (isHistoryFilter(historyType)) {
            return students.stream()
                    .filter(s -> historyService.hasHistoryByType(s.getIdHocSinh(), historyType))
                    .toList();
        }

        return students;
    }

    private boolean isHistoryFilter(String historyType) {
        return StudentClassHistoryService.CHUYEN_LOP.equals(historyType)
                || StudentClassHistoryService.CHUYEN_TRUONG.equals(historyType);
    }

    private void applyHistoryDisplay(Student student, String historyType) {
        student.setPreviousClass(historyService.getPreviousClass(student.getIdHocSinh()));
        student.setHistoryTypeDisplay(null);
        student.setHistoryDetail(null);

        if (StudentClassHistoryService.CHUYEN_LOP.equals(historyType)) {
            StudentClassHistory history = historyService.getLatestHistoryByType(
                    student.getIdHocSinh(),
                    StudentClassHistoryService.CHUYEN_LOP
            );

            if (history != null) {
                student.setHistoryTypeDisplay("Chuyá»ƒn lá»›p");
                student.setHistoryDetail("Tá»« " + history.getLopCu() + " sang " + history.getLopMoi());
            }
            return;
        }

        if (StudentClassHistoryService.CHUYEN_TRUONG.equals(historyType)) {
            StudentClassHistory history = historyService.getLatestHistoryByType(
                    student.getIdHocSinh(),
                    StudentClassHistoryService.CHUYEN_TRUONG
            );

            if (history != null) {
                student.setHistoryTypeDisplay("Chuyá»ƒn trÆ°á»ng");
                student.setHistoryDetail("Tá»« " + history.getTruongCu() + " sang " + history.getTruongMoi());
            }
        }
    }

    // ================================
    // CREATE STUDENT
    // ================================
    @Transactional
    public void createWithAutoCourseClass(Student student,
                                          String courseId,
                                          String tenKhoa,
                                          String idLop,
                                          Integer khoi,
                                          MultipartFile avatar) {
        createWithAutoCourseClass(student, courseId, tenKhoa, idLop, khoi, avatar, null, null);
    }

    @Transactional
    public void createWithAutoCourseClass(Student student,
                                          String courseId,
                                          String tenKhoa,
                                          String idLop,
                                          Integer khoi,
                                          MultipartFile avatar,
                                          String operatorUsername,
                                          String ipAddress) {

        validateAndNormalizeStudentForCreate(student);

        String cId = norm(courseId);
        if (cId == null) {
            throw new RuntimeException("KhĂ³a há»c khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        String lopId = norm(idLop);
        if (lopId == null) {
            throw new RuntimeException("Lá»›p khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        validateKhoi(khoi);

        Course course = upsertCourse(cId, tenKhoa, student.getNgayNhapHoc());
        ClassEntity lop = upsertClass(lopId, khoi, course, student.getNgayNhapHoc());

        student.setLop(lop);
        saveAvatarIfPresent(student, avatar, student.getIdHocSinh());
        studentDAO.save(student);
        syncConductsForStudent(
                student.getIdHocSinh(),
                resolveStudentNamHoc(student),
                student.getHanhKiemHocKy1(),
                student.getHanhKiemHocKy2(),
                student.getHanhKiemCaNam()
        );
        activityLogService.logStudentCreate(
                student.getIdHocSinh(),
                operatorUsername,
                buildStudentCreateSummary(student),
                ipAddress
        );
    }

    // ================================
    // UPDATE STUDENT
    // ================================
    @Transactional
    public void updateStudent(String id,
                              Student formStudent,
                              String courseId,
                              String tenKhoa,
                              Integer khoi,
                              String currentClassId,
                              String transferClassId,
                              MultipartFile avatar,
                              String operatorUsername,
                              String ipAddress) {

        Student student = studentDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y há»c sinh"));
        populateConductForStudent(student);

        StudentSnapshot beforeSnapshot = snapshot(student);

        String oldStudentId = student.getIdHocSinh();
        String newStudentId = resolveUpdatedStudentId(oldStudentId, formStudent.getIdHocSinh());

        applyEditableStudentFields(student, formStudent);
        validateKhoi(khoi);

        String currentId = norm(currentClassId);
        if (currentId == null) {
            throw new RuntimeException("Lá»›p hiá»‡n táº¡i khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        ClassEntity currentClass = classDAO.findById(currentId)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y lá»›p hiá»‡n táº¡i"));

        Course course = resolveCourseForEdit(courseId, tenKhoa, student.getNgayNhapHoc());
        applyCourseAndGradeToClass(currentClass, course, khoi, student.getNgayNhapHoc());

        student.setLop(currentClass);

        String transferId = norm(transferClassId);
        if (transferId != null && !currentId.equals(transferId)) {
            ClassEntity transferClass = classDAO.findById(transferId)
                    .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y lá»›p chuyá»ƒn Ä‘áº¿n"));

            historyService.saveClassHistory(
                    oldStudentId,
                    currentId,
                    transferId,
                    "Chuyá»ƒn lá»›p tá»« trang sá»­a há»c sinh"
            );

            student.setLop(transferClass);
        }

        saveAvatarIfPresent(student, avatar, newStudentId);
        studentDAO.saveAndFlush(student);

        if (!oldStudentId.equals(newStudentId)) {
            try {
                int updatedRows = studentDAO.updateStudentId(oldStudentId, newStudentId);
                if (updatedRows != 1) {
                    throw new RuntimeException("KhĂ´ng thá»ƒ cáº­p nháº­t mĂ£ há»c sinh.");
                }

                historyService.rebindStudentId(oldStudentId, newStudentId);
                activityLogService.rebindStudentRecordId(oldStudentId, newStudentId);
            } catch (DataIntegrityViolationException ex) {
                throw new RuntimeException(
                        "KhĂ´ng thá»ƒ Ä‘á»•i mĂ£ há»c sinh vĂ¬ Ä‘Ă£ cĂ³ dá»¯ liá»‡u liĂªn quan "
                                + "(Ä‘iá»ƒm, Ä‘iá»ƒm trung bĂ¬nh, háº¡nh kiá»ƒm...)."
                );
            }
        }

        String namHocForConduct = resolveStudentNamHoc(student);
        if (!oldStudentId.equals(newStudentId) && namHocForConduct != null) {
            conductDAO.deleteRecordsByStudentIdAndNamHoc(oldStudentId, namHocForConduct);
        }
        syncConductsForStudent(
                newStudentId,
                namHocForConduct,
                formStudent.getHanhKiemHocKy1(),
                formStudent.getHanhKiemHocKy2(),
                formStudent.getHanhKiemCaNam()
        );
        populateConductForStudent(student);

        StudentSnapshot afterSnapshot = snapshot(student);
        afterSnapshot.idHocSinh = newStudentId;

        String summary = buildChangeSummary(beforeSnapshot, afterSnapshot, avatar != null && !avatar.isEmpty());
        activityLogService.logStudentUpdate(newStudentId, operatorUsername, summary, ipAddress);
    }

    // ================================
    // UPDATE / CHUYá»‚N Lá»P NHANH
    // ================================
    @Transactional
    public void updateStudentClass(String studentId, String newClassId) {
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y há»c sinh"));

        ClassEntity newClass = classDAO.findById(newClassId)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y lá»›p"));

        String oldClass = student.getLop() != null ? student.getLop().getIdLop() : null;

        if (oldClass != null && !oldClass.equals(newClassId)) {
            historyService.saveClassHistory(studentId, oldClass, newClassId, "Chuyá»ƒn lá»›p");
            student.setLop(newClass);
            studentDAO.save(student);
        }
    }

    @Transactional
    public void deleteStudent(String studentId) {
        deleteStudent(studentId, null, null);
    }

    @Transactional
    public void deleteStudent(String studentId, String operatorUsername, String ipAddress) {
        String normalizedStudentId = norm(studentId);
        if (normalizedStudentId == null) {
            throw new RuntimeException("Mã học sinh không hợp lệ.");
        }

        Student student = studentDAO.findById(normalizedStudentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh."));
        String deleteSummary = buildStudentDeleteSummary(student);

        try {
            studentDAO.delete(student);
            studentDAO.flush();
            activityLogService.logStudentDelete(normalizedStudentId, operatorUsername, deleteSummary, ipAddress);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể xóa học sinh vì dữ liệu liên quan đang tồn tại.");
        }
    }

    @Transactional(readOnly = true)
    public void populateConductForStudent(Student student) {
        if (student == null || norm(student.getIdHocSinh()) == null) {
            return;
        }

        String namHoc = resolveStudentNamHoc(student);
        if (namHoc == null) {
            student.setHanhKiemHocKy1(null);
            student.setHanhKiemHocKy2(null);
            student.setHanhKiemCaNam(null);
            return;
        }

        List<ConductRecord> records = conductDAO.findRecordsByStudentIdAndNamHoc(student.getIdHocSinh(), namHoc);
        student.setHanhKiemHocKy1(extractConductValue(records, HOC_KY_1));
        student.setHanhKiemHocKy2(extractConductValue(records, HOC_KY_2));
        student.setHanhKiemCaNam(extractConductValue(records, HOC_KY_CA_NAM));
    }

    private String extractConductValue(List<ConductRecord> records, int hocKy) {
        if (records == null || records.isEmpty()) {
            return null;
        }

        for (ConductRecord record : records) {
            if (record == null || record.getHocKy() == null) {
                continue;
            }
            if (record.getHocKy() == hocKy) {
                return norm(record.getXepLoai());
            }
        }

        return null;
    }

    private String resolveStudentNamHoc(Student student) {
        if (student == null) {
            return null;
        }

        if (student.getLop() != null) {
            String classSchoolYear = norm(student.getLop().getNamHoc());
            if (classSchoolYear != null) {
                return classSchoolYear;
            }
        }

        if (student.getNgayNhapHoc() != null) {
            return buildNamHoc(student.getNgayNhapHoc());
        }

        return null;
    }

    private void syncConductsForStudent(String studentId,
                                        String namHoc,
                                        String hanhKiemHk1,
                                        String hanhKiemHk2,
                                        String hanhKiemCaNam) {
        String normalizedStudentId = norm(studentId);
        String normalizedNamHoc = norm(namHoc);
        if (normalizedStudentId == null || normalizedNamHoc == null) {
            return;
        }

        upsertOrDeleteConductRecord(normalizedStudentId, normalizedNamHoc, HOC_KY_1, hanhKiemHk1);
        upsertOrDeleteConductRecord(normalizedStudentId, normalizedNamHoc, HOC_KY_2, hanhKiemHk2);
        upsertOrDeleteConductRecord(normalizedStudentId, normalizedNamHoc, HOC_KY_CA_NAM, hanhKiemCaNam);
    }

    private void upsertOrDeleteConductRecord(String studentId,
                                             String namHoc,
                                             int hocKy,
                                             String rawValue) {
        String value = normalizeConduct(rawValue);
        if (value != null && value.length() > 50) {
            throw new RuntimeException("Háº¡nh kiá»ƒm khĂ´ng Ä‘Æ°á»£c vÆ°á»£t quĂ¡ 50 kĂ½ tá»±.");
        }

        if (value == null) {
            conductDAO.deleteRecordByStudentIdAndNamHocAndHocKy(studentId, namHoc, hocKy);
            return;
        }

        ConductRecord record = new ConductRecord();
        record.setIdHocSinh(studentId);
        record.setNamHoc(namHoc);
        record.setHocKy(hocKy);
        record.setXepLoai(value);
        record.setNhanXet(null);
        record.setIdGvcn(null);
        conductDAO.save(record);
    }

    private void saveAvatarIfPresent(Student student, MultipartFile avatar, String studentIdForFileName) {
        if (avatar == null || avatar.isEmpty()) {
            return;
        }

        String savedPath = fileStorageService.saveStudentAvatar(studentIdForFileName, avatar);
        student.setAnh(savedPath);
    }

    private void validateKhoi(Integer khoi) {
        if (khoi == null || !(khoi == 10 || khoi == 11 || khoi == 12)) {
            throw new RuntimeException("Khá»‘i pháº£i lĂ  10 / 11 / 12.");
        }
    }

    private StudentSnapshot snapshot(Student student) {
        StudentSnapshot s = new StudentSnapshot();
        s.idHocSinh = norm(student.getIdHocSinh());
        s.hoTen = norm(student.getHoTen());
        s.ngaySinh = student.getNgaySinh();
        s.gioiTinh = norm(student.getGioiTinh());
        s.noiSinh = norm(student.getNoiSinh());
        s.danToc = norm(student.getDanToc());
        s.soDienThoai = norm(student.getSoDienThoai());
        s.email = norm(student.getEmail());
        s.diaChi = norm(student.getDiaChi());
        s.hoTenCha = norm(student.getHoTenCha());
        s.sdtCha = norm(student.getSdtCha());
        s.hoTenMe = norm(student.getHoTenMe());
        s.sdtMe = norm(student.getSdtMe());
        s.ngayNhapHoc = student.getNgayNhapHoc();
        s.trangThai = norm(student.getTrangThai());
        s.hanhKiemHocKy1 = norm(student.getHanhKiemHocKy1());
        s.hanhKiemHocKy2 = norm(student.getHanhKiemHocKy2());
        s.hanhKiemCaNam = norm(student.getHanhKiemCaNam());

        if (student.getLop() != null) {
            s.idLop = norm(student.getLop().getIdLop());
            s.khoi = student.getLop().getKhoi();

            if (student.getLop().getKhoaHoc() != null) {
                s.idKhoa = norm(student.getLop().getKhoaHoc().getIdKhoa());
                s.tenKhoa = norm(student.getLop().getKhoaHoc().getTenKhoa());
            }
        }

        return s;
    }

    private String buildChangeSummary(StudentSnapshot before,
                                      StudentSnapshot after,
                                      boolean avatarChanged) {
        List<String> changes = new ArrayList<>();

        addChange(changes, "MĂ£ há»c sinh", before.idHocSinh, after.idHocSinh);
        addChange(changes, "Há» tĂªn", before.hoTen, after.hoTen);
        addChange(changes, "NgĂ y sinh", before.ngaySinh, after.ngaySinh);
        addChange(changes, "Giá»›i tĂ­nh", formatGender(before.gioiTinh), formatGender(after.gioiTinh));
        addChange(changes, "NÆ¡i sinh", before.noiSinh, after.noiSinh);
        addChange(changes, "DĂ¢n tá»™c", before.danToc, after.danToc);
        addChange(changes, "Sá»‘ Ä‘iá»‡n thoáº¡i", before.soDienThoai, after.soDienThoai);
        addChange(changes, "Email", before.email, after.email);
        addChange(changes, "Äá»‹a chá»‰", before.diaChi, after.diaChi);
        addChange(changes, "Há» tĂªn cha", before.hoTenCha, after.hoTenCha);
        addChange(changes, "SÄT cha", before.sdtCha, after.sdtCha);
        addChange(changes, "Há» tĂªn máº¹", before.hoTenMe, after.hoTenMe);
        addChange(changes, "SÄT máº¹", before.sdtMe, after.sdtMe);
        addChange(changes, "NgĂ y nháº­p há»c", before.ngayNhapHoc, after.ngayNhapHoc);
        addChange(changes, "Tráº¡ng thĂ¡i", formatStatus(before.trangThai), formatStatus(after.trangThai));
        addChange(changes, "Háº¡nh kiá»ƒm HK1", formatConduct(before.hanhKiemHocKy1), formatConduct(after.hanhKiemHocKy1));
        addChange(changes, "Háº¡nh kiá»ƒm HK2", formatConduct(before.hanhKiemHocKy2), formatConduct(after.hanhKiemHocKy2));
        addChange(changes, "Háº¡nh kiá»ƒm cáº£ nÄƒm", formatConduct(before.hanhKiemCaNam), formatConduct(after.hanhKiemCaNam));
        addChange(changes, "Lá»›p", before.idLop, after.idLop);
        addChange(changes, "Khá»‘i", before.khoi, after.khoi);
        addChange(changes, "MĂ£ khĂ³a", before.idKhoa, after.idKhoa);
        addChange(changes, "TĂªn khĂ³a", before.tenKhoa, after.tenKhoa);

        if (avatarChanged) {
            changes.add("áº¢nh há»c sinh: Ä‘Ă£ cáº­p nháº­t");
        }

        if (changes.isEmpty()) {
            return "Cáº­p nháº­t há»“ sÆ¡ há»c sinh (khĂ´ng thay Ä‘á»•i dá»¯ liá»‡u).";
        }

        StringBuilder sb = new StringBuilder("CĂ¡c thay Ä‘á»•i:\n");
        for (String change : changes) {
            sb.append("- ").append(change).append('\n');
        }

        return sb.toString().trim();
    }

    private String buildStudentCreateSummary(Student student) {
        if (student == null) {
            return "Thêm học sinh mới.";
        }
        String studentId = norm(student.getIdHocSinh());
        String studentName = norm(student.getHoTen());
        String className = student.getLop() == null ? null : norm(student.getLop().getMaVaTenLop());
        return "Thêm học sinh: " + safeStudentText(studentName, studentId)
                + ". Lớp: " + (className == null ? "(trống)" : className) + ".";
    }

    private String buildStudentDeleteSummary(Student student) {
        if (student == null) {
            return "Xóa học sinh.";
        }
        String studentId = norm(student.getIdHocSinh());
        String studentName = norm(student.getHoTen());
        String className = student.getLop() == null ? null : norm(student.getLop().getMaVaTenLop());
        return "Xóa học sinh: " + safeStudentText(studentName, studentId)
                + ". Lớp trước khi xóa: " + (className == null ? "(trống)" : className) + ".";
    }

    private String safeStudentText(String studentName, String studentId) {
        if (studentName != null && studentId != null) {
            return studentName + " (" + studentId + ")";
        }
        if (studentName != null) {
            return studentName;
        }
        if (studentId != null) {
            return studentId;
        }
        return "(không rõ)";
    }

    private void addChange(List<String> changes, String label, Object before, Object after) {
        if (Objects.equals(before, after)) {
            return;
        }

        changes.add(label + ": " + formatValue(before) + " -> " + formatValue(after));
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "(trá»‘ng)";
        }
        if (value instanceof LocalDate localDate) {
            return DATE_FORMAT.format(localDate);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(localDateTime);
        }
        return value.toString();
    }

    private String resolveUpdatedStudentId(String oldId, String candidateId) {
        String newId = norm(candidateId);
        if (newId == null) {
            throw new RuntimeException("MĂ£ há»c sinh khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        if (!oldId.equals(newId) && studentDAO.existsById(newId)) {
            throw new RuntimeException("MĂ£ há»c sinh Ä‘Ă£ tá»“n táº¡i.");
        }

        return newId;
    }

    private Course resolveCourseForEdit(String courseId, String tenKhoa, LocalDate ngayNhapHoc) {
        String cId = norm(courseId);
        if (cId == null) {
            throw new RuntimeException("KhĂ³a há»c khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        return upsertCourse(cId, tenKhoa, ngayNhapHoc);
    }

    private void applyCourseAndGradeToClass(ClassEntity currentClass,
                                            Course course,
                                            Integer khoi,
                                            LocalDate ngayNhapHoc) {
        currentClass.setKhoaHoc(course);
        currentClass.setKhoi(khoi);

        if (currentClass.getTenLop() == null || currentClass.getTenLop().isBlank()) {
            String normalizedClassCode = ClassCodeSupport.normalizeUpperAlphaNumeric(currentClass.getIdLop());
            String courseId = course == null ? null : course.getIdKhoa();
            try {
                String className = ClassCodeSupport.buildFromClassCode(courseId, normalizedClassCode, khoi).className();
                currentClass.setTenLop(className);
            } catch (IllegalArgumentException ex) {
                currentClass.setTenLop(currentClass.getIdLop());
            }
        }

        if (currentClass.getNamHoc() == null || currentClass.getNamHoc().isBlank()) {
            currentClass.setNamHoc(buildNamHoc(ngayNhapHoc));
        }

        classDAO.save(currentClass);
    }

    private void validateAndNormalizeStudentForCreate(Student student) {
        if (student == null) {
            throw new RuntimeException("Dá»¯ liá»‡u há»c sinh khĂ´ng há»£p lá»‡.");
        }

        String hsId = norm(student.getIdHocSinh());
        if (hsId == null) {
            throw new RuntimeException("MĂ£ há»c sinh khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        student.setIdHocSinh(hsId);

        if (studentDAO.existsById(hsId)) {
            throw new RuntimeException("MĂ£ há»c sinh Ä‘Ă£ tá»“n táº¡i.");
        }

        applyEditableStudentFields(student, student);
    }

    private void applyEditableStudentFields(Student target, Student source) {
        String hoTen = norm(source.getHoTen());
        if (hoTen == null) {
            throw new RuntimeException("Há» tĂªn khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        if (source.getNgaySinh() == null) {
            throw new RuntimeException("NgĂ y sinh khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        if (source.getNgayNhapHoc() == null) {
            throw new RuntimeException("NgĂ y nháº­p há»c khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        target.setHoTen(hoTen);
        target.setNgaySinh(source.getNgaySinh());
        target.setNgayNhapHoc(source.getNgayNhapHoc());

        target.setGioiTinh(normalizeGender(source.getGioiTinh()));
        target.setNoiSinh(norm(source.getNoiSinh()));
        target.setDanToc(norm(source.getDanToc()));
        target.setSoDienThoai(norm(source.getSoDienThoai()));
        target.setEmail(norm(source.getEmail()));
        target.setDiaChi(norm(source.getDiaChi()));
        target.setHoTenCha(norm(source.getHoTenCha()));
        target.setSdtCha(norm(source.getSdtCha()));
        target.setHoTenMe(norm(source.getHoTenMe()));
        target.setSdtMe(norm(source.getSdtMe()));
        target.setHanhKiemHocKy1(normalizeConduct(source.getHanhKiemHocKy1()));
        target.setHanhKiemHocKy2(normalizeConduct(source.getHanhKiemHocKy2()));
        target.setHanhKiemCaNam(normalizeConduct(source.getHanhKiemCaNam()));

        target.setTrangThai(normalizeStatus(source.getTrangThai()));
    }

    private String normalizeStatus(String value) {
        String normalized = normalizeAsciiLower(value).replace('-', '_').replace(' ', '_');
        if (normalized.isBlank()) {
            return "dang_hoc";
        }
        return switch (normalized) {
            case "dang_hoc", "da_tot_nghiep", "bo_hoc", "chuyen_truong", "bao_luu" -> normalized;
            default -> "dang_hoc";
        };
    }

    private String normalizeGender(String value) {
        String normalized = normalizeAsciiLower(value);
        if (normalized.isBlank()) {
            return null;
        }
        if ("nam".equals(normalized)) {
            return "Nam";
        }
        if ("nu".equals(normalized)) {
            return "Ná»¯";
        }
        return norm(value);
    }

    private String normalizeConduct(String value) {
        String normalized = normalizeAsciiLower(value).replace('-', '_').replace(' ', '_');
        if (normalized.isBlank()) {
            return null;
        }
        return switch (normalized) {
            case "tot", "gioi" -> "Tá»‘t";
            case "kha" -> "KhĂ¡";
            case "trung_binh", "tb" -> "Trung bĂ¬nh";
            case "yeu" -> "Yáº¿u";
            case "kem" -> "KĂ©m";
            default -> norm(value);
        };
    }

    private String formatStatus(String value) {
        String normalized = normalizeStatus(value);
        return switch (normalized) {
            case "dang_hoc" -> "Äang há»c";
            case "da_tot_nghiep" -> "ÄĂ£ tá»‘t nghiá»‡p";
            case "bo_hoc" -> "Bá» há»c";
            case "chuyen_truong" -> "Chuyá»ƒn trÆ°á»ng";
            case "bao_luu" -> "Báº£o lÆ°u";
            default -> value;
        };
    }

    private String formatGender(String value) {
        return normalizeGender(value);
    }

    private String formatConduct(String value) {
        return normalizeConduct(value);
    }

    private String normalizeAsciiLower(String value) {
        String normalized = norm(value);
        if (normalized == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    // ================================
    // UPSERT COURSE
    // ================================
    private Course upsertCourse(String courseId, String tenKhoa, LocalDate ngayNhapHoc) {
        Course course = courseDAO.findById(courseId).orElse(null);
        if (course != null) {
            String tenDaNhap = norm(tenKhoa);
            if (tenDaNhap != null && !tenDaNhap.equals(course.getTenKhoa())) {
                course.setTenKhoa(tenDaNhap);
                return courseDAO.save(course);
            }

            return course;
        }

        String ten = norm(tenKhoa);
        if (ten == null) {
            ten = "KhĂ³a " + courseId;
        }

        Course newCourse = new Course();
        newCourse.setIdKhoa(courseId);
        newCourse.setTenKhoa(ten);
        newCourse.setNgayBatDau(ngayNhapHoc);
        newCourse.setNgayKetThuc(null);
        newCourse.setTrangThai("dang_hoc");

        return courseDAO.save(newCourse);
    }

    // ================================
    // UPSERT CLASS
    // ================================
    private ClassEntity upsertClass(String idLop,
                                    Integer khoi,
                                    Course course,
                                    LocalDate ngayNhapHoc) {
        String courseId = course == null ? null : course.getIdKhoa();
        ClassCodeSupport.ClassCodeParts classCodeParts;
        try {
            classCodeParts = ClassCodeSupport.buildFromClassCode(courseId, idLop, khoi);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        String classCode = classCodeParts.classCode();
        String className = classCodeParts.className();

        ClassEntity lop = classDAO.findById(classCode).orElse(null);

        if (lop == null) {
            ClassEntity newClass = new ClassEntity();
            newClass.setIdLop(classCode);
            newClass.setTenLop(className);
            newClass.setKhoi(khoi);
            newClass.setNamHoc(buildNamHoc(ngayNhapHoc));
            newClass.setKhoaHoc(course);
            newClass.setSiSo(0);

            return classDAO.save(newClass);
        }

        if (lop.getTenLop() == null || lop.getTenLop().isBlank()) {
            lop.setTenLop(className);
        }

        if (lop.getKhoaHoc() != null
                && lop.getKhoaHoc().getIdKhoa() != null
                && !lop.getKhoaHoc().getIdKhoa().equals(course.getIdKhoa())) {
            throw new RuntimeException(
                    "Lá»›p " + idLop + " Ä‘Ă£ thuá»™c khĂ³a "
                            + lop.getKhoaHoc().getIdKhoa()
                            + ", khĂ´ng thá»ƒ gĂ¡n sang khĂ³a "
                            + course.getIdKhoa() + "."
            );
        }

        return lop;
    }

    private static class StudentSnapshot {
        private String idHocSinh;
        private String hoTen;
        private LocalDate ngaySinh;
        private String gioiTinh;
        private String noiSinh;
        private String danToc;
        private String soDienThoai;
        private String email;
        private String diaChi;
        private String hoTenCha;
        private String sdtCha;
        private String hoTenMe;
        private String sdtMe;
        private LocalDate ngayNhapHoc;
        private String trangThai;
        private String hanhKiemHocKy1;
        private String hanhKiemHocKy2;
        private String hanhKiemCaNam;
        private String idLop;
        private Integer khoi;
        private String idKhoa;
        private String tenKhoa;
    }
}


