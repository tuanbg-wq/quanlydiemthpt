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
import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreDetailExportService {

    private static final String SEMESTER_ALL = "0";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exportExcel(ScoreManagementService.ScoreGroupSummary summary,
                              ScoreCreateService.ScoreCreatePageData detailData,
                              String hocKy) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Chi tiết điểm");

            CellStyle titleStyle = createExcelTitleStyle(workbook);
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle bodyStyle = createExcelBodyStyle(workbook);
            CellStyle sectionStyle = createExcelSectionStyle(workbook);

            int rowIndex = 0;
            rowIndex = writeExcelTitle(sheet, rowIndex, "CHI TIẾT ĐIỂM SỐ - " + safeText(summary.getSubjectName()), titleStyle);
            rowIndex = writeExcelTitle(sheet, rowIndex, "Ngày xuất: " + DATE_FORMATTER.format(LocalDate.now()), bodyStyle);
            rowIndex++;

            rowIndex = writeExcelPair(sheet, rowIndex, "Học sinh", safeText(summary.getStudentName()), bodyStyle);
            rowIndex = writeExcelPair(sheet, rowIndex, "Mã học sinh", safeText(summary.getStudentId()), bodyStyle);
            rowIndex = writeExcelPair(sheet, rowIndex, "Lớp", safeText(summary.getClassName()), bodyStyle);
            rowIndex = writeExcelPair(sheet, rowIndex, "Khối", safeText(summary.getGrade()), bodyStyle);
            rowIndex = writeExcelPair(sheet, rowIndex, "Khóa", safeText(summary.getCourseDisplay()), bodyStyle);
            rowIndex = writeExcelPair(sheet, rowIndex, "Môn học", safeText(summary.getSubjectName()), bodyStyle);
            rowIndex = writeExcelPair(sheet, rowIndex, "Năm học", safeText(summary.getNamHoc()), bodyStyle);
            rowIndex = writeExcelPair(sheet, rowIndex, "Chế độ xem", semesterLabel(hocKy), bodyStyle);
            rowIndex++;

            rowIndex = writeExcelPair(sheet, rowIndex, "Công thức", safeText(detailData.getFormulaText()), bodyStyle);
            rowIndex = writeExcelPair(sheet, rowIndex, "Công thức cả năm", "ĐTBmcn = (ĐTBhkI + 2 x ĐTBhkII) / 3", bodyStyle);
            rowIndex++;

            List<Integer> semesters = resolveSemesters(hocKy);
            for (Integer semester : semesters) {
                ScoreCreateService.SemesterInput input = semester == 1 ? detailData.getHk1Input() : detailData.getHk2Input();
                String teacher = semester == 1 ? detailData.getFilter().getTeacherHk1() : detailData.getFilter().getTeacherHk2();
                rowIndex = writeSemesterExcelTable(
                        sheet,
                        rowIndex,
                        "Học kỳ " + (semester == 1 ? "I" : "II"),
                        teacher,
                        input,
                        detailData.getFrequentColumns(),
                        sectionStyle,
                        headerStyle,
                        bodyStyle
                );
                rowIndex++;
            }

            if (SEMESTER_ALL.equals(hocKy)) {
                rowIndex = writeAnnualExcelSummary(sheet, rowIndex, detailData, sectionStyle, headerStyle, bodyStyle);
            }

            for (int col = 0; col < 8; col++) {
                sheet.autoSizeColumn(col);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể xuất file Excel chi tiết điểm.");
        }
    }

    public byte[] exportPdf(ScoreManagementService.ScoreGroupSummary summary,
                            ScoreCreateService.ScoreCreatePageData detailData,
                            String hocKy) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 28, 28, 26, 26);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = createPdfFont(16, true);
            Font sectionFont = createPdfFont(12, true);
            Font labelFont = createPdfFont(10, true);
            Font bodyFont = createPdfFont(10, false);
            Font metaFont = createPdfFont(9, false);

            Paragraph title = new Paragraph("CHI TIẾT ĐIỂM SỐ - " + safeText(summary.getSubjectName()), titleFont);
            title.setSpacingAfter(6f);
            document.add(title);

            Paragraph generatedAt = new Paragraph("Ngày xuất: " + DATE_FORMATTER.format(LocalDate.now()), metaFont);
            generatedAt.setSpacingAfter(12f);
            document.add(generatedAt);

            PdfPTable infoTable = new PdfPTable(new float[]{2.1f, 3.1f, 2.1f, 3.1f});
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(12f);
            addInfoRow(infoTable, "Học sinh", summary.getStudentName(), "Mã học sinh", summary.getStudentId(), labelFont, bodyFont);
            addInfoRow(infoTable, "Lớp", summary.getClassName(), "Khối", summary.getGrade(), labelFont, bodyFont);
            addInfoRow(infoTable, "Khóa", summary.getCourseDisplay(), "Năm học", summary.getNamHoc(), labelFont, bodyFont);
            addInfoRow(infoTable, "Môn học", summary.getSubjectName(), "Chế độ xem", semesterLabel(hocKy), labelFont, bodyFont);
            document.add(infoTable);

            Paragraph formula = new Paragraph("Công thức: " + safeText(detailData.getFormulaText()), metaFont);
            formula.setSpacingAfter(3f);
            document.add(formula);
            Paragraph annualFormula = new Paragraph("ĐTBmcn = (ĐTBhkI + 2 x ĐTBhkII) / 3", metaFont);
            annualFormula.setSpacingAfter(10f);
            document.add(annualFormula);

            List<Integer> semesters = resolveSemesters(hocKy);
            for (Integer semester : semesters) {
                ScoreCreateService.SemesterInput input = semester == 1 ? detailData.getHk1Input() : detailData.getHk2Input();
                String teacher = semester == 1 ? detailData.getFilter().getTeacherHk1() : detailData.getFilter().getTeacherHk2();
                addSemesterPdfSection(
                        document,
                        semester == 1 ? "Học kỳ I" : "Học kỳ II",
                        teacher,
                        input,
                        detailData.getFrequentColumns(),
                        sectionFont,
                        labelFont,
                        bodyFont
                );
            }

            if (SEMESTER_ALL.equals(hocKy)) {
        Paragraph annualTitle = new Paragraph("KẾT QUẢ CẢ NĂM", sectionFont);
                annualTitle.setSpacingBefore(8f);
                annualTitle.setSpacingAfter(6f);
                document.add(annualTitle);

                PdfPTable annualTable = new PdfPTable(new float[]{2.4f, 2.0f, 2.0f});
                annualTable.setWidthPercentage(100);
                addHeaderCell(annualTable, "ĐTB HK I", labelFont);
                addHeaderCell(annualTable, "ĐTB HK II", labelFont);
                addHeaderCell(annualTable, "ĐTB cả năm", labelFont);
                addBodyCell(annualTable, detailData.getHk1Input().getAverageDisplay(), bodyFont);
                addBodyCell(annualTable, detailData.getHk2Input().getAverageDisplay(), bodyFont);
                addBodyCell(annualTable, detailData.getYearAverageDisplay(), bodyFont);
                document.add(annualTable);
            }

            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new RuntimeException("Không thể xuất file PDF chi tiết điểm.");
        }
    }

    private int writeSemesterExcelTable(Sheet sheet,
                                        int rowIndex,
                                        String title,
                                        String teacher,
                                        ScoreCreateService.SemesterInput input,
                                        int frequentColumns,
                                        CellStyle sectionStyle,
                                        CellStyle headerStyle,
                                        CellStyle bodyStyle) {
        Row sectionRow = sheet.createRow(rowIndex++);
        writeCell(sectionRow, 0, title, sectionStyle);

        Row teacherRow = sheet.createRow(rowIndex++);
        writeCell(teacherRow, 0, "Giáo viên chấm", headerStyle);
        writeCell(teacherRow, 1, valueOrDash(teacher), bodyStyle);

        Row headerRow = sheet.createRow(rowIndex++);
        int col = 0;
        for (int i = 1; i <= frequentColumns; i++) {
            writeCell(headerRow, col++, "TX " + i, headerStyle);
        }
        writeCell(headerRow, col++, "Giữa kỳ (HS2)", headerStyle);
        writeCell(headerRow, col++, "Cuối kỳ (HS3)", headerStyle);
        writeCell(headerRow, col, "ĐTB học kỳ", headerStyle);

        Row valueRow = sheet.createRow(rowIndex++);
        col = 0;
        List<String> txScores = input.getFrequentScores() == null ? List.of() : input.getFrequentScores();
        for (int i = 0; i < frequentColumns; i++) {
            String txValue = i < txScores.size() ? txScores.get(i) : "";
            writeCell(valueRow, col++, valueOrDash(txValue), bodyStyle);
        }
        writeCell(valueRow, col++, valueOrDash(input.getMidterm()), bodyStyle);
        writeCell(valueRow, col++, valueOrDash(input.getFinalScore()), bodyStyle);
        writeCell(valueRow, col, valueOrDash(input.getAverageDisplay()), bodyStyle);
        return rowIndex;
    }

    private int writeAnnualExcelSummary(Sheet sheet,
                                        int rowIndex,
                                        ScoreCreateService.ScoreCreatePageData detailData,
                                        CellStyle sectionStyle,
                                        CellStyle headerStyle,
                                        CellStyle bodyStyle) {
        Row sectionRow = sheet.createRow(rowIndex++);
        writeCell(sectionRow, 0, "KẾT QUẢ CẢ NĂM", sectionStyle);

        Row headerRow = sheet.createRow(rowIndex++);
        writeCell(headerRow, 0, "ĐTB HK I", headerStyle);
        writeCell(headerRow, 1, "ĐTB HK II", headerStyle);
        writeCell(headerRow, 2, "ĐTB cả năm", headerStyle);

        Row valueRow = sheet.createRow(rowIndex++);
        writeCell(valueRow, 0, valueOrDash(detailData.getHk1Input().getAverageDisplay()), bodyStyle);
        writeCell(valueRow, 1, valueOrDash(detailData.getHk2Input().getAverageDisplay()), bodyStyle);
        writeCell(valueRow, 2, valueOrDash(detailData.getYearAverageDisplay()), bodyStyle);
        return rowIndex;
    }

    private void addSemesterPdfSection(Document document,
                                       String title,
                                       String teacher,
                                       ScoreCreateService.SemesterInput input,
                                       int frequentColumns,
                                       Font sectionFont,
                                       Font labelFont,
                                       Font bodyFont) throws DocumentException {
        Paragraph semesterTitle = new Paragraph(title, sectionFont);
        semesterTitle.setSpacingBefore(8f);
        semesterTitle.setSpacingAfter(5f);
        document.add(semesterTitle);

        Paragraph teacherLine = new Paragraph("Giáo viên chấm: " + valueOrDash(teacher), bodyFont);
        teacherLine.setSpacingAfter(5f);
        document.add(teacherLine);

        PdfPTable table = new PdfPTable(frequentColumns + 3);
        table.setWidthPercentage(100);

        for (int i = 1; i <= frequentColumns; i++) {
            addHeaderCell(table, "TX " + i, labelFont);
        }
        addHeaderCell(table, "Giữa kỳ", labelFont);
        addHeaderCell(table, "Cuối kỳ", labelFont);
        addHeaderCell(table, "ĐTB", labelFont);

        List<String> txScores = input.getFrequentScores() == null ? List.of() : input.getFrequentScores();
        for (int i = 0; i < frequentColumns; i++) {
            String txValue = i < txScores.size() ? txScores.get(i) : "";
            addBodyCell(table, valueOrDash(txValue), bodyFont);
        }
        addBodyCell(table, valueOrDash(input.getMidterm()), bodyFont);
        addBodyCell(table, valueOrDash(input.getFinalScore()), bodyFont);
        addBodyCell(table, valueOrDash(input.getAverageDisplay()), bodyFont);
        document.add(table);
    }

    private int writeExcelTitle(Sheet sheet, int rowIndex, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        writeCell(row, 0, value, style);
        return rowIndex + 1;
    }

    private int writeExcelPair(Sheet sheet, int rowIndex, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        writeCell(row, 0, label, style);
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
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createExcelSectionStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createExcelHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createExcelBodyStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void addInfoRow(PdfPTable table,
                            String label1,
                            String value1,
                            String label2,
                            String value2,
                            Font labelFont,
                            Font bodyFont) {
        addInfoLabelCell(table, label1, labelFont);
        addInfoValueCell(table, value1, bodyFont);
        addInfoLabelCell(table, label2, labelFont);
        addInfoValueCell(table, value2, bodyFont);
    }

    private void addInfoLabelCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(7f);
        cell.setBackgroundColor(new java.awt.Color(245, 247, 250));
        table.addCell(cell);
    }

    private void addInfoValueCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valueOrDash(text), font));
        cell.setPadding(7f);
        cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        table.addCell(cell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(7f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new java.awt.Color(231, 236, 242));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valueOrDash(text), font));
        cell.setPadding(7f);
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

    private List<Integer> resolveSemesters(String hocKy) {
        if (SEMESTER_ALL.equals(hocKy)) {
            return List.of(1, 2);
        }
        if ("1".equals(hocKy)) {
            return List.of(1);
        }
        if ("2".equals(hocKy)) {
            return List.of(2);
        }
        List<Integer> fallback = new ArrayList<>();
        fallback.add(1);
        fallback.add(2);
        return fallback;
    }

    private String semesterLabel(String hocKy) {
        if ("1".equals(hocKy)) {
            return "Học kỳ I";
        }
        if ("2".equals(hocKy)) {
            return "Học kỳ II";
        }
        return "Cả năm";
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

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
