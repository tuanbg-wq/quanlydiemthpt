package com.quanly.webdiem.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nhat_ky")
    private Integer idNhatKy;

    @Column(name = "id_tai_khoan", nullable = false)
    private Integer idTaiKhoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", insertable = false, updatable = false)
    private User user;

    @Column(name = "hanh_dong", nullable = false, length = 100)
    private String hanhDong;

    @Column(name = "bang_tac_dong", length = 50)
    private String bangTacDong;

    @Column(name = "id_ban_ghi", length = 50)
    private String idBanGhi;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "dia_chi_ip", length = 45)
    private String diaChiIp;

    @Column(name = "thoi_gian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    public Integer getIdNhatKy() {
        return idNhatKy;
    }

    public void setIdNhatKy(Integer idNhatKy) {
        this.idNhatKy = idNhatKy;
    }

    public Integer getIdTaiKhoan() {
        return idTaiKhoan;
    }

    public void setIdTaiKhoan(Integer idTaiKhoan) {
        this.idTaiKhoan = idTaiKhoan;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(String hanhDong) {
        this.hanhDong = hanhDong;
    }

    public String getBangTacDong() {
        return bangTacDong;
    }

    public void setBangTacDong(String bangTacDong) {
        this.bangTacDong = bangTacDong;
    }

    public String getIdBanGhi() {
        return idBanGhi;
    }

    public void setIdBanGhi(String idBanGhi) {
        this.idBanGhi = idBanGhi;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getDiaChiIp() {
        return diaChiIp;
    }

    public void setDiaChiIp(String diaChiIp) {
        this.diaChiIp = diaChiIp;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getThoiGianHienThi() {
        if (thoiGian == null) {
            return "";
        }
        return DATE_TIME_FORMAT.format(thoiGian);
    }

    public String getHanhDongHienThi() {
        if (hanhDong == null || hanhDong.isBlank()) {
            return "";
        }
        if ("THEM_HOC_SINH".equalsIgnoreCase(hanhDong)) {
            return "Th\u00eam h\u1ecdc sinh";
        }
        if ("CAP_NHAT_HOC_SINH".equalsIgnoreCase(hanhDong)) {
            return "C\u1eadp nh\u1eadt h\u1ecdc sinh";
        }
        if ("XOA_HOC_SINH".equalsIgnoreCase(hanhDong)) {
            return "X\u00f3a h\u1ecdc sinh";
        }
        return hanhDong.replace('_', ' ');
    }

    public String getNguoiThaoTacHienThi() {
        if (user == null) {
            return "N/A";
        }

        String username = user.getTenDangNhap() == null ? "" : user.getTenDangNhap().trim();
        if (username.isEmpty()) {
            username = "N/A";
        }

        String roleName = null;
        if (user.getVaiTro() != null) {
            roleName = normalizeRoleDisplay(user.getVaiTro().getTenVaiTro());
        }

        if (roleName == null || roleName.isBlank()) {
            return username;
        }

        return roleName + " (" + username + ")";
    }

    private String normalizeRoleDisplay(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return null;
        }

        String normalized = roleCode.trim();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        String upper = normalized.toUpperCase();
        return switch (upper) {
            case "ADMIN" -> "Admin";
            case "GVCN" -> "GVCN";
            case "GIAO_VIEN", "GIAOVIEN" -> "Gi\u00e1o vi\u00ean";
            case "HOC_SINH", "HOCSINH" -> "H\u1ecdc sinh";
            default -> normalized.replace('_', ' ');
        };
    }
}