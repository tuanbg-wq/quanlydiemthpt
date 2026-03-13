package com.quanly.webdiem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @Column(name = "id_mon_hoc", length = 10)
    private String idMonHoc;

    @Column(name = "ten_mon_hoc", nullable = false, length = 100)
    private String tenMonHoc;

    @Column(name = "id_khoa", length = 10)
    private String idKhoa;

    @Column(name = "nam_hoc_ap_dung", length = 20)
    private String namHocApDung;

    @Column(name = "hoc_ky_ap_dung", length = 20)
    private String hocKyApDung;

    @Column(name = "khoi_ap_dung", length = 20)
    private String khoiApDung;

    @Column(name = "to_bo_mon", length = 100)
    private String toBoMon;

    @Column(name = "id_giao_vien_phu_trach", length = 10)
    private String idGiaoVienPhuTrach;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

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

    public String getIdKhoa() {
        return idKhoa;
    }

    public void setIdKhoa(String idKhoa) {
        this.idKhoa = idKhoa;
    }

    public String getNamHocApDung() {
        return namHocApDung;
    }

    public void setNamHocApDung(String namHocApDung) {
        this.namHocApDung = namHocApDung;
    }

    public String getHocKyApDung() {
        return hocKyApDung;
    }

    public void setHocKyApDung(String hocKyApDung) {
        this.hocKyApDung = hocKyApDung;
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

    public String getIdGiaoVienPhuTrach() {
        return idGiaoVienPhuTrach;
    }

    public void setIdGiaoVienPhuTrach(String idGiaoVienPhuTrach) {
        this.idGiaoVienPhuTrach = idGiaoVienPhuTrach;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
