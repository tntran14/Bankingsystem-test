package com.bankingsystem.service;

import com.bankingsystem.model.Transaction;
import com.bankingsystem.repository.TransactionRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service xuất báo cáo tài chính ra Excel và PDF
 */
@Service
public class ReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Xuất báo cáo giao dịch ra Excel.
     */
    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<byte[]> exportTransactionsToExcel(LocalDateTime from, LocalDateTime to)
            throws IOException {
        List<Transaction> transactions = transactionRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions Report");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "Type", "Amount", "Fee", "From Account", "To Account",
                "Location", "Date", "Description" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Transaction tx : transactions) {
            if (from != null && tx.getTransactionDate().isBefore(from))
                continue;
            if (to != null && tx.getTransactionDate().isAfter(to))
                continue;

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(tx.getId());
            row.createCell(1).setCellValue(tx.getType().name());
            row.createCell(2).setCellValue(tx.getAmount().doubleValue());
            row.createCell(3).setCellValue(tx.getTransactionFee().doubleValue());
            row.createCell(4)
                    .setCellValue(tx.getFromAccount() != null ? tx.getFromAccount().getAccountNumber() : "N/A");
            row.createCell(5).setCellValue(tx.getToAccount() != null ? tx.getToAccount().getAccountNumber() : "N/A");
            row.createCell(6).setCellValue(tx.getLocation() != null ? tx.getLocation() : "N/A");
            row.createCell(7).setCellValue(tx.getTransactionDate().format(DATE_FMT));
            row.createCell(8).setCellValue(tx.getDescription() != null ? tx.getDescription() : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return CompletableFuture.completedFuture(out.toByteArray());
    }

    /**
     * Xuất báo cáo giao dịch ra PDF.
     */
    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<byte[]> exportTransactionsToPdf(LocalDateTime from, LocalDateTime to) {
        List<Transaction> transactions = transactionRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);
        document.open();

        // Title
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Banking System - Transaction Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Subtitle with date range
        Font subFont = new Font(Font.HELVETICA, 10, Font.ITALIC);
        String dateRange = "Report generated: " + LocalDateTime.now().format(DATE_FMT);
        if (from != null && to != null) {
            dateRange += " | Period: " + from.format(DATE_FMT) + " to " + to.format(DATE_FMT);
        }
        Paragraph subtitle = new Paragraph(dateRange, subFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(15);
        document.add(subtitle);

        // Table
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);

        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        String[] headers = { "ID", "Type", "Amount", "Fee", "From", "To", "Date" };
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(66, 133, 244));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.HELVETICA, 8);
        for (Transaction tx : transactions) {
            if (from != null && tx.getTransactionDate().isBefore(from))
                continue;
            if (to != null && tx.getTransactionDate().isAfter(to))
                continue;

            table.addCell(new Phrase(String.valueOf(tx.getId()), dataFont));
            table.addCell(new Phrase(tx.getType().name(), dataFont));
            table.addCell(new Phrase(tx.getAmount().toPlainString(), dataFont));
            table.addCell(new Phrase(tx.getTransactionFee().toPlainString(), dataFont));
            table.addCell(
                    new Phrase(tx.getFromAccount() != null ? tx.getFromAccount().getAccountNumber() : "N/A", dataFont));
            table.addCell(
                    new Phrase(tx.getToAccount() != null ? tx.getToAccount().getAccountNumber() : "N/A", dataFont));
            table.addCell(new Phrase(tx.getTransactionDate().format(DATE_FMT), dataFont));
        }

        document.add(table);
        document.close();

        return CompletableFuture.completedFuture(out.toByteArray());
    }
}
