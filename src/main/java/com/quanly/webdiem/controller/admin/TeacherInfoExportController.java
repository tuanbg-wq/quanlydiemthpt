package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.TeacherInfoExportService;
import com.quanly.webdiem.model.service.admin.TeacherInfoService;
import com.quanly.webdiem.model.service.admin.TeacherInfoService.TeacherInfoView;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/admin/teacher")
public class TeacherInfoExportController {

    private final TeacherInfoService teacherInfoService;
    private final TeacherInfoExportService teacherInfoExportService;

    public TeacherInfoExportController(TeacherInfoService teacherInfoService,
                                       TeacherInfoExportService teacherInfoExportService) {
        this.teacherInfoService = teacherInfoService;
        this.teacherInfoExportService = teacherInfoExportService;
    }

    @GetMapping("/{id}/info/export/excel")
    public ResponseEntity<byte[]> exportTeacherInfoExcel(@PathVariable("id") String id) {
        TeacherInfoView teacherInfo = loadTeacherInfo(id);
        byte[] content = teacherInfoExportService.exportExcel(teacherInfo);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .headers(downloadHeaders("teacher-info-" + teacherInfo.getIdGiaoVien() + ".xlsx"))
                .body(content);
    }

    @GetMapping("/{id}/info/export/pdf")
    public ResponseEntity<byte[]> exportTeacherInfoPdf(@PathVariable("id") String id) {
        TeacherInfoView teacherInfo = loadTeacherInfo(id);
        byte[] content = teacherInfoExportService.exportPdf(teacherInfo);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .headers(downloadHeaders("teacher-info-" + teacherInfo.getIdGiaoVien() + ".pdf"))
                .body(content);
    }

    private TeacherInfoView loadTeacherInfo(String teacherId) {
        try {
            return teacherInfoService.getTeacherInfo(teacherId);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(NOT_FOUND, ex.getMessage());
        }
    }

    private HttpHeaders downloadHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );
        return headers;
    }
}
