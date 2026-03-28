package com.quanly.webdiem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "conducts")
@IdClass(ConductRecordId.class)
public class ConductRecord {

    @Id
    @Column(name = "id_hoc_sinh", length = 10, nullable = false)
    private String idHocSinh;

    @Id
    @Column(name = "nam_hoc", length = 20, nullable = false)
    private String namHoc;

    @Id
    @Column(name = "hoc_ky", nullable = false)
    private Integer hocKy;

    @Column(name = "xep_loai")
    private String xepLoai;

    @Column(name = "nhan_xet")
    private String nhanXet;

    @Column(name = "id_gvcn")
    private String idGvcn;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    public String getIdHocSinh() {
        return idHocSinh;
    }

    public void setIdHocSinh(String idHocSinh) {
        this.idHocSinh = idHocSinh;
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

    public String getXepLoai() {
        return xepLoai;
    }

    public void setXepLoai(String xepLoai) {
        this.xepLoai = xepLoai;
    }

    public String getNhanXet() {
        return nhanXet;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }

    public String getIdGvcn() {
        return idGvcn;
    }

    public void setIdGvcn(String idGvcn) {
        this.idGvcn = idGvcn;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}
