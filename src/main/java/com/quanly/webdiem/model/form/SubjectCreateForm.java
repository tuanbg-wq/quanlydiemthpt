package com.quanly.webdiem.model.form;

public class SubjectCreateForm {

    private String idMonHoc;
    private String tenMonHoc;
    private String courseId;
    private String namHoc;
    private String hocKy;
    private String khoiApDung;
    private String toBoMon;
    private Integer soDiemThuongXuyen = 3;
    private String giaoVienPhuTrach;
    private String moTa;

    public String getIdMonHoc() {
        return idMonHoc;
    }

    public void setIdMonHoc(String idMonHoc) {
        this.idMonHoc = idMonHoc;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

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

    public String getKhoiApDung() {
        return khoiApDung;
    }

    public void setKhoiApDung(String khoiApDung) {
        this.khoiApDung = khoiApDung;
    }

    public String getToBoMon() {
        return toBoMon;
    }

    public void setToBoMon(String toBoMon) {
        this.toBoMon = toBoMon;
    }

    public Integer getSoDiemThuongXuyen() {
        return soDiemThuongXuyen;
    }

    public void setSoDiemThuongXuyen(Integer soDiemThuongXuyen) {
        this.soDiemThuongXuyen = soDiemThuongXuyen;
    }

    public String getGiaoVienPhuTrach() {
        return giaoVienPhuTrach;
    }

    public void setGiaoVienPhuTrach(String giaoVienPhuTrach) {
        this.giaoVienPhuTrach = giaoVienPhuTrach;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
