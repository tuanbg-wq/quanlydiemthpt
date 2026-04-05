package com.quanly.webdiem.model.service.teacher;

import com.quanly.webdiem.model.search.ConductSearch;
import com.quanly.webdiem.model.service.admin.ConductListExportService;
import com.quanly.webdiem.model.service.admin.ConductManagementService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherConductExportService {

    private final ConductListExportService conductListExportService;

    public TeacherConductExportService(ConductListExportService conductListExportService) {
        this.conductListExportService = conductListExportService;
    }

    public byte[] exportExcel(List<ConductManagementService.ConductRow> rows, ConductSearch search) {
        return conductListExportService.exportExcel(rows, search);
    }

    public byte[] exportPdf(List<ConductManagementService.ConductRow> rows, ConductSearch search) {
        return conductListExportService.exportPdf(rows, search);
    }
}
