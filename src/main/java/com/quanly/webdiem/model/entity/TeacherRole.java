package com.quanly.webdiem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher_roles")
public class TeacherRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_giao_vien", nullable = false, length = 10)
    private String idGiaoVien;

    @Column(name = "id_loai_vai_tro", nullable = false)
    private Integer idLoaiVaiTro;

    @Column(name = "nam_hoc", nullable = false, length = 20)
    private String namHoc;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdGiaoVien() {
        return idGiaoVien;
    }

    public void setIdGiaoVien(String idGiaoVien) {
        this.idGiaoVien = idGiaoVien;
    }

    public Integer getIdLoaiVaiTro() {
        return idLoaiVaiTro;
    }

    public void setIdLoaiVaiTro(Integer idLoaiVaiTro) {
        this.idLoaiVaiTro = idLoaiVaiTro;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
