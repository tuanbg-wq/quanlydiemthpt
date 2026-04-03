package com.quanly.webdiem.model.search;

public class TeacherScoreSearch {

    private String q;
    private String mon;
    private String hocKy;
    private String classScope;
    private Integer page;

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getMon() {
        return mon;
    }

    public void setMon(String mon) {
        this.mon = mon;
    }

    public String getHocKy() {
        return hocKy;
    }

    public void setHocKy(String hocKy) {
        this.hocKy = hocKy;
    }

    public String getClassScope() {
        return classScope;
    }

    public void setClassScope(String classScope) {
        this.classScope = classScope;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
