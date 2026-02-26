package org.example.devopslearning.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.NotesCour;
import org.example.devopslearning.repositories.NotesCourRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeExportService {

    private final NotesCourRepository notesCoursRepository;

    /**
     * Exporte les notes d'un cours en PDF
     */
    public ByteArrayOutputStream exportCourseToPDF(Cours cours) throws DocumentException {
        List<NotesCour> notes = notesCoursRepository.findByCoursId(cours.getId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Titre
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Relevé de Notes - " + cours.getTitle(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Info du cours
        com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.GRAY);
        Paragraph info = new Paragraph("Code: " + cours.getCode(), infoFont);
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(20);
        document.add(info);

        // Tableau des notes
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // En-têtes
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        addTableHeader(table, "Étudiant", headerFont);
        addTableHeader(table, "Email", headerFont);
        addTableHeader(table, "Note finale", headerFont);
        addTableHeader(table, "Mention", headerFont);
        addTableHeader(table, "Statut", headerFont);

        // Données
        com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL);
        for (NotesCour note : notes) {
            addTableCell(table, note.getEtudiant().getFirstName() + " " + note.getEtudiant().getLastName(), dataFont);
            addTableCell(table, note.getEtudiant().getEmail(), dataFont);
            addTableCell(table, note.getNoteFinale() != null ? note.getNoteFinale() + "/20" : "N/A", dataFont);
            addTableCell(table, note.getMention() != null ? note.getMention() : "-", dataFont);
            addTableCell(table, note.getStatut() != null ? note.getStatut() : "-", dataFont);
        }

        document.add(table);

        // Statistiques
        document.add(new Paragraph("\n"));
        com.itextpdf.text.Font statsFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);

        BigDecimal moyenne = calculateAverage(notes);
        long reussites = notes.stream()
                .filter(n -> n.getNoteFinale() != null && n.getNoteFinale().compareTo(BigDecimal.valueOf(10)) >= 0)
                .count();

        Paragraph stats = new Paragraph("Statistiques", statsFont);
        stats.setSpacingBefore(10);
        document.add(stats);

        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 9);
        document.add(new Paragraph("Nombre d'étudiants: " + notes.size(), normalFont));
        document.add(new Paragraph("Moyenne générale: " + (moyenne != null ? moyenne : "N/A") + "/20", normalFont));
        document.add(new Paragraph("Taux de réussite: " + reussites + "/" + notes.size(), normalFont));

        document.close();

        return outputStream;
    }

    /**
     * Exporte les notes d'un cours en Excel
     */
    public ByteArrayOutputStream exportCourseToExcel(Cours cours) throws IOException {
        List<NotesCour> notes = notesCoursRepository.findByCoursId(cours.getId());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Notes - " + cours.getCode());

        // Style pour les en-têtes
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Titre
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Relevé de Notes - " + cours.getTitle());
        CellStyle titleStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        // Info cours
        Row infoRow = sheet.createRow(1);
        infoRow.createCell(0).setCellValue("Code: " + cours.getCode());

        // En-têtes du tableau
        Row headerRow = sheet.createRow(3);
        String[] headers = {"Nom", "Prénom", "Email", "Note finale", "Note max", "Mention", "Statut", "Date calcul"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Données
        int rowNum = 4;
        for (NotesCour note : notes) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(note.getEtudiant().getLastName());
            row.createCell(1).setCellValue(note.getEtudiant().getFirstName());
            row.createCell(2).setCellValue(note.getEtudiant().getEmail());

            if (note.getNoteFinale() != null) {
                row.createCell(3).setCellValue(note.getNoteFinale().doubleValue());
            } else {
                row.createCell(3).setCellValue("N/A");
            }

            row.createCell(4).setCellValue(note.getNoteMax() != null ? note.getNoteMax().doubleValue() : 20.0);
            row.createCell(5).setCellValue(note.getMention() != null ? note.getMention() : "-");
            row.createCell(6).setCellValue(note.getStatut() != null ? note.getStatut() : "-");

            if (note.getDateCalcul() != null) {
                String date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(note.getDateCalcul());
                row.createCell(7).setCellValue(date);
            } else {
                row.createCell(7).setCellValue("-");
            }
        }

        // Statistiques
        rowNum += 2;
        Row statsRow = sheet.createRow(rowNum++);
        Cell statsCell = statsRow.createCell(0);
        statsCell.setCellValue("STATISTIQUES");
        statsCell.setCellStyle(headerStyle);

        BigDecimal moyenne = calculateAverage(notes);
        long reussites = notes.stream()
                .filter(n -> n.getNoteFinale() != null && n.getNoteFinale().compareTo(BigDecimal.valueOf(10)) >= 0)
                .count();

        sheet.createRow(rowNum++).createCell(0).setCellValue("Nombre d'étudiants: " + notes.size());
        sheet.createRow(rowNum++).createCell(0).setCellValue("Moyenne générale: " + (moyenne != null ? moyenne + "/20" : "N/A"));
        sheet.createRow(rowNum++).createCell(0).setCellValue("Taux de réussite: " + reussites + "/" + notes.size());

        // Auto-dimensionner les colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    /**
     * Calcule la moyenne d'un cours
     */
    private BigDecimal calculateAverage(List<NotesCour> notes) {
        List<BigDecimal> validNotes = notes.stream()
                .filter(n -> n.getNoteFinale() != null)
                .map(NotesCour::getNoteFinale)
                .toList();

        if (validNotes.isEmpty()) {
            return null;
        }

        BigDecimal sum = validNotes.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(validNotes.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Ajoute une cellule d'en-tête au tableau PDF
     */
    private void addTableHeader(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    /**
     * Ajoute une cellule de données au tableau PDF
     */
    private void addTableCell(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
}