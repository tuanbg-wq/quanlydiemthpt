package com.quanly.webdiem.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AccountUpsertForm {

    @NotBlank(message = "Tên đăng nhập là bắt buộc.")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự.")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Tên đăng nhập chỉ được chứa chữ, số, dấu gạch dưới, gạch ngang và dấu chấm.")
    private String tenDangNhap;

    @Size(max = 72, message = "Mật khẩu không vượt quá 72 ký tự.")
    @Pattern(regexp = "^$|^(?=.*\\d)(?=.*@).{5,72}$", message = "Mật khẩu phải từ 5 ký tự, có ít nhất 1 số và ký tự @.")
    private String matKhau;

    @Size(max = 72, message = "Mật khẩu hiện tại không vượt quá 72 ký tự.")
    private String matKhauHienTai;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Email không đúng định dạng.")
    @Size(max = 100, message = "Email không vượt quá 100 ký tự.")
    private String email;

    @NotBlank(message = "Vai trò là bắt buộc.")
    @Pattern(regexp = "^(ADMIN|GVCN|GVBM)$", message = "Vai trò không hợp lệ.")
    private String vaiTroMa;

    @NotBlank(message = "Trạng thái tài khoản là bắt buộc.")
    @Pattern(regexp = "^(hoat_dong|khoa)$", message = "Trạng thái tài khoản không hợp lệ.")
    private String trangThai = "hoat_dong";

    @Size(max = 10, message = "Mã giáo viên không hợp lệ.")
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
