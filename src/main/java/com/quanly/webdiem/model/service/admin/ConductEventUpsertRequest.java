package com.quanly.webdiem.model.service.admin;

public class ConductEventUpsertRequest {

    private Long eventId;
    private String studentId;
    private String loai;
    private String loaiChiTiet;
    private String soQuyetDinh;
    private String ngayBanHanh;
    private String noiDung;
    private String ghiChu;
    private String namHoc;
    private Integer hocKy;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getLoaiChiTiet() {
        return loaiChiTiet;
    }

    public void setLoaiChiTiet(String loaiChiTiet) {
        this.loaiChiTiet = loaiChiTiet;
    }

    public String getSoQuyetDinh() {
        return soQuyetDinh;
    }

    public void setSoQuyetDinh(String soQuyetDinh) {
        this.soQuyetDinh = soQuyetDinh;
    }

    public String getNgayBanHanh() {
        return ngayBanHanh;
    }

    public void setNgayBanHanh(String ngayBanHanh) {
        this.ngayBanHanh = ngayBanHanh;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public Integer getHocKy() {
        return hocKy;
    }

    public void setHocKy(Integer hocKy) {
        this.hocKy = hocKy;
    }
}
