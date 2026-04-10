package com.quanly.webdiem.model.service.admin.report;

public class AdminReportFilterOption {

    private final String value;
    private final String label;

    public AdminReportFilterOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
