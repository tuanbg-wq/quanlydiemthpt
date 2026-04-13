package com.quanly.webdiem.model.search;

public class AdminReportSearch {

    private String type;
    private String q;
    private String namHoc;
    private String hocKy;
    private String khoi;
    private String lop;
    private String mon;
    private String khoa;
    private String loai;
    private String boMon;
    private String trangThai;
    private String vaiTro;
    private String hanhKiem;
    private String lichSuChuyen;
    private String applyPreview;
    private String previewPage;
    private String historyType;
    private String historyFormat;
    private String historyTime;
    private String historyRole;
    private String historyDate;
    private String historyMonth;
    private String historyYear;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public String getHocKy() {
        return hocKy;
    }

    public void setHocKy(String hocKy) {
        this.hocKy = hocKy;
    }

    public String getKhoi() {
        return khoi;
    }

    public void setKhoi(String khoi) {
        this.khoi = khoi;
    }

    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }

    public String getMon() {
        return mon;
    }

    public void setMon(String mon) {
        this.mon = mon;
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getBoMon() {
        return boMon;
    }

    public void setBoMon(String boMon) {
        this.boMon = boMon;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getHanhKiem() {
        return hanhKiem;
    }

    public void setHanhKiem(String hanhKiem) {
        this.hanhKiem = hanhKiem;
    }

    public String getLichSuChuyen() {
        return lichSuChuyen;
    }

    public void setLichSuChuyen(String lichSuChuyen) {
        this.lichSuChuyen = lichSuChuyen;
    }

    public String getApplyPreview() {
        return applyPreview;
    }

    public void setApplyPreview(String applyPreview) {
        this.applyPreview = applyPreview;
    }

    public String getPreviewPage() {
        return previewPage;
    }

    public void setPreviewPage(String previewPage) {
        this.previewPage = previewPage;
    }

    public String getHistoryType() {
        return historyType;
    }

    public void setHistoryType(String historyType) {
        this.historyType = historyType;
    }

    public String getHistoryFormat() {
        return historyFormat;
    }

    public void setHistoryFormat(String historyFormat) {
        this.historyFormat = historyFormat;
    }

    public String getHistoryTime() {
        return historyTime;
    }

    public void setHistoryTime(String historyTime) {
        this.historyTime = historyTime;
    }

    public String getHistoryRole() {
        return historyRole;
    }

    public void setHistoryRole(String historyRole) {
        this.historyRole = historyRole;
    }

    public String getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(String historyDate) {
        this.historyDate = historyDate;
    }

    public String getHistoryMonth() {
        return historyMonth;
    }

    public void setHistoryMonth(String historyMonth) {
        this.historyMonth = historyMonth;
    }

    public String getHistoryYear() {
        return historyYear;
    }

    public void setHistoryYear(String historyYear) {
        this.historyYear = historyYear;
    }

    public boolean isPreviewRequested() {
        return "1".equals(applyPreview);
    }

    public int resolvePreviewPageOrDefault() {
        if (previewPage == null || previewPage.isBlank()) {
            return 1;
        }
        try {
            int parsed = Integer.parseInt(previewPage.trim());
            return parsed > 0 ? parsed : 1;
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
