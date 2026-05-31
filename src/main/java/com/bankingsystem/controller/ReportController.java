package com.bankingsystem.controller;

import com.bankingsystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Controller xuất báo cáo tài chính
 * Chỉ Admin mới có quyền truy cập.
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

        @Autowired
        private ReportService reportService;

        /**
         * Xuất báo cáo giao dịch ra Excel.
         */
        @GetMapping("/transactions/excel")
        public CompletableFuture<ResponseEntity<byte[]>> exportExcel(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
                        throws Exception {

                return reportService.exportTransactionsToExcel(from, to)
                                .thenApply(bytes -> ResponseEntity.ok()
                                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                                "attachment; filename=transactions_report.xlsx")
                                                .contentType(MediaType.parseMediaType(
                                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                                .body(bytes));
        }

        /**
         * Xuất báo cáo giao dịch ra PDF.
         */
        @GetMapping("/transactions/pdf")
        public CompletableFuture<ResponseEntity<byte[]>> exportPdf(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

                return reportService.exportTransactionsToPdf(from, to)
                                .thenApply(bytes -> ResponseEntity.ok()
                                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                                "attachment; filename=transactions_report.pdf")
                                                .contentType(MediaType.APPLICATION_PDF)
                                                .body(bytes));
        }
}
