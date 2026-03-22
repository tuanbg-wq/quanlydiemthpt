package com.quanly.webdiem.model.dto;

public class TeacherListItem {

    private final String idGiaoVien;
    private final String hoTen;
    private final String ngaySinh;
    private final String gioiTinh;
    private final String soDienThoai;
    private final String email;
    private final String monDay;
    private final String chuNhiemLop;
    private final String lopBoMon;
    private final String vaiTro;
    private final String trangThai;
    private final String khoi;
    private final String avatar;

    public TeacherListItem(String idGiaoVien,
                           String hoTen,
                           String ngaySinh,
                           String gioiTinh,
                           String soDienThoai,
                           String email,
                           String monDay,
                           String chuNhiemLop,
                           String lopBoMon,
                           String vaiTro,
                           String trangThai,
                           String khoi,
                           String avatar) {
        this.idGiaoVien = idGiaoVien;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.monDay = monDay;
        this.chuNhiemLop = chuNhiemLop;
        this.lopBoMon = lopBoMon;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
        this.khoi = khoi;
        this.avatar = avatar;
    }

    public String getIdGiaoVien() {
        return idGiaoVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public String getMonDay() {
        return monDay;
    }

    public String getChuNhiemLop() {
        return chuNhiemLop;
    }

    public String getLopBoMon() {
        return lopBoMon;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getKhoi() {
        return khoi;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarInitials() {
        if (hoTen == null || hoTen.isBlank()) {
            return "GV";
        }

        String[] words = hoTen.trim().split("\\s+");
        if (words.length == 1) {
            String oneWord = words[0];
            return oneWord.length() >= 2
                    ? oneWord.substring(0, 2).toUpperCase()
                    : oneWord.toUpperCase();
        }

        String first = words[words.length - 2];
        String last = words[words.length - 1];
        return (first.substring(0, 1) + last.substring(0, 1)).toUpperCase();
    }
}
