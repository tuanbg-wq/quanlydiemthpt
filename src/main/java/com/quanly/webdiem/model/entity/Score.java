package com.quanly.webdiem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @Column(name = "id_diem")
    private Integer idDiem;

    @Column(name = "id_hoc_sinh", length = 10, nullable = false)
    private String idHocSinh;

    @Column(name = "id_mon_hoc", length = 10, nullable = false)
    private String idMonHoc;

    @Column(name = "id_loai_diem", nullable = false)
    private Integer idLoaiDiem;

    @Column(name = "nam_hoc", length = 20, nullable = false)
    private String namHoc;

    @Column(name = "hoc_ky", nullable = false)
    private Integer hocKy;

    @Column(name = "diem", precision = 4, scale = 2, nullable = false)
    private BigDecimal diem;

    @Column(name = "id_giao_vien", length = 10)
    private String idGiaoVien;

    @Column(name = "ngay_nhap")
    private LocalDate ngayNhap;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;

    public Integer getIdDiem() {
        return idDiem;
    }

    public void setIdDiem(Integer idDiem) {
        this.idDiem = idDiem;
    }

    public String getIdHocSinh() {
        return idHocSinh;
    }

    public void setIdHocSinh(String idHocSinh) {
        this.idHocSinh = idHocSinh;
    }

    public String getIdMonHoc() {
        return idMonHoc;
    }

    public void setIdMonHoc(String idMonHoc) {
        this.idMonHoc = idMonHoc;
    }

    public Integer getIdLoaiDiem() {
        return idLoaiDiem;
    }

    public void setIdLoaiDiem(Integer idLoaiDiem) {
        this.idLoaiDiem = idLoaiDiem;
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

    public BigDecimal getDiem() {
        return diem;
    }

    public void setDiem(BigDecimal diem) {
        this.diem = diem;
    }

    public String getIdGiaoVien() {
        return idGiaoVien;
    }

    public void setIdGiaoVien(String idGiaoVien) {
        this.idGiaoVien = idGiaoVien;
    }

    public LocalDate getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(LocalDate ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}
