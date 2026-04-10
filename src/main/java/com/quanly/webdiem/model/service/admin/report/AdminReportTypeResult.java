package com.quanly.webdiem.model.service.admin.report;

public class AdminReportTypeResult {

    private final AdminReportFilterBundle filters;
    private final AdminReportPreview preview;

    public AdminReportTypeResult(AdminReportFilterBundle filters, AdminReportPreview preview) {
        this.filters = filters;
        this.preview = preview;
    }

    public AdminReportFilterBundle getFilters() {
        return filters;
    }

    public AdminReportPreview getPreview() {
        return preview;
    }
}
