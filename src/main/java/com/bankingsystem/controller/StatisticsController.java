package com.bankingsystem.controller;

import com.bankingsystem.dto.response.AccountStatisticsResponse;
import com.bankingsystem.dto.response.CustomerLocationResponse;
import com.bankingsystem.dto.response.TransactionReportResponse;
import com.bankingsystem.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller thống kê
 * Chỉ Admin mới có quyền truy cập.
 */
@RestController
@RequestMapping("/api/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Thống kê số lượng tài khoản phân loại theo số dư (cao/trung bình/thấp)
     * và số lượng giao dịch theo loại.
     */
    @GetMapping("/accounts")
    public ResponseEntity<AccountStatisticsResponse> getAccountStatistics() {
        return ResponseEntity.ok(statisticsService.getAccountStatistics());
    }

    /**
     * Thống kê khách hàng theo địa điểm.
     */
    @GetMapping("/customers/location")
    public ResponseEntity<List<CustomerLocationResponse>> getCustomersByLocation() {
        return ResponseEntity.ok(statisticsService.getCustomersByLocation());
    }

    /**
     * Báo cáo giao dịch theo tuần (trung bình, lớn nhất, nhỏ nhất).
     */
    @GetMapping("/transactions/weekly")
    public ResponseEntity<TransactionReportResponse> getWeeklyReport() {
        return ResponseEntity.ok(statisticsService.getWeeklyReport());
    }

    /**
     * Báo cáo giao dịch theo quý.
     */
    @GetMapping("/transactions/quarterly")
    public ResponseEntity<TransactionReportResponse> getQuarterlyReport() {
        return ResponseEntity.ok(statisticsService.getQuarterlyReport());
    }

    /**
     * Báo cáo giao dịch theo năm.
     */
    @GetMapping("/transactions/yearly")
    public ResponseEntity<TransactionReportResponse> getYearlyReport() {
        return ResponseEntity.ok(statisticsService.getYearlyReport());
    }
}
