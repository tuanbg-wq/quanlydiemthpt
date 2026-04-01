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
import java.time.Period;
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
                student.setHistoryTypeDisplay("Chuyển lớp");
                student.setHistoryDetail("Từ " + history.getLopCu() + " sang " + history.getLopMoi());
            }
            return;
        }

        if (StudentClassHistoryService.CHUYEN_TRUONG.equals(historyType)) {
            StudentClassHistory history = historyService.getLatestHistoryByType(
                    student.getIdHocSinh(),
                    StudentClassHistoryService.CHUYEN_TRUONG
            );

            if (history != null) {
                student.setHistoryTypeDisplay("Chuyển trường");
                student.setHistoryDetail("Từ " + history.getTruongCu() + " sang " + history.getTruongMoi());
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
            throw new RuntimeException("Khóa học không được để trống.");
        }

        String lopId = norm(idLop);
        if (lopId == null) {
            throw new RuntimeException("Lớp không được để trống.");
        }

        validateKhoi(khoi);
        validateEnrollmentAge(student.getNgaySinh(), student.getNgayNhapHoc(), khoi);

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh"));
        populateConductForStudent(student);

        StudentSnapshot beforeSnapshot = snapshot(student);

        String oldStudentId = student.getIdHocSinh();
        String newStudentId = resolveUpdatedStudentId(oldStudentId, formStudent.getIdHocSinh());

        applyEditableStudentFields(student, formStudent);
        validateKhoi(khoi);
        validateEnrollmentAge(student.getNgaySinh(), student.getNgayNhapHoc(), khoi);

        String currentId = norm(currentClassId);
        if (currentId == null) {
            throw new RuntimeException("Lớp hiện tại không được để trống.");
        }

        ClassEntity currentClass = classDAO.findById(currentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp hiện tại"));

        Course course = resolveCourseForEdit(courseId, tenKhoa, student.getNgayNhapHoc());
        applyCourseAndGradeToClass(currentClass, course, khoi, student.getNgayNhapHoc());

        student.setLop(currentClass);

        String transferId = norm(transferClassId);
        if (transferId != null && !currentId.equals(transferId)) {
            ClassEntity transferClass = classDAO.findById(transferId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp chuyển đến"));

            historyService.saveClassHistory(
                    oldStudentId,
                    currentId,
                    transferId,
                    "Chuyển lớp từ trang sửa học sinh"
            );

            student.setLop(transferClass);
        }

        saveAvatarIfPresent(student, avatar, newStudentId);
        studentDAO.saveAndFlush(student);

        if (!oldStudentId.equals(newStudentId)) {
            try {
                int updatedRows = studentDAO.updateStudentId(oldStudentId, newStudentId);
                if (updatedRows != 1) {
                    throw new RuntimeException("Không thể cập nhật mã học sinh.");
                }

                historyService.rebindStudentId(oldStudentId, newStudentId);
                activityLogService.rebindStudentRecordId(oldStudentId, newStudentId);
            } catch (DataIntegrityViolationException ex) {
                throw new RuntimeException(
                        "Không thể đổi mã học sinh vì đã có dữ liệu liên quan "
                                + "(điểm, điểm trung bình, hạnh kiểm...)."
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
    // UPDATE / CHUYỂN LỚP NHANH
    // ================================
    @Transactional
    public void updateStudentClass(String studentId, String newClassId) {
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh"));

        ClassEntity newClass = classDAO.findById(newClassId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp"));

        String oldClass = student.getLop() != null ? student.getLop().getIdLop() : null;

        if (oldClass != null && !oldClass.equals(newClassId)) {
            historyService.saveClassHistory(studentId, oldClass, newClassId, "Chuyển lớp");
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
            throw new RuntimeException("Hạnh kiểm không được vượt quá 50 ký tự.");
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
            throw new RuntimeException("Khối phải là 10 / 11 / 12.");
        }
    }

    private void validateEnrollmentAge(LocalDate ngaySinh, LocalDate ngayNhapHoc, Integer khoi) {
        if (ngaySinh == null || ngayNhapHoc == null || khoi == null) {
            return;
        }

        if (!ngayNhapHoc.isAfter(ngaySinh)) {
            throw new RuntimeException("Ngày nhập học phải sau ngày sinh.");
        }

        int tuoi = Period.between(ngaySinh, ngayNhapHoc).getYears();
        int tuoiToiThieu = switch (khoi) {
            case 10 -> 14;
            case 11 -> 15;
            case 12 -> 16;
            default -> 14;
        };

        if (tuoi < tuoiToiThieu) {
            throw new RuntimeException(
                    "Học sinh khối " + khoi + " phải đủ " + tuoiToiThieu
                            + " tuổi tại ngày nhập học. Tuổi hiện tại: " + tuoi + "."
            );
        }

        if (tuoi > 25) {
            throw new RuntimeException("Tuổi tại ngày nhập học không hợp lệ (tối đa 25 tuổi).");
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

        addChange(changes, "Mã học sinh", before.idHocSinh, after.idHocSinh);
        addChange(changes, "Họ tên", before.hoTen, after.hoTen);
        addChange(changes, "Ngày sinh", before.ngaySinh, after.ngaySinh);
        addChange(changes, "Giới tính", formatGender(before.gioiTinh), formatGender(after.gioiTinh));
        addChange(changes, "Nơi sinh", before.noiSinh, after.noiSinh);
        addChange(changes, "Dân tộc", before.danToc, after.danToc);
        addChange(changes, "Số điện thoại", before.soDienThoai, after.soDienThoai);
        addChange(changes, "Email", before.email, after.email);
        addChange(changes, "Địa chỉ", before.diaChi, after.diaChi);
        addChange(changes, "Họ tên cha", before.hoTenCha, after.hoTenCha);
        addChange(changes, "SĐT cha", before.sdtCha, after.sdtCha);
        addChange(changes, "Họ tên mẹ", before.hoTenMe, after.hoTenMe);
        addChange(changes, "SĐT mẹ", before.sdtMe, after.sdtMe);
        addChange(changes, "Ngày nhập học", before.ngayNhapHoc, after.ngayNhapHoc);
        addChange(changes, "Trạng thái", formatStatus(before.trangThai), formatStatus(after.trangThai));
        addChange(changes, "Hạnh kiểm HK1", formatConduct(before.hanhKiemHocKy1), formatConduct(after.hanhKiemHocKy1));
        addChange(changes, "Hạnh kiểm HK2", formatConduct(before.hanhKiemHocKy2), formatConduct(after.hanhKiemHocKy2));
        addChange(changes, "Hạnh kiểm cả năm", formatConduct(before.hanhKiemCaNam), formatConduct(after.hanhKiemCaNam));
        addChange(changes, "Lớp", before.idLop, after.idLop);
        addChange(changes, "Khối", before.khoi, after.khoi);
        addChange(changes, "Mã khóa", before.idKhoa, after.idKhoa);
        addChange(changes, "Tên khóa", before.tenKhoa, after.tenKhoa);

        if (avatarChanged) {
            changes.add("Ảnh học sinh: đã cập nhật");
        }

        if (changes.isEmpty()) {
            return "Cập nhật hồ sơ học sinh (không thay đổi dữ liệu).";
        }

        StringBuilder sb = new StringBuilder("Các thay đổi:\n");
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
            return "(trống)";
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
            throw new RuntimeException("Mã học sinh không được để trống.");
        }

        if (!oldId.equals(newId) && studentDAO.existsById(newId)) {
            throw new RuntimeException("Mã học sinh đã tồn tại.");
        }

        return newId;
    }

    private Course resolveCourseForEdit(String courseId, String tenKhoa, LocalDate ngayNhapHoc) {
        String cId = norm(courseId);
        if (cId == null) {
            throw new RuntimeException("Khóa học không được để trống.");
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
            throw new RuntimeException("Dữ liệu học sinh không hợp lệ.");
        }

        String hsId = norm(student.getIdHocSinh());
        if (hsId == null) {
            throw new RuntimeException("Mã học sinh không được để trống.");
        }

        student.setIdHocSinh(hsId);

        if (studentDAO.existsById(hsId)) {
            throw new RuntimeException("Mã học sinh đã tồn tại.");
        }

        applyEditableStudentFields(student, student);
    }

    private void applyEditableStudentFields(Student target, Student source) {
        String hoTen = norm(source.getHoTen());
        if (hoTen == null) {
            throw new RuntimeException("Họ tên không được để trống.");
        }

        if (source.getNgaySinh() == null) {
            throw new RuntimeException("Ngày sinh không được để trống.");
        }

        if (source.getNgayNhapHoc() == null) {
            throw new RuntimeException("Ngày nhập học không được để trống.");
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
            return "Nữ";
        }
        return norm(value);
    }

    private String normalizeConduct(String value) {
        String normalized = normalizeAsciiLower(value).replace('-', '_').replace(' ', '_');
        if (normalized.isBlank()) {
            return null;
        }
        return switch (normalized) {
            case "tot", "gioi" -> "Tốt";
            case "kha" -> "Khá";
            case "trung_binh", "tb" -> "Trung bình";
            case "yeu" -> "Yếu";
            case "kem" -> "Kém";
            default -> norm(value);
        };
    }

    private String formatStatus(String value) {
        String normalized = normalizeStatus(value);
        return switch (normalized) {
            case "dang_hoc" -> "Đang học";
            case "da_tot_nghiep" -> "Đã tốt nghiệp";
            case "bo_hoc" -> "Bỏ học";
            case "chuyen_truong" -> "Chuyển trường";
            case "bao_luu" -> "Bảo lưu";
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
            ten = "Khóa " + courseId;
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
                    "Lớp " + idLop + " đã thuộc khóa "
                            + lop.getKhoaHoc().getIdKhoa()
                            + ", không thể gán sang khóa "
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

