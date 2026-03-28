package com.quanly.webdiem.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class ConductRecordId implements Serializable {

    private String idHocSinh;
    private String namHoc;
    private Integer hocKy;

    public ConductRecordId() {
    }

    public ConductRecordId(String idHocSinh, String namHoc, Integer hocKy) {
        this.idHocSinh = idHocSinh;
        this.namHoc = namHoc;
        this.hocKy = hocKy;
    }

    public String getIdHocSinh() {
        return idHocSinh;
    }

    public void setIdHocSinh(String idHocSinh) {
        this.idHocSinh = idHocSinh;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public Integer getHocKy() {
        return hocKy;
    }

    public void setHocKy(Integer hocKy) {
        this.hocKy = hocKy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConductRecordId that)) {
            return false;
        }
        return Objects.equals(idHocSinh, that.idHocSinh)
                && Objects.equals(namHoc, that.namHoc)
                && Objects.equals(hocKy, that.hocKy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idHocSinh, namHoc, hocKy);
    }
}
