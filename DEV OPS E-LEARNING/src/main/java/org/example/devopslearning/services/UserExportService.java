package org.example.devopslearning.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.devopslearning.entities.User;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserExportService {

    /**
     * ✅ Exporte les utilisateurs en fichier Excel (.xlsx)
     */
    public byte[] exportUsersToExcel(List<User> users) throws IOException {
        // Créer un workbook Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Utilisateurs");

        // ✅ STYLES
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(false);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // ✅ LIGNE D'EN-TÊTE
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Prénom", "Nom", "Email", "Rôle(s)", "Date d'inscription"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // ✅ LIGNES DE DONNÉES
        int rowNum = 1;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (User user : users) {
            Row row = sheet.createRow(rowNum++);

            // ID
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(user.getId());
            cell0.setCellStyle(dataStyle);

            // Prénom
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(user.getFirstName());
            cell1.setCellStyle(dataStyle);

            // Nom
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(user.getLastName());
            cell2.setCellStyle(dataStyle);

            // Email
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(user.getEmail());
            cell3.setCellStyle(dataStyle);

            // Rôle(s)
            String roles = user.getUserRoles().stream()
                    .map(ur -> {
                        String roleName = ur.getRole().getName();
                        if (roleName.equals("ROLE_ADMIN")) return "Admin";
                        if (roleName.equals("ROLE_TEACHER")) return "Enseignant";
                        if (roleName.equals("ROLE_STUDENT")) return "Étudiant";
                        return roleName;
                    })
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(roles);
            cell4.setCellStyle(dataStyle);

            // Date d'inscription
            String createdAt = user.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(dateFormatter);

            Cell cell5 = row.createCell(5);
            cell5.setCellValue(createdAt);
            cell5.setCellStyle(dataStyle);
        }

        // ✅ AUTO-SIZE des colonnes
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
            // Ajouter un peu d'espace
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        // ✅ CONVERTIR EN BYTE ARRAY
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}