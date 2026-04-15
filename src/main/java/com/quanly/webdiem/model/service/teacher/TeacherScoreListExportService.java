package com.quanly.webdiem.model.service.teacher;

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
import com.quanly.webdiem.model.search.TeacherScoreSearch;
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
public class TeacherScoreListExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TeacherScoreService teacherScoreService;

    public TeacherScoreListExportService(TeacherScoreService teacherScoreService) {
        this.teacherScoreService = teacherScoreService;
    }

    public byte[] exportExcel(List<TeacherScoreService.ScoreRow> rows,
                              TeacherScoreService.ScoreDashboardData dashboardData) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("\u0110i\u1EC3m s\u1ED1 gi\u00E1o vi\u00EAn");

            CellStyle titleStyle = createExcelTitleStyle(workbook);
            CellStyle labelStyle = createExcelLabelStyle(workbook);
            CellStyle bodyStyle = createExcelBodyStyle(workbook);
            CellStyle headerStyle = createExcelHeaderStyle(workbook);

            TeacherScoreSearch search = dashboardData == null ? null : dashboardData.getSearch();
            boolean annualView = search != null && "0".equals(search.getHocKy());

            int rowIndex = 0;
            rowIndex = writeCellPair(
                    sheet,
                    rowIndex,
                    "B\u00C1O C\u00C1O \u0110I\u1EC2M S\u1ED0 GI\u00C1O VI\u00CAN",
                    "Ng\u00E0y xu\u1EA5t: " + DATE_FORMATTER.format(LocalDate.now()),
                    titleStyle
            );
            rowIndex = writeCellPair(sheet, rowIndex, "Gi\u00E1o vi\u00EAn", valueOrDash(dashboardData == null ? null : dashboardData.getTeacherId()), labelStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "N\u0103m h\u1ECDc", valueOrDash(dashboardData == null ? null : dashboardData.getSchoolYear()), labelStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "T\u1ED5ng s\u1ED1 d\u00F2ng", String.valueOf(rows == null ? 0 : rows.size()), labelStyle);
            rowIndex++;

            rowIndex = writeCellPair(sheet, rowIndex, "T\u1EEB kh\u00F3a", valueOrDash(search == null ? null : search.getQ()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "Nh\u00F3m l\u1EDBp", resolveClassScopeLabel(search == null ? null : search.getClassScope()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "L\u1EDBp", resolveClassLabel(dashboardData, search == null ? null : search.getClassId()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "H\u1ECDc l\u1EF1c", resolveAcademicLevelLabel(search == null ? null : search.getHocLuc()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "M\u00F4n h\u1ECDc", resolveSubjectLabel(dashboardData, search == null ? null : search.getMon()), bodyStyle);
            rowIndex = writeCellPair(sheet, rowIndex, "H\u1ECDc k\u1EF3", resolveSemesterLabel(search == null ? null : search.getHocKy()), bodyStyle);
            rowIndex++;

            int headerRowIndex = rowIndex++;
            Row headerRow = sheet.createRow(headerRowIndex);
            int columnIndex = 0;
            writeCell(headerRow, columnIndex++, "STT", headerStyle);
            writeCell(headerRow, columnIndex++, "M\u00E3 HS", headerStyle);
            writeCell(headerRow, columnIndex++, "H\u1ECD v\u00E0 t\u00EAn", headerStyle);
            writeCell(headerRow, columnIndex++, "L\u1EDBp", headerStyle);
            writeCell(headerRow, columnIndex++, "Lo\u1EA1i l\u1EDBp", headerStyle);
            writeCell(headerRow, columnIndex++, "M\u00F4n h\u1ECDc", headerStyle);
            if (annualView) {
                writeCell(headerRow, columnIndex++, "T\u1ED5ng k\u1EBFt k\u1EF3 1", headerStyle);
                writeCell(headerRow, columnIndex++, "T\u1ED5ng k\u1EBFt k\u1EF3 2", headerStyle);
                writeCell(headerRow, columnIndex++, "C\u1EA3 n\u0103m", headerStyle);
            } else {
                writeCell(headerRow, columnIndex++, "Gi\u1EEFa k\u1EF3", headerStyle);
                writeCell(headerRow, columnIndex++, "Cu\u1ED1i k\u1EF3", headerStyle);
                writeCell(headerRow, columnIndex++, "Trung b\u00ECnh", headerStyle);
            }
            writeCell(headerRow, columnIndex++, "H\u1ECDc k\u1EF3", headerStyle);
            writeCell(headerRow, columnIndex, "N\u0103m h\u1ECDc", headerStyle);

            int order = 1;
            if (rows != null) {
                for (TeacherScoreService.ScoreRow row : rows) {
                    Row bodyRow = sheet.createRow(rowIndex++);
                    int bodyColumn = 0;
                    writeCell(bodyRow, bodyColumn++, String.valueOf(order++), bodyStyle);
                    writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getStudentId()), bodyStyle);
                    writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getStudentName()), bodyStyle);
                    writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getClassDisplay()), bodyStyle);
                    writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getClassScopeDisplay()), bodyStyle);
                    writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getSubjectName()), bodyStyle);
                    if (annualView) {
                        writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getTongKetHocKy1Display()), bodyStyle);
                        writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getTongKetHocKy2Display()), bodyStyle);
                        writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getTongKetCaNamDisplay()), bodyStyle);
                    } else {
                        writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getDiemGiuaKyDisplay()), bodyStyle);
                        writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getDiemCuoiKyDisplay()), bodyStyle);
                        writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getTongKetDisplay()), bodyStyle);
                    }
                    writeCell(bodyRow, bodyColumn++, valueOrDash(row == null ? null : row.getHocKyDisplay()), bodyStyle);
                    writeCell(bodyRow, bodyColumn, valueOrDash(row == null ? null : row.getNamHoc()), bodyStyle);
                }
            }

            for (int index = 0; index <= 10; index++) {
                sheet.autoSizeColumn(index);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Kh\u00F4ng th\u1EC3 xu\u1EA5t file Excel danh s\u00E1ch \u0111i\u1EC3m gi\u00E1o vi\u00EAn.");
        }
    }

    public byte[] exportPdf(List<TeacherScoreService.ScoreRow> rows,
                            TeacherScoreService.ScoreDashboardData dashboardData) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 18, 18);
            PdfWriter.getInstance(document, output);
            document.open();

            TeacherScoreSearch search = dashboardData == null ? null : dashboardData.getSearch();
            boolean annualView = search != null && "0".equals(search.getHocKy());

            Font titleFont = createPdfFont(14, true);
            Font labelFont = createPdfFont(9, true);
            Font bodyFont = createPdfFont(9, false);
            Font metaFont = createPdfFont(8.5f, false);

            Paragraph title = new Paragraph("B\u00C1O C\u00C1O \u0110I\u1EC2M S\u1ED0 GI\u00C1O VI\u00CAN", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4f);
            document.add(title);

            Paragraph generated = new Paragraph("Ng\u00E0y xu\u1EA5t: " + DATE_FORMATTER.format(LocalDate.now()), metaFont);
            generated.setSpacingAfter(5f);
            document.add(generated);

            Paragraph teacherMeta = new Paragraph(
                    "Gi\u00E1o vi\u00EAn: " + valueOrDash(dashboardData == null ? null : dashboardData.getTeacherId())
                            + " | N\u0103m h\u1ECDc: " + valueOrDash(dashboardData == null ? null : dashboardData.getSchoolYear()),
                    metaFont
            );
            teacherMeta.setSpacingAfter(5f);
            document.add(teacherMeta);

            Paragraph filters = new Paragraph(
                    "B\u1ED9 l\u1ECDc: T\u1EEB kh\u00F3a = " + valueOrDash(search == null ? null : search.getQ())
                            + " | Nh\u00F3m l\u1EDBp = " + resolveClassScopeLabel(search == null ? null : search.getClassScope())
                            + " | L\u1EDBp = " + resolveClassLabel(dashboardData, search == null ? null : search.getClassId())
                            + " | H\u1ECDc l\u1EF1c = " + resolveAcademicLevelLabel(search == null ? null : search.getHocLuc())
                            + " | M\u00F4n = " + resolveSubjectLabel(dashboardData, search == null ? null : search.getMon())
                            + " | H\u1ECDc k\u1EF3 = " + resolveSemesterLabel(search == null ? null : search.getHocKy()),
                    metaFont
            );
            filters.setSpacingAfter(10f);
            document.add(filters);

            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.75f, 1.2f, 2.8f, 1.7f, 1.45f, 1.8f, 1.0f, 1.0f, 1.05f, 1.2f, 1.2f});

            addHeaderCell(table, "STT", labelFont);
            addHeaderCell(table, "M\u00E3 HS", labelFont);
            addHeaderCell(table, "H\u1ECD v\u00E0 t\u00EAn", labelFont);
            addHeaderCell(table, "L\u1EDBp", labelFont);
            addHeaderCell(table, "Lo\u1EA1i l\u1EDBp", labelFont);
            addHeaderCell(table, "M\u00F4n h\u1ECDc", labelFont);
            if (annualView) {
                addHeaderCell(table, "T\u1ED5ng k\u1EBFt k\u1EF3 1", labelFont);
                addHeaderCell(table, "T\u1ED5ng k\u1EBFt k\u1EF3 2", labelFont);
                addHeaderCell(table, "C\u1EA3 n\u0103m", labelFont);
            } else {
                addHeaderCell(table, "Gi\u1EEFa k\u1EF3", labelFont);
                addHeaderCell(table, "Cu\u1ED1i k\u1EF3", labelFont);
                addHeaderCell(table, "Trung b\u00ECnh", labelFont);
            }
            addHeaderCell(table, "H\u1ECDc k\u1EF3", labelFont);
            addHeaderCell(table, "N\u0103m h\u1ECDc", labelFont);

            int order = 1;
            if (rows != null) {
                for (TeacherScoreService.ScoreRow row : rows) {
                    addBodyCell(table, String.valueOf(order++), bodyFont, Element.ALIGN_CENTER);
                    addBodyCell(table, valueOrDash(row == null ? null : row.getStudentId()), bodyFont, Element.ALIGN_CENTER);
                    addBodyCell(table, valueOrDash(row == null ? null : row.getStudentName()), bodyFont, Element.ALIGN_LEFT);
                    addBodyCell(table, valueOrDash(row == null ? null : row.getClassDisplay()), bodyFont, Element.ALIGN_LEFT);
                    addBodyCell(table, valueOrDash(row == null ? null : row.getClassScopeDisplay()), bodyFont, Element.ALIGN_CENTER);
                    addBodyCell(table, valueOrDash(row == null ? null : row.getSubjectName()), bodyFont, Element.ALIGN_LEFT);
                    if (annualView) {
                        addBodyCell(table, valueOrDash(row == null ? null : row.getTongKetHocKy1Display()), bodyFont, Element.ALIGN_CENTER);
                        addBodyCell(table, valueOrDash(row == null ? null : row.getTongKetHocKy2Display()), bodyFont, Element.ALIGN_CENTER);
                        addBodyCell(table, valueOrDash(row == null ? null : row.getTongKetCaNamDisplay()), bodyFont, Element.ALIGN_CENTER);
                    } else {
                        addBodyCell(table, valueOrDash(row == null ? null : row.getDiemGiuaKyDisplay()), bodyFont, Element.ALIGN_CENTER);
                        addBodyCell(table, valueOrDash(row == null ? null : row.getDiemCuoiKyDisplay()), bodyFont, Element.ALIGN_CENTER);
                        addBodyCell(table, valueOrDash(row == null ? null : row.getTongKetDisplay()), bodyFont, Element.ALIGN_CENTER);
                    }
                    addBodyCell(table, valueOrDash(row == null ? null : row.getHocKyDisplay()), bodyFont, Element.ALIGN_CENTER);
                    addBodyCell(table, valueOrDash(row == null ? null : row.getNamHoc()), bodyFont, Element.ALIGN_CENTER);
                }
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (IOException | DocumentException ex) {
            throw new RuntimeException("Kh\u00F4ng th\u1EC3 xu\u1EA5t file PDF danh s\u00E1ch \u0111i\u1EC3m gi\u00E1o vi\u00EAn.");
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

    private void addBodyCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(valueOrDash(text), font));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(align);
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

    private String resolveClassScopeLabel(String value) {
        if ("HOMEROOM".equalsIgnoreCase(value)) {
            return "L\u1EDBp ch\u1EE7 nhi\u1EC7m";
        }
        if ("SUBJECT".equalsIgnoreCase(value)) {
            return "L\u1EDBp b\u1ED9 m\u00F4n";
        }
        return "T\u1EA5t c\u1EA3 nh\u00F3m";
    }

    private String resolveSemesterLabel(String value) {
        if ("0".equals(value)) {
            return "C\u1EA3 n\u0103m";
        }
        if ("1".equals(value)) {
            return "H\u1ECDc k\u1EF3 I";
        }
        if ("2".equals(value)) {
            return "H\u1ECDc k\u1EF3 II";
        }
        return "T\u1EA5t c\u1EA3 h\u1ECDc k\u1EF3";
    }

    private String resolveClassLabel(TeacherScoreService.ScoreDashboardData dashboardData, String classId) {
        if (classId == null || classId.isBlank()) {
            return "T\u1EA5t c\u1EA3 l\u1EDBp";
        }
        if (dashboardData == null || dashboardData.getClassOptions() == null) {
            return classId;
        }
        for (TeacherScoreService.ClassFilterOption item : dashboardData.getClassOptions()) {
            if (item != null && item.getId() != null && item.getId().equalsIgnoreCase(classId)) {
                return valueOrDash(item.getName()) + " (" + item.getId() + ")";
            }
        }
        return classId;
    }

    private String resolveSubjectLabel(TeacherScoreService.ScoreDashboardData dashboardData, String subjectId) {
        if (subjectId == null || subjectId.isBlank()) {
            return "T\u1EA5t c\u1EA3 m\u00F4n";
        }
        if (dashboardData == null || dashboardData.getSubjectOptions() == null) {
            return subjectId;
        }
        for (TeacherScoreService.FilterOption item : dashboardData.getSubjectOptions()) {
            if (item != null && item.getId() != null && item.getId().equalsIgnoreCase(subjectId)) {
                return valueOrDash(item.getName()) + " (" + item.getId() + ")";
            }
        }
        return subjectId;
    }

    private String resolveAcademicLevelLabel(String hocLuc) {
        return teacherScoreService.resolveAcademicLevelLabel(hocLuc);
    }

    private String valueOrDash(String value) {
        if (value == null) {
            return "-";
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? "-" : normalized;
    }
}
