package com.bankingsystem.dto.response;

import lombok.*;

import java.util.Map;

/**
 * Response cho thống kê tài khoản: phân loại theo số dư (cao/trung bình/thấp),
 * số lượng giao dịch.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountStatisticsResponse {
    private long totalAccounts;
    private long highBalanceAccounts;
    private long mediumBalanceAccounts;
    private long lowBalanceAccounts;
    private long totalTransactions;
    private Map<String, Long> transactionsByType;
}
