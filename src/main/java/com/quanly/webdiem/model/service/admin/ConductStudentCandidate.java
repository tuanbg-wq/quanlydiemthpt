package com.quanly.webdiem.model.service.admin;

public class ConductStudentCandidate {

    private final String idHocSinh;
    private final String hoTen;
    private final String classId;
    private final String tenLop;
    private final String khoi;
    private final String courseId;
    private final String khoaHoc;

    public ConductStudentCandidate(String idHocSinh, String hoTen, String classId, String tenLop,
                                   String khoi, String courseId, String khoaHoc) {
        this.idHocSinh = idHocSinh;
        this.hoTen = hoTen;
        this.classId = classId;
        this.tenLop = tenLop;
        this.khoi = khoi;
        this.courseId = courseId;
        this.khoaHoc = khoaHoc;
    }

    public String getIdHocSinh() {
        return idHocSinh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getClassId() {
        return classId;
    }

    public String getTenLop() {
        return tenLop;
    }

    public String getKhoi() {
        return khoi;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getKhoaHoc() {
        return khoaHoc;
    }
}
