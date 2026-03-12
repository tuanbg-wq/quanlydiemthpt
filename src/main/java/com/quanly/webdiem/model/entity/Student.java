package com.quanly.webdiem.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "id_hoc_sinh", length = 10)
    private String idHocSinh;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "ngay_sinh", nullable = false)
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(name = "noi_sinh", length = 100)
    private String noiSinh;

    @Column(name = "dan_toc", length = 50)
    private String danToc;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "anh", length = 255)
    private String anh;

    @Column(name = "dia_chi", columnDefinition = "TEXT")
    private String diaChi;

    @Column(name = "ho_ten_cha", length = 100)
    private String hoTenCha;

    @Column(name = "sdt_cha", length = 15)
    private String sdtCha;

    @Column(name = "ho_ten_me", length = 100)
    private String hoTenMe;

    @Column(name = "sdt_me", length = 15)
    private String sdtMe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lop")
    private ClassEntity lop;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "ngay_nhap_hoc")
    private LocalDate ngayNhapHoc;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Transient
    private String previousClass;

    @Transient
    private String historyTypeDisplay;

    @Transient
    private String historyDetail;

    public String getIdHocSinh() {
        return idHocSinh;
    }

    public void setIdHocSinh(String idHocSinh) {
        this.idHocSinh = idHocSinh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getNoiSinh() {
        return noiSinh;
    }

    public void setNoiSinh(String noiSinh) {
        this.noiSinh = noiSinh;
    }

    public String getDanToc() {
        return danToc;
    }

    public void setDanToc(String danToc) {
        this.danToc = danToc;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAnh() {
        return anh;
    }

    public void setAnh(String anh) {
        this.anh = anh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getHoTenCha() {
        return hoTenCha;
    }

    public void setHoTenCha(String hoTenCha) {
        this.hoTenCha = hoTenCha;
    }

    public String getSdtCha() {
        return sdtCha;
    }

    public void setSdtCha(String sdtCha) {
        this.sdtCha = sdtCha;
    }

    public String getHoTenMe() {
        return hoTenMe;
    }

    public void setHoTenMe(String hoTenMe) {
        this.hoTenMe = hoTenMe;
    }

    public String getSdtMe() {
        return sdtMe;
    }

    public void setSdtMe(String sdtMe) {
        this.sdtMe = sdtMe;
    }

    public ClassEntity getLop() {
        return lop;
    }

    public void setLop(ClassEntity lop) {
        this.lop = lop;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayNhapHoc() {
        return ngayNhapHoc;
    }

    public void setNgayNhapHoc(LocalDate ngayNhapHoc) {
        this.ngayNhapHoc = ngayNhapHoc;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getPreviousClass() {
        return previousClass;
    }

    public void setPreviousClass(String previousClass) {
        this.previousClass = previousClass;
    }

    public String getHistoryTypeDisplay() {
        return historyTypeDisplay;
    }

    public void setHistoryTypeDisplay(String historyTypeDisplay) {
        this.historyTypeDisplay = historyTypeDisplay;
    }

    public String getHistoryDetail() {
        return historyDetail;
    }

    public void setHistoryDetail(String historyDetail) {
        this.historyDetail = historyDetail;
    }
}