package com.quanly.webdiem.model.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AccountUpsertForm {

    @NotBlank(message = "Ten dang nhap la bat buoc.")
    @Size(min = 4, max = 50, message = "Ten dang nhap phai tu 4 den 50 ky tu.")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Ten dang nhap chi duoc chua chu, so, dau gach duoi, gach ngang va dau cham.")
    private String tenDangNhap;

    @Size(max = 72, message = "Mat khau khong vuot qua 72 ky tu.")
    @Pattern(regexp = "^$|^(?=.*\\d)(?=.*@).{5,72}$", message = "Mat khau phai tu 5 ky tu, co it nhat 1 so va ky tu @.")
    private String matKhau;

    @Size(max = 72, message = "Mat khau hien tai khong vuot qua 72 ky tu.")
    private String matKhauHienTai;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Email khong dung dinh dang.")
    @Size(max = 100, message = "Email khong vuot qua 100 ky tu.")
    private String email;

    @NotBlank(message = "Vai tro la bat buoc.")
    @Pattern(regexp = "^(ADMIN|GVCN|GVBM)$", message = "Vai tro khong hop le.")
    private String vaiTroMa;

    @NotBlank(message = "Trang thai tai khoan la bat buoc.")
    @Pattern(regexp = "^(hoat_dong|khoa)$", message = "Trang thai tai khoan khong hop le.")
    private String trangThai = "hoat_dong";

    @Size(max = 10, message = "Ma giao vien khong hop le.")
    private String idGiaoVien;

    private String hoTenGiaoVien;
    private String gioiTinhGiaoVien;
    private String ngaySinhGiaoVien;
    private String monDayGiaoVien;
    private String soDienThoaiGiaoVien;

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getMatKhauHienTai() {
        return matKhauHienTai;
    }

    public void setMatKhauHienTai(String matKhauHienTai) {
        this.matKhauHienTai = matKhauHienTai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVaiTroMa() {
        return vaiTroMa;
    }

    public void setVaiTroMa(String vaiTroMa) {
        this.vaiTroMa = vaiTroMa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getIdGiaoVien() {
        return idGiaoVien;
    }

    public void setIdGiaoVien(String idGiaoVien) {
        this.idGiaoVien = idGiaoVien;
    }

    public String getHoTenGiaoVien() {
        return hoTenGiaoVien;
    }

    public void setHoTenGiaoVien(String hoTenGiaoVien) {
        this.hoTenGiaoVien = hoTenGiaoVien;
    }

    public String getGioiTinhGiaoVien() {
        return gioiTinhGiaoVien;
    }

    public void setGioiTinhGiaoVien(String gioiTinhGiaoVien) {
        this.gioiTinhGiaoVien = gioiTinhGiaoVien;
    }

    public String getNgaySinhGiaoVien() {
        return ngaySinhGiaoVien;
    }

    public void setNgaySinhGiaoVien(String ngaySinhGiaoVien) {
        this.ngaySinhGiaoVien = ngaySinhGiaoVien;
    }

    public String getMonDayGiaoVien() {
        return monDayGiaoVien;
    }

    public void setMonDayGiaoVien(String monDayGiaoVien) {
        this.monDayGiaoVien = monDayGiaoVien;
    }

    public String getSoDienThoaiGiaoVien() {
        return soDienThoaiGiaoVien;
    }

    public void setSoDienThoaiGiaoVien(String soDienThoaiGiaoVien) {
        this.soDienThoaiGiaoVien = soDienThoaiGiaoVien;
    }
}
