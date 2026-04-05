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
            Sheet sheet = workbook.createSheet("Danh sách điểm");

            CellStyle titleStyle = createExcelTitleStyle(workbook);
            CellStyle labelStyle = createExcelLabelStyle(workbook);
            CellStyle bodyStyle = createExcelBodyStyle(workbook);
            CellStyle headerStyle = createExcelHeaderStyle(workbook);

            int rowIndex = 0;
            rowIndex = writeCellPair(sheet, rowIndex, "BÁO CÁO ĐIỂM SỐ", "Ngày xuất: " + DATE_FORMATTER.format(LocalDate.now()), titleStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Tổng số dòng", String.valueOf(rows == null ? 0 : rows.size()), labelStyle);
            rowIndex++;

            rowIndex = writeCellPair(sheet, rowIndex, "Từ khóa", safeText(search == null ? null : search.getQ()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Khối", safeText(search == null ? null : search.getKhoi()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Lớp", safeText(search == null ? null : search.getLop()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Môn", safeText(search == null ? null : search.getMon()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Học kỳ", semesterLabel(search == null ? null : search.getHocKy()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Khóa học", safeText(search == null ? null : search.getKhoa()), bodyStyle);
            rowIndex++;

            int headerRowIndex = rowIndex++;
            Row headerRow = sheet.createRow(headerRowIndex);
            int col = 0;
            writeCell(headerRow, col++, "STT", headerStyle);
            writeCell(headerRow, col++, "Mã học sinh", headerStyle);
            writeCell(headerRow, col++, "Tên học sinh", headerStyle);
            writeCell(headerRow, col++, "Lớp", headerStyle);
            writeCell(headerRow, col++, "Môn", headerStyle);
            if (annualView) {
                writeCell(headerRow, col++, "Tổng kết kỳ 1", headerStyle);
                writeCell(headerRow, col++, "Tổng kết kỳ 2", headerStyle);
                writeCell(headerRow, col++, "Cả năm", headerStyle);
            } else {
                writeCell(headerRow, col++, "Giữa kỳ", headerStyle);
                writeCell(headerRow, col++, "Cuối kỳ", headerStyle);
                writeCell(headerRow, col++, "Tổng kết", headerStyle);
            }
            writeCell(headerRow, col++, "Xếp loại", headerStyle);
            if (!annualView) {
                writeCell(headerRow, col++, "Học kỳ", headerStyle);
            }
            writeCell(headerRow, col, "Năm học", headerStyle);

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
                    writeCell(row, rowCol++, resolveRankDisplay(item, annualView), bodyStyle);
                    if (!annualView) {
                        writeCell(row, rowCol++, safeText(item == null ? null : item.getHocKyDisplay()), bodyStyle);
                    }
                    writeCell(row, rowCol, safeText(item == null ? null : item.getNamHocDisplay()), bodyStyle);
                }
            }

            int lastColumn = annualView ? 9 : 10;
            for (int i = 0; i <= lastColumn; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể xuất file Excel danh sách điểm.");
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

            Paragraph title = new Paragraph("BÁO CÁO ĐIỂM SỐ", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4f);
            document.add(title);

            Paragraph generated = new Paragraph("Ngày xuất: " + DATE_FORMATTER.format(LocalDate.now()), metaFont);
            generated.setSpacingAfter(6f);
            document.add(generated);

            Paragraph filters = new Paragraph(
                    "Bộ lọc: Từ khóa = " + valueOrDash(search == null ? null : search.getQ())
                            + " | Khối = " + valueOrDash(search == null ? null : search.getKhoi())
                            + " | Lớp = " + valueOrDash(search == null ? null : search.getLop())
                            + " | Môn = " + valueOrDash(search == null ? null : search.getMon())
                            + " | Học kỳ = " + semesterLabel(search == null ? null : search.getHocKy())
                            + " | Khóa học = " + valueOrDash(search == null ? null : search.getKhoa()),
                    metaFont
            );
            filters.setSpacingAfter(10f);
            document.add(filters);

            int columnCount = annualView ? 10 : 11;
            float[] widths = annualView
                    ? new float[]{0.9f, 1.6f, 3.1f, 1.6f, 1.9f, 1.3f, 1.3f, 1.2f, 1.35f, 1.35f}
                    : new float[]{0.9f, 1.6f, 3.1f, 1.6f, 1.9f, 1.2f, 1.2f, 1.2f, 1.35f, 1.2f, 1.3f};
            PdfPTable table = new PdfPTable(columnCount);
            table.setWidthPercentage(100);
            table.setWidths(widths);

            addHeaderCell(table, "STT", labelFont);
            addHeaderCell(table, "Mã học sinh", labelFont);
            addHeaderCell(table, "Tên học sinh", labelFont);
            addHeaderCell(table, "Lớp", labelFont);
            addHeaderCell(table, "Môn", labelFont);
            if (annualView) {
                addHeaderCell(table, "Tổng kết kỳ 1", labelFont);
                addHeaderCell(table, "Tổng kết kỳ 2", labelFont);
                addHeaderCell(table, "Cả năm", labelFont);
            } else {
                addHeaderCell(table, "Giữa kỳ", labelFont);
                addHeaderCell(table, "Cuối kỳ", labelFont);
                addHeaderCell(table, "Tổng kết", labelFont);
            }
            addHeaderCell(table, "Xếp loại", labelFont);
            if (!annualView) {
                addHeaderCell(table, "Học kỳ", labelFont);
            }
            addHeaderCell(table, "Năm học", labelFont);

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
                    addBodyCell(table, resolveRankDisplay(item, annualView), bodyFont);
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
            throw new RuntimeException("Không thể xuất file PDF danh sách điểm.");
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

    private String resolveRankDisplay(ScoreManagementService.ScoreRow row, boolean annualView) {
        if (row == null) {
            return "-";
        }
        Double score = annualView ? row.getTongKetCaNam() : row.getTongKet();
        if (score == null) {
            return "-";
        }
        if (score >= 8.0) {
            return "Giỏi";
        }
        if (score >= 6.5) {
            return "Khá";
        }
        if (score >= 5.0) {
            return "Trung bình";
        }
        if (score >= 3.5) {
            return "Yếu";
        }
        return "Kém";
    }

    private boolean isAnnualView(ScoreSearch search) {
        return search != null && "0".equals(safeText(search.getHocKy()));
    }

    private String semesterLabel(String hocKy) {
        String value = safeText(hocKy);
        if ("0".equals(value)) {
            return "Cả năm";
        }
        if ("1".equals(value)) {
            return "Học kỳ 1";
        }
        if ("2".equals(value)) {
            return "Học kỳ 2";
        }
        return "Tất cả";
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
