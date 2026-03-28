package com.quanly.webdiem.model.service.admin;

public class ConductStudentCandidate {

    private final String idHocSinh;
    private final String hoTen;
    private final String tenLop;
    private final String khoi;
    private final String khoaHoc;

    public ConductStudentCandidate(String idHocSinh, String hoTen, String tenLop, String khoi, String khoaHoc) {
        this.idHocSinh = idHocSinh;
        this.hoTen = hoTen;
        this.tenLop = tenLop;
        this.khoi = khoi;
        this.khoaHoc = khoaHoc;
    }

    public String getIdHocSinh() {
        return idHocSinh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getTenLop() {
        return tenLop;
    }

    public String getKhoi() {
        return khoi;
    }

    public String getKhoaHoc() {
        return khoaHoc;
    }
}
