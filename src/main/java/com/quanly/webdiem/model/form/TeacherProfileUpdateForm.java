package com.quanly.webdiem.model.form;

import org.springframework.web.multipart.MultipartFile;

public class TeacherProfileUpdateForm {

    private String email;
    private String soDienThoai;
    private MultipartFile avatar;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public MultipartFile getAvatar() {
        return avatar;
    }

    public void setAvatar(MultipartFile avatar) {
        this.avatar = avatar;
    }
}
