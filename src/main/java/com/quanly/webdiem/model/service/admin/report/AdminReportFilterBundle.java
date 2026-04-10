package com.quanly.webdiem.model.service.admin.report;

import java.util.Collections;
import java.util.List;

public class AdminReportFilterBundle {

    private final List<AdminReportFilterOption> namHocOptions;
    private final List<AdminReportFilterOption> hocKyOptions;
    private final List<AdminReportFilterOption> khoiOptions;
    private final List<AdminReportFilterOption> lopOptions;
    private final List<AdminReportFilterOption> monOptions;
    private final List<AdminReportFilterOption> khoaOptions;
    private final List<AdminReportFilterOption> loaiOptions;
    private final List<AdminReportFilterOption> boMonOptions;
    private final List<AdminReportFilterOption> trangThaiOptions;
    private final List<AdminReportFilterOption> vaiTroOptions;

    public AdminReportFilterBundle(List<AdminReportFilterOption> namHocOptions,
                                   List<AdminReportFilterOption> hocKyOptions,
                                   List<AdminReportFilterOption> khoiOptions,
                                   List<AdminReportFilterOption> lopOptions,
                                   List<AdminReportFilterOption> monOptions,
                                   List<AdminReportFilterOption> khoaOptions,
                                   List<AdminReportFilterOption> loaiOptions,
                                   List<AdminReportFilterOption> boMonOptions,
                                   List<AdminReportFilterOption> trangThaiOptions,
                                   List<AdminReportFilterOption> vaiTroOptions) {
        this.namHocOptions = safe(namHocOptions);
        this.hocKyOptions = safe(hocKyOptions);
        this.khoiOptions = safe(khoiOptions);
        this.lopOptions = safe(lopOptions);
        this.monOptions = safe(monOptions);
        this.khoaOptions = safe(khoaOptions);
        this.loaiOptions = safe(loaiOptions);
        this.boMonOptions = safe(boMonOptions);
        this.trangThaiOptions = safe(trangThaiOptions);
        this.vaiTroOptions = safe(vaiTroOptions);
    }

    private List<AdminReportFilterOption> safe(List<AdminReportFilterOption> values) {
        return values == null ? Collections.emptyList() : values;
    }

    public List<AdminReportFilterOption> getNamHocOptions() {
        return namHocOptions;
    }

    public List<AdminReportFilterOption> getHocKyOptions() {
        return hocKyOptions;
    }

    public List<AdminReportFilterOption> getKhoiOptions() {
        return khoiOptions;
    }

    public List<AdminReportFilterOption> getLopOptions() {
        return lopOptions;
    }

    public List<AdminReportFilterOption> getMonOptions() {
        return monOptions;
    }

    public List<AdminReportFilterOption> getKhoaOptions() {
        return khoaOptions;
    }

    public List<AdminReportFilterOption> getLoaiOptions() {
        return loaiOptions;
    }

    public List<AdminReportFilterOption> getBoMonOptions() {
        return boMonOptions;
    }

    public List<AdminReportFilterOption> getTrangThaiOptions() {
        return trangThaiOptions;
    }

    public List<AdminReportFilterOption> getVaiTroOptions() {
        return vaiTroOptions;
    }
}
