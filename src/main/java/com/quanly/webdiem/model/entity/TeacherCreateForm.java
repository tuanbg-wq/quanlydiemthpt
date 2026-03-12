package com.quanly.webdiem.model.entity;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeacherCreateForm {

    private String idGiaoVien;
    private String hoTen;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;

    private String gioiTinh;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String monHocId;
    private String trinhDo;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayBatDauCongTac;

    private MultipartFile avatar;
    private String ghiChu;
    private String trangThai;
    private String namHoc;
    private List<String> vaiTroMa = new ArrayList<>();

    public String getIdGiaoVien() {
        return idGiaoVien;
    }

    public void setIdGiaoVien(String idGiaoVien) {
        this.idGiaoVien = idGiaoVien;
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

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getMonHocId() {
        return monHocId;
    }

    public void setMonHocId(String monHocId) {
        this.monHocId = monHocId;
    }

    public String getTrinhDo() {
        return trinhDo;
    }

    public void setTrinhDo(String trinhDo) {
        this.trinhDo = trinhDo;
    }

    public LocalDate getNgayBatDauCongTac() {
        return ngayBatDauCongTac;
    }

    public void setNgayBatDauCongTac(LocalDate ngayBatDauCongTac) {
        this.ngayBatDauCongTac = ngayBatDauCongTac;
    }

    public MultipartFile getAvatar() {
        return avatar;
    }

    public void setAvatar(MultipartFile avatar) {
        this.avatar = avatar;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public List<String> getVaiTroMa() {
        return vaiTroMa;
    }

    public void setVaiTroMa(List<String> vaiTroMa) {
        this.vaiTroMa = vaiTroMa == null ? new ArrayList<>() : vaiTroMa;
    }
}
