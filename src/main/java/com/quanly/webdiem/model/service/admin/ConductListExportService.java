package com.quanly.webdiem.model.service.admin;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.quanly.webdiem.model.search.ConductSearch;
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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ConductListExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public byte[] exportExcel(List<ConductManagementService.ConductRow> rows, ConductSearch search) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Khen thưởng kỷ luật");

            CellStyle titleStyle = createExcelTitleStyle(workbook);
            CellStyle labelStyle = createExcelLabelStyle(workbook);
            CellStyle bodyStyle = createExcelBodyStyle(workbook);
            CellStyle headerStyle = createExcelHeaderStyle(workbook);

            int rowIndex = 0;
            rowIndex = writeCellPair(
                    sheet,
                    rowIndex,
                    "BÁO CÁO KHEN THƯỞNG / KỶ LUẬT",
                    "Thời gian xuất: " + DATE_TIME_FORMATTER.format(LocalDateTime.now()),
                    titleStyle
            );
            rowIndex = writeCellPair(sheet, rowIndex, "Tổng số học sinh", String.valueOf(rows == null ? 0 : rows.size()), labelStyle);
            rowIndex++;

            rowIndex = writeCellPair(sheet, rowIndex, "Từ khóa", safeText(search == null ? null : search.getQ()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Khối", safeText(search == null ? null : search.getKhoi()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Lớp", safeText(search == null ? null : search.getLop()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Khóa học", safeText(search == null ? null : search.getKhoa()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Loại", resolveTypeLabel(search == null ? null : search.getLoai()), bodyStyle);
            rowIndex++;

            int headerRowIndex = rowIndex++;
            Row headerRow = sheet.createRow(headerRowIndex);
            headerRow.setHeightInPoints(24f);
            int col = 0;
            writeCell(headerRow, col++, "STT", headerStyle);
            writeCell(headerRow, col++, "Mã học sinh", headerStyle);
            writeCell(headerRow, col++, "Họ tên", headerStyle);
            writeCell(headerRow, col++, "Lớp", headerStyle);
            writeCell(headerRow, col++, "Loại", headerStyle);
            writeCell(headerRow, col++, "Số quyết định", headerStyle);
            writeCell(headerRow, col++, "Loại chi tiết", headerStyle);
            writeCell(headerRow, col++, "Nội dung chi tiết", headerStyle);
            writeCell(headerRow, col++, "Ngày ban hành", headerStyle);
            writeCell(headerRow, col++, "Năm học", headerStyle);
            writeCell(headerRow, col, "Học kỳ", headerStyle);

            int stt = 1;
            if (rows != null) {
                for (ConductManagementService.ConductRow item : rows) {
                    Row row = sheet.createRow(rowIndex++);
                    row.setHeightInPoints(36f);
                    int rowCol = 0;
                    writeCell(row, rowCol++, String.valueOf(stt++), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getIdHocSinh()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getTenHocSinh()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getTenLop()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getLoaiDisplay()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getSoQuyetDinh()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getLoaiChiTiet()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getNoiDungChiTiet()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getNgayBanHanh()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getNamHoc()), bodyStyle);
                    writeCell(row, rowCol, safeText(item == null ? null : item.getHocKyDisplay()), bodyStyle);
                }
            }

            sheet.setColumnWidth(0, 2400);
            sheet.setColumnWidth(1, 4200);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 3600);
            sheet.setColumnWidth(4, 3400);
            sheet.setColumnWidth(5, 5200);
            sheet.setColumnWidth(6, 5000);
            sheet.setColumnWidth(7, 13000);
            sheet.setColumnWidth(8, 4200);
            sheet.setColumnWidth(9, 3800);
            sheet.setColumnWidth(10, 3600);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể xuất file Excel khen thưởng/kỷ luật.");
        }
    }

    public byte[] exportPdf(List<ConductManagementService.ConductRow> rows, ConductSearch search) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 18, 18);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = createPdfFont(14, true);
            Font labelFont = createPdfFont(9, true);
            Font bodyFont = createPdfFont(9, false);
            Font metaFont = createPdfFont(8.5f, false);

            Paragraph title = new Paragraph("BÁO CÁO KHEN THƯỞNG / KỶ LUẬT", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4f);
            document.add(title);

            Paragraph generated = new Paragraph(
                    "Thời gian xuất: " + DATE_TIME_FORMATTER.format(LocalDateTime.now()),
                    metaFont
            );
            generated.setSpacingAfter(6f);
            document.add(generated);

            Paragraph filters = new Paragraph(
                    "Bộ lọc: Từ khóa = " + valueOrDash(search == null ? null : search.getQ())
                            + " | Khối = " + valueOrDash(search == null ? null : search.getKhoi())
                            + " | Lớp = " + valueOrDash(search == null ? null : search.getLop())
                            + " | Khóa học = " + valueOrDash(search == null ? null : search.getKhoa())
                            + " | Loại = " + valueOrDash(resolveTypeLabel(search == null ? null : search.getLoai())),
                    metaFont
            );
            filters.setSpacingAfter(10f);
            document.add(filters);

            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 1.4f, 2.6f, 1.2f, 1.2f, 1.6f, 1.5f, 3.4f, 1.4f, 1.2f, 1.2f});

            addHeaderCell(table, "STT", labelFont);
            addHeaderCell(table, "Mã học sinh", labelFont);
            addHeaderCell(table, "Họ tên", labelFont);
            addHeaderCell(table, "Lớp", labelFont);
            addHeaderCell(table, "Loại", labelFont);
            addHeaderCell(table, "Số quyết định", labelFont);
            addHeaderCell(table, "Loại chi tiết", labelFont);
            addHeaderCell(table, "Nội dung chi tiết", labelFont);
            addHeaderCell(table, "Ngày ban hành", labelFont);
            addHeaderCell(table, "Năm học", labelFont);
            addHeaderCell(table, "Học kỳ", labelFont);

            int stt = 1;
            if (rows != null) {
                for (ConductManagementService.ConductRow item : rows) {
                    addBodyCell(table, String.valueOf(stt++), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getIdHocSinh()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getTenHocSinh()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getTenLop()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getLoaiDisplay()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getSoQuyetDinh()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getLoaiChiTiet()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getNoiDungChiTiet()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getNgayBanHanh()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getNamHoc()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getHocKyDisplay()), bodyFont);
                }
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (IOException | DocumentException ex) {
            throw new RuntimeException("Không thể xuất file PDF khen thưởng/kỷ luật.");
        }
    }

    private int writeCellPair(Sheet sheet, int rowIndex, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        writeCell(row, 0, valueOrDash(label), style);
        writeCell(row, 1, valueOrDash(value), style);
        return rowIndex + 1;
    }

    private void writeCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(valueOrDash(value));
        cell.setCellStyle(style);
    }

    private CellStyle createExcelTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createExcelLabelStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createExcelHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createExcelBodyStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
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

    private String resolveTypeLabel(String type) {
        String normalized = safeText(type);
        if ("KHEN_THUONG".equalsIgnoreCase(normalized)) {
            return "Khen thưởng";
        }
        if ("KY_LUAT".equalsIgnoreCase(normalized)) {
            return "Kỷ luật";
        }
        return normalized;
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String valueOrDash(String value) {
        if (value == null) {
            return "-";
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return "-";
        }
        return normalized;
    }
}
