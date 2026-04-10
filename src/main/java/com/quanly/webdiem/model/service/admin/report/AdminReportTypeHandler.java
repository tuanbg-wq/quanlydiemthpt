package com.quanly.webdiem.model.service.admin.report;

import com.quanly.webdiem.model.search.AdminReportSearch;

public interface AdminReportTypeHandler {

    AdminReportType getType();

    AdminReportTypeResult buildResult(AdminReportSearch search);

    String buildFilterSummary(AdminReportSearch search);
}
