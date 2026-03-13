package com.quanly.webdiem.model.service.admin;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.quanly.webdiem.model.service.admin.TeacherInfoService.TeacherInfoView;
import com.quanly.webdiem.model.service.admin.TeacherInfoService.WorkHistoryItem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TeacherInfoExportService {

    private final Path uploadRoot;

    public TeacherInfoExportService(@Value("${app.upload.dir:${app.upload-dir:uploads}}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public byte[] exportExcel(TeacherInfoView teacherInfo) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet infoSheet = workbook.createSheet("Thong tin giao vien");
            Sheet historySheet = workbook.createSheet("Lich su cong tac");

            fillInfoSheet(workbook, infoSheet, teacherInfo);
            fillHistorySheet(workbook, historySheet, teacherInfo.getWorkHistory());

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Kh\u00f4ng th\u1ec3 xu\u1ea5t Excel gi\u00e1o vi\u00ean.");
        }
    }

    public byte[] exportPdf(TeacherInfoView teacherInfo) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 28, 28);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = createFont(16, true);
            Font sectionFont = createFont(12, true);
            Font labelFont = createFont(10, true);
            Font bodyFont = createFont(10, false);
            Font metaFont = createFont(9, false);

            Paragraph title = new Paragraph("TH\u00d4NG TIN GI\u00c1O VI\u00caN", titleFont);
            title.setSpacingAfter(4f);
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(title);

            Paragraph generatedAt = new Paragraph(
                    "Ng\u00e0y xu\u1ea5t: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    metaFont
            );
            generatedAt.setSpacingAfter(12f);
            document.add(generatedAt);

            addProfileSection(document, teacherInfo, sectionFont, labelFont, bodyFont);

            Paragraph infoTitle = new Paragraph("Th\u00f4ng tin chi ti\u1ebft", sectionFont);
            infoTitle.setSpacingAfter(6f);
            document.add(infoTitle);

            PdfPTable infoTable = new PdfPTable(new float[]{2.2f, 3.8f, 2.2f, 3.8f});
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(12f);

            addInfoRow(infoTable, "M\u00e3 gi\u00e1o vi\u00ean", teacherInfo.getIdGiaoVien(), "H\u1ecd v\u00e0 t\u00ean", teacherInfo.getHoTen(), labelFont, bodyFont);
            addInfoRow(infoTable, "Ng\u00e0y sinh", valueOrDash(teacherInfo.getNgaySinh()), "Gi\u1edbi t\u00ednh", teacherInfo.getGioiTinh(), labelFont, bodyFont);
            addInfoRow(infoTable, "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i", teacherInfo.getSoDienThoai(), "Email", teacherInfo.getEmail(), labelFont, bodyFont);
            addInfoRow(infoTable, "\u0110\u1ecba ch\u1ec9", teacherInfo.getDiaChi(), "Tr\u1ea1ng th\u00e1i", teacherInfo.getTrangThai(), labelFont, bodyFont);
            addInfoRow(infoTable, "Chuy\u00ean m\u00f4n", teacherInfo.getChuyenMon(), "Tr\u00ecnh \u0111\u1ed9 h\u1ecdc v\u1ea5n", teacherInfo.getTrinhDo(), labelFont, bodyFont);
            addInfoRow(infoTable, "Ng\u00e0y b\u1eaft \u0111\u1ea7u c\u00f4ng t\u00e1c", valueOrDash(teacherInfo.getNgayVaoLam()), "N\u0103m h\u1ecdc \u00e1p d\u1ee5ng vai tr\u00f2", teacherInfo.getCurrentRoleSchoolYear(), labelFont, bodyFont);
            addInfoRow(infoTable, "Vai tr\u00f2 hi\u1ec7n t\u1ea1i", teacherInfo.getCurrentRole(), "Ghi ch\u00fa", teacherInfo.getGhiChu(), labelFont, bodyFont);
            document.add(infoTable);

            Paragraph historyTitle = new Paragraph("L\u1ecbch s\u1eed c\u00f4ng t\u00e1c t\u1ea1i tr\u01b0\u1eddng", sectionFont);
            historyTitle.setSpacingAfter(6f);
            document.add(historyTitle);

            PdfPTable historyTable = new PdfPTable(new float[]{2.2f, 2.8f, 2.5f, 2.5f});
            historyTable.setWidthPercentage(100);
            addHeaderCell(historyTable, "Kho\u1ea3ng th\u1eddi gian", labelFont);
            addHeaderCell(historyTable, "Vai tr\u00f2", labelFont);
            addHeaderCell(historyTable, "L\u1edbp ch\u1ee7 nhi\u1ec7m", labelFont);
            addHeaderCell(historyTable, "L\u1edbp b\u1ed9 m\u00f4n ph\u1ee5 tr\u00e1ch", labelFont);

            if (teacherInfo.getWorkHistory().isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Ch\u01b0a c\u00f3 l\u1ecbch s\u1eed c\u00f4ng t\u00e1c.", bodyFont));
                emptyCell.setColspan(4);
                emptyCell.setPadding(8f);
                historyTable.addCell(emptyCell);
            } else {
                for (WorkHistoryItem item : teacherInfo.getWorkHistory()) {
                    addBodyCell(historyTable, item.getSchoolYear(), bodyFont);
                    addBodyCell(historyTable, item.getRoleName(), bodyFont);
                    addBodyCell(historyTable, item.getHomeroomClasses(), bodyFont);
                    addBodyCell(historyTable, item.getSubjectClassNames(), bodyFont);
                }
            }

            document.add(historyTable);
            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new RuntimeException("Kh\u00f4ng th\u1ec3 xu\u1ea5t PDF gi\u00e1o vi\u00ean.");
        }
    }

    private void fillInfoSheet(XSSFWorkbook workbook, Sheet sheet, TeacherInfoView teacherInfo) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle bodyStyle = createBodyStyle(workbook);

        int rowIndex = 0;
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Thong tin giao vien");
        titleCell.setCellStyle(titleStyle);

        rowIndex++;
        rowIndex = writeInfoPair(sheet, rowIndex, "Ma giao vien", teacherInfo.getIdGiaoVien(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Ho va ten", teacherInfo.getHoTen(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Ngay sinh", valueOrDash(teacherInfo.getNgaySinh()), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Gioi tinh", teacherInfo.getGioiTinh(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "So dien thoai", teacherInfo.getSoDienThoai(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Email", teacherInfo.getEmail(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Dia chi", teacherInfo.getDiaChi(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Chuyen mon", teacherInfo.getChuyenMon(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Trinh do hoc van", teacherInfo.getTrinhDo(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Ngay bat dau cong tac", valueOrDash(teacherInfo.getNgayVaoLam()), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Vai tro hien tai", teacherInfo.getCurrentRole(), headerStyle, bodyStyle);
        rowIndex = writeInfoPair(sheet, rowIndex, "Nam hoc ap dung vai tro", teacherInfo.getCurrentRoleSchoolYear(), headerStyle, bodyStyle);
        writeInfoPair(sheet, rowIndex, "Ghi chu", teacherInfo.getGhiChu(), headerStyle, bodyStyle);

        sheet.setColumnWidth(0, 7200);
        sheet.setColumnWidth(1, 12500);
    }

    private void fillHistorySheet(XSSFWorkbook workbook, Sheet sheet, List<WorkHistoryItem> history) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle bodyStyle = createBodyStyle(workbook);
        CellStyle classChipStyle = createChipStyle(workbook);

        int rowIndex = 0;
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Lich su cong tac tai truong");
        titleCell.setCellStyle(titleStyle);

        rowIndex++;
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Khoang thoi gian", "Vai tro", "Lop chu nhiem", "Lop bo mon phu trach"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        if (history.isEmpty()) {
            Row emptyRow = sheet.createRow(rowIndex);
            Cell cell = emptyRow.createCell(0);
            cell.setCellValue("Chua co lich su cong tac.");
            cell.setCellStyle(bodyStyle);
            return;
        }

        for (WorkHistoryItem item : history) {
            Row row = sheet.createRow(rowIndex++);
            writeCell(row, 0, item.getSchoolYear(), bodyStyle);
            writeCell(row, 1, item.getRoleName(), bodyStyle);
            writeCell(row, 2, item.getHomeroomClasses(), classChipStyle);
            writeCell(row, 3, item.getSubjectClassNames(), classChipStyle);
        }

        sheet.setColumnWidth(0, 5200);
        sheet.setColumnWidth(1, 6500);
        sheet.setColumnWidth(2, 6500);
        sheet.setColumnWidth(3, 7600);
    }

    private int writeInfoPair(Sheet sheet,
                              int rowIndex,
                              String label,
                              String value,
                              CellStyle labelStyle,
                              CellStyle valueStyle) {
        Row row = sheet.createRow(rowIndex);
        writeCell(row, 0, label, labelStyle);
        writeCell(row, 1, value, valueStyle);
        return rowIndex + 1;
    }

    private void writeCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null || value.isBlank() ? "-" : value);
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
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
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createBodyStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createChipStyle(XSSFWorkbook workbook) {
        CellStyle style = createBodyStyle(workbook);
        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        font.setUnderline(FontUnderline.NONE);
        style.setFont(font);
        return style;
    }

    private Font createFont(float size, boolean bold) throws IOException, DocumentException {
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

        for (String fontPath : candidates) {
            if (Files.exists(Path.of(fontPath))) {
                return BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }

        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }

    private void addProfileSection(Document document,
                                   TeacherInfoView teacherInfo,
                                   Font nameFont,
                                   Font labelFont,
                                   Font bodyFont) throws DocumentException {
        PdfPTable profileTable = new PdfPTable(new float[]{2.2f, 7.8f});
        profileTable.setWidthPercentage(100);
        profileTable.setSpacingAfter(12f);

        PdfPCell avatarCell = new PdfPCell();
        avatarCell.setBorder(Rectangle.NO_BORDER);
        avatarCell.setPaddingRight(10f);
        avatarCell.setVerticalAlignment(PdfPCell.ALIGN_TOP);

        Image avatarImage = loadAvatarImage(teacherInfo.getAvatar());
        if (avatarImage != null) {
            avatarImage.scaleToFit(112f, 112f);
            avatarCell.addElement(avatarImage);
        } else {
            Paragraph noAvatar = new Paragraph("Kh\u00f4ng c\u00f3 \u1ea3nh", bodyFont);
            noAvatar.setSpacingBefore(42f);
            avatarCell.addElement(noAvatar);
        }
        profileTable.addCell(avatarCell);

        PdfPCell summaryCell = new PdfPCell();
        summaryCell.setBorder(Rectangle.NO_BORDER);
        summaryCell.setPadding(0f);

        Paragraph name = new Paragraph(valueOrDash(teacherInfo.getHoTen()), nameFont);
        name.setSpacingAfter(8f);
        summaryCell.addElement(name);

        PdfPTable summaryTable = new PdfPTable(new float[]{2.6f, 7.4f});
        summaryTable.setWidthPercentage(100);
        addSummaryRow(summaryTable, "M\u00e3 GV", teacherInfo.getIdGiaoVien(), labelFont, bodyFont);
        addSummaryRow(summaryTable, "Tr\u1ea1ng th\u00e1i", teacherInfo.getTrangThai(), labelFont, bodyFont);
        addSummaryRow(summaryTable, "Vai tr\u00f2 hi\u1ec7n t\u1ea1i", teacherInfo.getCurrentRole(), labelFont, bodyFont);
        addSummaryRow(summaryTable, "N\u0103m h\u1ecdc vai tr\u00f2", teacherInfo.getCurrentRoleSchoolYear(), labelFont, bodyFont);
        summaryCell.addElement(summaryTable);

        profileTable.addCell(summaryCell);
        document.add(profileTable);
    }

    private Image loadAvatarImage(String avatarPath) {
        if (avatarPath == null || avatarPath.isBlank()) {
            return null;
        }

        String normalized = avatarPath.trim().replace('\\', '/');
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("uploads/")) {
            normalized = normalized.substring("uploads/".length());
        }

        if (normalized.isBlank()) {
            return null;
        }

        Path avatarFile = uploadRoot.resolve(normalized).normalize();
        if (!avatarFile.startsWith(uploadRoot) || !Files.exists(avatarFile) || !Files.isRegularFile(avatarFile)) {
            return null;
        }

        try {
            return Image.getInstance(avatarFile.toAbsolutePath().toString());
        } catch (Exception ex) {
            return null;
        }
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font bodyFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(valueOrDash(value), bodyFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4f);
        table.addCell(valueCell);
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
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setBackgroundColor(new java.awt.Color(231, 236, 242));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valueOrDash(text), font));
        cell.setPadding(7f);
        cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        table.addCell(cell);
    }

    private String valueOrDash(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString().trim();
        return text.isEmpty() ? "-" : text;
    }
}
