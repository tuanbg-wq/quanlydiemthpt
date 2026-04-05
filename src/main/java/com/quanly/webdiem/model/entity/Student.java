package com.quanly.webdiem.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;
import java.util.Locale;

@Entity
@Table(name = "students")
public class Student {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

    @Transient
    private String hanhKiemHocKy1;

    @Transient
    private String hanhKiemHocKy2;

    @Transient
    private String hanhKiemCaNam;

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

    public String getHanhKiemHocKy1() {
        return hanhKiemHocKy1;
    }

    public void setHanhKiemHocKy1(String hanhKiemHocKy1) {
        this.hanhKiemHocKy1 = hanhKiemHocKy1;
    }

    public String getHanhKiemHocKy2() {
        return hanhKiemHocKy2;
    }

    public void setHanhKiemHocKy2(String hanhKiemHocKy2) {
        this.hanhKiemHocKy2 = hanhKiemHocKy2;
    }

    public String getHanhKiemCaNam() {
        return hanhKiemCaNam;
    }

    public void setHanhKiemCaNam(String hanhKiemCaNam) {
        this.hanhKiemCaNam = hanhKiemCaNam;
    }

    public String getNgaySinhHienThi() {
        return formatDate(ngaySinh);
    }

    public String getNgayNhapHocHienThi() {
        return formatDate(ngayNhapHoc);
    }

    public String getNgayTaoHienThi() {
        if (ngayTao == null) {
            return "";
        }
        return DATE_TIME_FORMAT.format(ngayTao);
    }

    public String getGioiTinhHienThi() {
        String normalized = normalizeAsciiLower(gioiTinh);
        if (normalized.isBlank()) {
            return "";
        }
        if (normalized.equals("nam")) {
            return "Nam";
        }
        if (normalized.equals("nu")) {
            return "Nữ";
        }
        return gioiTinh == null ? "" : gioiTinh;
    }

    public String getTrangThaiHienThi() {
        String normalized = normalizeAsciiLower(trangThai).replace('-', '_').replace(' ', '_');
        if (normalized.isBlank()) {
            return "";
        }
        return switch (normalized) {
            case "dang_hoc" -> "Đang học";
            case "da_tot_nghiep" -> "Đã tốt nghiệp";
            case "bo_hoc" -> "Bỏ học";
            case "chuyen_truong" -> "Chuyển trường";
            case "bao_luu" -> "Bảo lưu";
            default -> trangThai == null ? "" : trangThai;
        };
    }

    public String getHanhKiemHocKy1HienThi() {
        return formatConduct(hanhKiemHocKy1);
    }

    public String getHanhKiemHocKy2HienThi() {
        return formatConduct(hanhKiemHocKy2);
    }

    public String getHanhKiemCaNamHienThi() {
        return formatConduct(hanhKiemCaNam);
    }

    public String getHanhKiemTongHienThi() {
        String caNam = formatConduct(hanhKiemCaNam);
        if (!caNam.isBlank()) {
            return caNam;
        }
        String hk2 = formatConduct(hanhKiemHocKy2);
        if (!hk2.isBlank()) {
            return hk2;
        }
        return formatConduct(hanhKiemHocKy1);
    }

    public String getKhoaHienThi() {
        if (lop == null || lop.getKhoaHoc() == null) {
            return "";
        }
        String idKhoa = safeTrim(lop.getKhoaHoc().getIdKhoa());
        String tenKhoa = safeTrim(lop.getKhoaHoc().getTenKhoa());

        if (idKhoa == null && tenKhoa == null) {
            return "";
        }
        if (idKhoa == null) {
            return "(" + tenKhoa + ")";
        }
        if (tenKhoa == null) {
            return "(" + idKhoa + ")";
        }
        return "(" + idKhoa + " " + tenKhoa + ")";
    }

    private String formatDate(LocalDate value) {
        if (value == null) {
            return "";
        }
        return DATE_FORMAT.format(value);
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatConduct(String value) {
        String normalized = normalizeAsciiLower(value).replace('-', '_').replace(' ', '_');
        if (normalized.isBlank()) {
            return "";
        }
        return switch (normalized) {
            case "tot", "gioi" -> "Tốt";
            case "kha" -> "Khá";
            case "trung_binh", "tb" -> "Trung bình";
            case "yeu" -> "Yếu";
            case "kem" -> "Kém";
            default -> value == null ? "" : value.trim();
        };
    }

    private String normalizeAsciiLower(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
