package com.bankingsystem.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * Response cho báo cáo giao dịch theo khoảng thời gian (tuần/quý/năm).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionReportResponse {
    private String period;
    private long totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;
    private BigDecimal totalFees;
}
