package com.bankingsystem.service;

import com.bankingsystem.dto.response.AccountStatisticsResponse;
import com.bankingsystem.dto.response.CustomerLocationResponse;
import com.bankingsystem.dto.response.TransactionReportResponse;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.CustomerRepository;
import com.bankingsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service thống kê và báo cáo
 */
@Service
public class StatisticsService {

    private static final BigDecimal HIGH_BALANCE_THRESHOLD = new BigDecimal("100000000"); // 100 triệu
    private static final BigDecimal MEDIUM_BALANCE_THRESHOLD = new BigDecimal("10000000"); // 10 triệu

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Thống kê tài khoản phân loại theo số dư (cao/trung bình/thấp) + giao dịch
     * theo loại.
     */
    @Cacheable(value = "statistics", key = "'accountStats'")
    public AccountStatisticsResponse getAccountStatistics() {
        long total = accountRepository.count();
        long high = accountRepository.countByBalanceGreaterThanOrEqual(HIGH_BALANCE_THRESHOLD);
        long medium = accountRepository.countByBalanceBetween(MEDIUM_BALANCE_THRESHOLD, HIGH_BALANCE_THRESHOLD);
        long low = accountRepository.countByBalanceLessThan(MEDIUM_BALANCE_THRESHOLD);

        // Đếm giao dịch theo loại
        Map<String, Long> txByType = new HashMap<>();
        List<Object[]> typeCounts = transactionRepository.countTransactionsByType();
        long totalTx = 0;
        for (Object[] row : typeCounts) {
            String typeName = row[0].toString();
            Long count = (Long) row[1];
            txByType.put(typeName, count);
            totalTx += count;
        }

        return AccountStatisticsResponse.builder()
                .totalAccounts(total)
                .highBalanceAccounts(high)
                .mediumBalanceAccounts(medium)
                .lowBalanceAccounts(low)
                .totalTransactions(totalTx)
                .transactionsByType(txByType)
                .build();
    }

    /**
     * Thống kê khách hàng theo địa điểm.
     */
    public List<CustomerLocationResponse> getCustomersByLocation() {
        List<Object[]> results = customerRepository.countCustomersByLocation();
        List<CustomerLocationResponse> responses = new ArrayList<>();
        for (Object[] row : results) {
            responses.add(CustomerLocationResponse.builder()
                    .address((String) row[0])
                    .customerCount((Long) row[1])
                    .build());
        }
        return responses;
    }

    /**
     * Báo cáo giao dịch theo tuần hiện tại.
     */
    public TransactionReportResponse getWeeklyReport() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0);
        return buildReport("Weekly", startOfWeek, now);
    }

    /**
     * Báo cáo giao dịch theo quý hiện tại.
     */
    public TransactionReportResponse getQuarterlyReport() {
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
        LocalDateTime startOfQuarter = now.withMonth(quarterStartMonth).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        return buildReport("Quarterly", startOfQuarter, now);
    }

    /**
     * Báo cáo giao dịch theo năm hiện tại.
     */
    public TransactionReportResponse getYearlyReport() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfYear = now.withMonth(1).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        return buildReport("Yearly", startOfYear, now);
    }

    private TransactionReportResponse buildReport(String period, LocalDateTime from, LocalDateTime to) {
        List<Object[]> results = transactionRepository.getTransactionReport(from, to);

        if (results.isEmpty() || results.get(0)[0] == null) {
            return TransactionReportResponse.builder()
                    .period(period)
                    .totalTransactions(0)
                    .totalAmount(BigDecimal.ZERO)
                    .averageAmount(BigDecimal.ZERO)
                    .maxAmount(BigDecimal.ZERO)
                    .minAmount(BigDecimal.ZERO)
                    .totalFees(BigDecimal.ZERO)
                    .build();
        }

        Object[] row = results.get(0);
        return TransactionReportResponse.builder()
                .period(period)
                .totalTransactions((Long) row[0])
                .totalAmount(toBigDecimal(row[1]))
                .averageAmount(toBigDecimal(row[2]))
                .maxAmount(toBigDecimal(row[3]))
                .minAmount(toBigDecimal(row[4]))
                .totalFees(toBigDecimal(row[5]))
                .build();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null)
            return BigDecimal.ZERO;
        if (value instanceof BigDecimal)
            return (BigDecimal) value;
        return new BigDecimal(value.toString());
    }
}
