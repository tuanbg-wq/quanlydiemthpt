package com.quanly.webdiem.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "student_class_history")
public class StudentClassHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_hoc_sinh", nullable = false, length = 10)
    private String studentId;

    @Column(name = "lop_cu", length = 50)
    private String lopCu;

    @Column(name = "lop_moi", length = 50)
    private String lopMoi;

    @Column(name = "truong_cu", length = 255)
    private String truongCu;

    @Column(name = "truong_moi", length = 255)
    private String truongMoi;

    @Column(name = "ngay_chuyen")
    private LocalDate ngayChuyen;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "loai_chuyen", nullable = false, length = 20)
    private String loaiChuyen;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLopCu() {
        return lopCu;
    }

    public void setLopCu(String lopCu) {
        this.lopCu = lopCu;
    }

    public String getLopMoi() {
        return lopMoi;
    }

    public void setLopMoi(String lopMoi) {
        this.lopMoi = lopMoi;
    }

    public String getTruongCu() {
        return truongCu;
    }

    public void setTruongCu(String truongCu) {
        this.truongCu = truongCu;
    }

    public String getTruongMoi() {
        return truongMoi;
    }

    public void setTruongMoi(String truongMoi) {
        this.truongMoi = truongMoi;
    }

    public LocalDate getNgayChuyen() {
        return ngayChuyen;
    }

    public void setNgayChuyen(LocalDate ngayChuyen) {
        this.ngayChuyen = ngayChuyen;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getLoaiChuyen() {
        return loaiChuyen;
    }

    public void setLoaiChuyen(String loaiChuyen) {
        this.loaiChuyen = loaiChuyen;
    }
}