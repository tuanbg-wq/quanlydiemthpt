package com.quanly.webdiem.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "classes")
public class ClassEntity {

    @Id
    @Column(name = "id_lop", length = 10)
    private String idLop;

    @Column(name = "ten_lop", length = 50, nullable = false)
    private String tenLop;

    @Column(name = "khoi", nullable = false)
    private Integer khoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khoa", nullable = false)
    private Course khoaHoc;

    @Column(name = "nam_hoc", length = 20, nullable = false)
    private String namHoc;

    @Column(name = "si_so")
    private Integer siSo;

    @Column(name = "id_gvcn", length = 10)
    private String idGvcn;

    public String getIdLop() { return idLop; }
    public void setIdLop(String idLop) { this.idLop = idLop; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public Integer getKhoi() { return khoi; }
    public void setKhoi(Integer khoi) { this.khoi = khoi; }

    public Course getKhoaHoc() { return khoaHoc; }
    public void setKhoaHoc(Course khoaHoc) { this.khoaHoc = khoaHoc; }

    public String getNamHoc() { return namHoc; }
    public void setNamHoc(String namHoc) { this.namHoc = namHoc; }

    public Integer getSiSo() { return siSo; }
    public void setSiSo(Integer siSo) { this.siSo = siSo; }

    public String getIdGvcn() { return idGvcn; }
    public void setIdGvcn(String idGvcn) { this.idGvcn = idGvcn; }
}
