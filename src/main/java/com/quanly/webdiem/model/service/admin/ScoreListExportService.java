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
import com.quanly.webdiem.model.search.ScoreSearch;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ScoreListExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exportExcel(List<ScoreManagementService.ScoreRow> rows, ScoreSearch search) {
        boolean annualView = isAnnualView(search);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sach diem");

            CellStyle titleStyle = createExcelTitleStyle(workbook);
            CellStyle labelStyle = createExcelLabelStyle(workbook);
            CellStyle bodyStyle = createExcelBodyStyle(workbook);
            CellStyle headerStyle = createExcelHeaderStyle(workbook);

            int rowIndex = 0;
            rowIndex = writeCellPair(sheet, rowIndex, "BAO CAO DIEM SO", "Ngay xuat: " + DATE_FORMATTER.format(LocalDate.now()), titleStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Tong so dong", String.valueOf(rows == null ? 0 : rows.size()), labelStyle);
            rowIndex++;

            rowIndex = writeCellPair(sheet, rowIndex, "Tu khoa", safeText(search == null ? null : search.getQ()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Khoi", safeText(search == null ? null : search.getKhoi()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Lop", safeText(search == null ? null : search.getLop()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Mon", safeText(search == null ? null : search.getMon()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Hoc ky", semesterLabel(search == null ? null : search.getHocKy()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Khoa hoc", safeText(search == null ? null : search.getKhoa()), bodyStyle);
            rowIndex++;

            int headerRowIndex = rowIndex++;
            Row headerRow = sheet.createRow(headerRowIndex);
            int col = 0;
            writeCell(headerRow, col++, "STT", headerStyle);
            writeCell(headerRow, col++, "Ma hoc sinh", headerStyle);
            writeCell(headerRow, col++, "Ten hoc sinh", headerStyle);
            writeCell(headerRow, col++, "Lop", headerStyle);
            writeCell(headerRow, col++, "Mon", headerStyle);
            if (annualView) {
                writeCell(headerRow, col++, "Tong ket ky 1", headerStyle);
                writeCell(headerRow, col++, "Tong ket ky 2", headerStyle);
                writeCell(headerRow, col++, "Ca nam", headerStyle);
            } else {
                writeCell(headerRow, col++, "Giua ky", headerStyle);
                writeCell(headerRow, col++, "Cuoi ky", headerStyle);
                writeCell(headerRow, col++, "Tong ket", headerStyle);
            }
            if (!annualView) {
                writeCell(headerRow, col++, "Hoc ky", headerStyle);
            }
            writeCell(headerRow, col, "Nam hoc", headerStyle);

            int stt = 1;
            if (rows != null) {
                for (ScoreManagementService.ScoreRow item : rows) {
                    Row row = sheet.createRow(rowIndex++);
                    int rowCol = 0;
                    writeCell(row, rowCol++, String.valueOf(stt++), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getIdHocSinh()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getTenHocSinh()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getTenLop()), bodyStyle);
                    writeCell(row, rowCol++, safeText(item == null ? null : item.getTenMon()), bodyStyle);
                    if (annualView) {
                        writeCell(row, rowCol++, safeText(item == null ? null : item.getTongKetHocKy1Display()), bodyStyle);
                        writeCell(row, rowCol++, safeText(item == null ? null : item.getTongKetHocKy2Display()), bodyStyle);
                        writeCell(row, rowCol++, safeText(item == null ? null : item.getTongKetCaNamDisplay()), bodyStyle);
                    } else {
                        writeCell(row, rowCol++, safeText(item == null ? null : item.getDiemGiuaKyDisplay()), bodyStyle);
                        writeCell(row, rowCol++, safeText(item == null ? null : item.getDiemCuoiKyDisplay()), bodyStyle);
                        writeCell(row, rowCol++, safeText(item == null ? null : item.getTongKetDisplay()), bodyStyle);
                    }
                    if (!annualView) {
                        writeCell(row, rowCol++, safeText(item == null ? null : item.getHocKyDisplay()), bodyStyle);
                    }
                    writeCell(row, rowCol, safeText(item == null ? null : item.getNamHocDisplay()), bodyStyle);
                }
            }

            int lastColumn = annualView ? 8 : 9;
            for (int i = 0; i <= lastColumn; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Khong the xuat file Excel danh sach diem.");
        }
    }

    public byte[] exportPdf(List<ScoreManagementService.ScoreRow> rows, ScoreSearch search) {
        boolean annualView = isAnnualView(search);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 18, 18);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = createPdfFont(14, true);
            Font labelFont = createPdfFont(9, true);
            Font bodyFont = createPdfFont(9, false);
            Font metaFont = createPdfFont(8.5f, false);

            Paragraph title = new Paragraph("BAO CAO DIEM SO", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4f);
            document.add(title);

            Paragraph generated = new Paragraph("Ngay xuat: " + DATE_FORMATTER.format(LocalDate.now()), metaFont);
            generated.setSpacingAfter(6f);
            document.add(generated);

            Paragraph filters = new Paragraph(
                    "Bo loc: Tu khoa = " + valueOrDash(search == null ? null : search.getQ())
                            + " | Khoi = " + valueOrDash(search == null ? null : search.getKhoi())
                            + " | Lop = " + valueOrDash(search == null ? null : search.getLop())
                            + " | Mon = " + valueOrDash(search == null ? null : search.getMon())
                            + " | Hoc ky = " + semesterLabel(search == null ? null : search.getHocKy())
                            + " | Khoa hoc = " + valueOrDash(search == null ? null : search.getKhoa()),
                    metaFont
            );
            filters.setSpacingAfter(10f);
            document.add(filters);

            int columnCount = annualView ? 9 : 10;
            float[] widths = annualView
                    ? new float[]{1.0f, 1.6f, 3.1f, 1.6f, 1.9f, 1.25f, 1.25f, 1.2f, 1.3f}
                    : new float[]{1.0f, 1.6f, 3.1f, 1.6f, 1.9f, 1.25f, 1.25f, 1.2f, 1.4f, 1.3f};
            PdfPTable table = new PdfPTable(columnCount);
            table.setWidthPercentage(100);
            table.setWidths(widths);

            addHeaderCell(table, "STT", labelFont);
            addHeaderCell(table, "Ma hoc sinh", labelFont);
            addHeaderCell(table, "Ten hoc sinh", labelFont);
            addHeaderCell(table, "Lop", labelFont);
            addHeaderCell(table, "Mon", labelFont);
            if (annualView) {
                addHeaderCell(table, "Tong ket ky 1", labelFont);
                addHeaderCell(table, "Tong ket ky 2", labelFont);
                addHeaderCell(table, "Ca nam", labelFont);
            } else {
                addHeaderCell(table, "Giua ky", labelFont);
                addHeaderCell(table, "Cuoi ky", labelFont);
                addHeaderCell(table, "Tong ket", labelFont);
            }
            if (!annualView) {
                addHeaderCell(table, "Hoc ky", labelFont);
            }
            addHeaderCell(table, "Nam hoc", labelFont);

            int stt = 1;
            if (rows != null) {
                for (ScoreManagementService.ScoreRow item : rows) {
                    addBodyCell(table, String.valueOf(stt++), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getIdHocSinh()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getTenHocSinh()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getTenLop()), bodyFont);
                    addBodyCell(table, safeText(item == null ? null : item.getTenMon()), bodyFont);
                    if (annualView) {
                        addBodyCell(table, safeText(item == null ? null : item.getTongKetHocKy1Display()), bodyFont);
                        addBodyCell(table, safeText(item == null ? null : item.getTongKetHocKy2Display()), bodyFont);
                        addBodyCell(table, safeText(item == null ? null : item.getTongKetCaNamDisplay()), bodyFont);
                    } else {
                        addBodyCell(table, safeText(item == null ? null : item.getDiemGiuaKyDisplay()), bodyFont);
                        addBodyCell(table, safeText(item == null ? null : item.getDiemCuoiKyDisplay()), bodyFont);
                        addBodyCell(table, safeText(item == null ? null : item.getTongKetDisplay()), bodyFont);
                    }
                    if (!annualView) {
                        addBodyCell(table, safeText(item == null ? null : item.getHocKyDisplay()), bodyFont);
                    }
                    addBodyCell(table, safeText(item == null ? null : item.getNamHocDisplay()), bodyFont);
                }
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (IOException | DocumentException ex) {
            throw new RuntimeException("Khong the xuat file PDF danh sach diem.");
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
        font.setBold(true);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createExcelHeaderStyle(XSSFWorkbook workbook) {
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
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createExcelBodyStyle(XSSFWorkbook workbook) {
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

    private boolean isAnnualView(ScoreSearch search) {
        return search != null && "0".equals(safeText(search.getHocKy()));
    }

    private String semesterLabel(String hocKy) {
        String value = safeText(hocKy);
        if ("0".equals(value)) {
            return "Ca nam";
        }
        if ("1".equals(value)) {
            return "Hoc ky 1";
        }
        if ("2".equals(value)) {
            return "Hoc ky 2";
        }
        return "Tat ca";
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
