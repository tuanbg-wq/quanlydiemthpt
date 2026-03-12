package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.StudentDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.entity.Student;
import com.quanly.webdiem.model.entity.StudentClassHistory;
import com.quanly.webdiem.model.service.FileStorageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class StudentService {

    private final StudentClassHistoryService historyService;
    private final StudentDAO studentDAO;
    private final ClassDAO classDAO;
    private final CourseDAO courseDAO;
    private final FileStorageService fileStorageService;
    private final ActivityLogService activityLogService;

    public StudentService(StudentDAO studentDAO,
                          ClassDAO classDAO,
                          CourseDAO courseDAO,
                          FileStorageService fileStorageService,
                          StudentClassHistoryService historyService,
                          ActivityLogService activityLogService) {
        this.studentDAO = studentDAO;
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

        Course course = upsertCourse(cId, tenKhoa, student.getNgayNhapHoc());
        ClassEntity lop = upsertClass(lopId, khoi, course, student.getNgayNhapHoc());

        student.setLop(lop);
        saveAvatarIfPresent(student, avatar, student.getIdHocSinh());
        studentDAO.save(student);
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

        StudentSnapshot beforeSnapshot = snapshot(student);

        String oldStudentId = student.getIdHocSinh();
        String newStudentId = resolveUpdatedStudentId(oldStudentId, formStudent.getIdHocSinh());

        applyEditableStudentFields(student, formStudent);
        validateKhoi(khoi);

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
        addChange(changes, "Giới tính", before.gioiTinh, after.gioiTinh);
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
        addChange(changes, "Trạng thái", before.trangThai, after.trangThai);
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

    private void addChange(List<String> changes, String label, Object before, Object after) {
        if (Objects.equals(before, after)) {
            return;
        }

        changes.add(label + ": " + formatValue(before) + " -> " + formatValue(after));
    }

    private String formatValue(Object value) {
        return value == null ? "(trống)" : value.toString();
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
            currentClass.setTenLop(currentClass.getIdLop());
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

        target.setGioiTinh(norm(source.getGioiTinh()));
        target.setNoiSinh(norm(source.getNoiSinh()));
        target.setDanToc(norm(source.getDanToc()));
        target.setSoDienThoai(norm(source.getSoDienThoai()));
        target.setEmail(norm(source.getEmail()));
        target.setDiaChi(norm(source.getDiaChi()));
        target.setHoTenCha(norm(source.getHoTenCha()));
        target.setSdtCha(norm(source.getSdtCha()));
        target.setHoTenMe(norm(source.getHoTenMe()));
        target.setSdtMe(norm(source.getSdtMe()));

        String trangThai = norm(source.getTrangThai());
        target.setTrangThai(trangThai == null ? "dang_hoc" : trangThai);
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
        ClassEntity lop = classDAO.findById(idLop).orElse(null);

        if (lop == null) {
            ClassEntity newClass = new ClassEntity();
            newClass.setIdLop(idLop);
            newClass.setTenLop(idLop);
            newClass.setKhoi(khoi);
            newClass.setNamHoc(buildNamHoc(ngayNhapHoc));
            newClass.setKhoaHoc(course);
            newClass.setSiSo(0);

            return classDAO.save(newClass);
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
        private String idLop;
        private Integer khoi;
        private String idKhoa;
        private String tenKhoa;
    }
}
