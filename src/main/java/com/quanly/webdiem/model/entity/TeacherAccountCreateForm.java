package com.quanly.webdiem.model.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TeacherAccountCreateForm {

    @NotBlank(message = "Ten dang nhap la bat buoc.")
    @Size(min = 4, max = 50, message = "Ten dang nhap phai tu 4 den 50 ky tu.")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Ten dang nhap chi duoc chua chu, so, dau gach duoi, gach ngang va dau cham.")
    private String tenDangNhap;

    @NotBlank(message = "Mat khau la bat buoc.")
    @Size(min = 6, max = 72, message = "Mat khau phai tu 6 den 72 ky tu.")
    @Pattern(regexp = "^(?=.*\\d)(?=.*@).+$", message = "Mat khau phai co it nhat 1 chu so va ky tu @.")
    private String matKhau;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Email khong dung dinh dang.")
    @Size(max = 100, message = "Email khong vuot qua 100 ky tu.")
    private String email;

    @NotBlank(message = "Trang thai tai khoan la bat buoc.")
    @Pattern(regexp = "^(hoat_dong|khoa)$", message = "Trang thai tai khoan khong hop le.")
    private String trangThai = "hoat_dong";

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
