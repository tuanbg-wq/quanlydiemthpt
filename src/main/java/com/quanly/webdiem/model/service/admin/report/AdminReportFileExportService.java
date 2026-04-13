package com.quanly.webdiem.model.service.admin.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminReportFileExportService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    public ExportFile createFile(AdminReportType reportType,
                                 AdminReportPreview preview,
                                 String format) {
        String resolvedFormat = normalizeFormat(format);
        AdminReportPreview normalizedPreview = normalizePreview(preview);
        if ("XLSX".equals(resolvedFormat)) {
            byte[] content = createExcel(reportType, normalizedPreview);
            return new ExportFile(content, XLSX_MEDIA_TYPE, "xlsx");
        }
        byte[] content = createPdf(reportType, normalizedPreview);
        return new ExportFile(content, MediaType.APPLICATION_PDF, "pdf");
    }

    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            return "PDF";
        }
        String normalized = format.trim().toUpperCase();
        if (!"PDF".equals(normalized) && !"XLSX".equals(normalized)) {
            return "PDF";
        }
        return normalized;
    }

    private AdminReportPreview normalizePreview(AdminReportPreview preview) {
        if (preview == null) {
            return new AdminReportPreview(List.of(), List.of("Dữ liệu"), List.of(), "Không có dữ liệu.", 0);
        }

        List<String> headers = preview.getHeaders() == null || preview.getHeaders().isEmpty()
                ? List.of("Dữ liệu")
                : preview.getHeaders();
        int columnSize = headers.size();

        List<List<String>> rows = new ArrayList<>();
        if (preview.getRows() != null) {
            for (List<String> row : preview.getRows()) {
                if (row == null) {
                    continue;
                }
                List<String> normalized = new ArrayList<>();
                for (int i = 0; i < columnSize; i++) {
                    String value = i < row.size() ? row.get(i) : "-";
                    if (value == null || value.isBlank()) {
                        normalized.add("-");
                    } else {
                        normalized.add(value.trim());
                    }
                }
                rows.add(normalized);
            }
        }

        long total = preview.getTotalRows() > 0 ? preview.getTotalRows() : rows.size();
        return new AdminReportPreview(
                preview.getMetrics(),
                headers,
                rows,
                preview.getEmptyMessage(),
                total
        );
    }

    private byte[] createExcel(AdminReportType reportType, AdminReportPreview preview) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyle = createBodyStyle(workbook);

            int rowIndex = 0;
            rowIndex = writeCellPair(sheet, rowIndex, "Báo cáo", reportType == null ? "Báo cáo thống kê" : reportType.getTitle(), titleStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Thời gian", TIME_FORMAT.format(LocalDateTime.now()), metaStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Tổng bản ghi", String.valueOf(preview.getRows().size()), metaStyle);
            rowIndex++;

            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < preview.getHeaders().size(); i++) {
                writeCell(headerRow, i, preview.getHeaders().get(i), headerStyle);
            }

            for (List<String> dataRow : preview.getRows()) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < preview.getHeaders().size(); i++) {
                    String value = i < dataRow.size() ? dataRow.get(i) : "-";
                    writeCell(row, i, value, bodyStyle);
                }
            }

            if (preview.getRows().isEmpty()) {
                Row emptyRow = sheet.createRow(rowIndex);
                writeCell(emptyRow, 0, preview.getEmptyMessage() == null ? "Không có dữ liệu." : preview.getEmptyMessage(), bodyStyle);
            }

            int lastColumn = Math.max(0, preview.getHeaders().size() - 1);
            for (int i = 0; i <= lastColumn; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể tạo file Excel báo cáo.");
        }
    }

    private byte[] createPdf(AdminReportType reportType, AdminReportPreview preview) {
        Rectangle pageSize = preview.getHeaders().size() > 7 ? PageSize.A4.rotate() : PageSize.A4;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(pageSize, 18, 18, 18, 18);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = createPdfFont(14, true);
            Font metaFont = createPdfFont(9, false);
            Font headerFont = createPdfFont(9, true);
            Font bodyFont = createPdfFont(9, false);

            Paragraph title = new Paragraph(
                    "Báo cáo: " + (reportType == null ? "Thống kê" : reportType.getTitle()),
                    titleFont
            );
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4f);
            document.add(title);

            Paragraph generatedAt = new Paragraph("Thời gian: " + TIME_FORMAT.format(LocalDateTime.now()), metaFont);
            generatedAt.setSpacingAfter(2f);
            document.add(generatedAt);

            Paragraph totalRows = new Paragraph("Tổng bản ghi: " + preview.getRows().size(), metaFont);
            totalRows.setSpacingAfter(10f);
            document.add(totalRows);

            PdfPTable table = new PdfPTable(Math.max(1, preview.getHeaders().size()));
            table.setWidthPercentage(100f);
            for (String header : preview.getHeaders()) {
                addHeaderCell(table, header, headerFont);
            }

            if (preview.getRows().isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase(
                        preview.getEmptyMessage() == null ? "Không có dữ liệu." : preview.getEmptyMessage(),
                        bodyFont
                ));
                emptyCell.setColspan(Math.max(1, preview.getHeaders().size()));
                emptyCell.setPadding(8f);
                table.addCell(emptyCell);
            } else {
                for (List<String> row : preview.getRows()) {
                    for (int i = 0; i < preview.getHeaders().size(); i++) {
                        String value = i < row.size() ? row.get(i) : "-";
                        addBodyCell(table, value, bodyFont);
                    }
                }
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (IOException | DocumentException ex) {
            throw new RuntimeException("Không thể tạo file PDF báo cáo.");
        }
    }

    private int writeCellPair(Sheet sheet, int rowIndex, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        writeCell(row, 0, label, style);
        writeCell(row, 1, value, style);
        return rowIndex + 1;
    }

    private void writeCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null || value.isBlank() ? "-" : value.trim());
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createMetaStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createBodyStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valueOrDash(text), font));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new java.awt.Color(236, 241, 247));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valueOrDash(text), font));
        cell.setPadding(6f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private Font createPdfFont(float size, boolean bold) throws IOException, DocumentException {
        BaseFont baseFont = resolveUnicodeBaseFont();
        Font font = new Font(baseFont, size);
        font.setStyle(bold ? Font.BOLD : Font.NORMAL);
        return font;
    }

    private BaseFont resolveUnicodeBaseFont() throws IOException, DocumentException {
        List<String> candidates = List.of(
                "C:/Windows/Fonts/arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
        );
        for (String candidate : candidates) {
            if (Files.exists(Path.of(candidate))) {
                return BaseFont.createFont(candidate, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }

    private String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.trim();
    }

    public static class ExportFile {
        private final byte[] content;
        private final MediaType mediaType;
        private final String extension;

        public ExportFile(byte[] content, MediaType mediaType, String extension) {
            this.content = content;
            this.mediaType = mediaType;
            this.extension = extension;
        }

        public byte[] getContent() {
            return content;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public String getExtension() {
            return extension;
        }
    }
}
