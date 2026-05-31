package com.bankingsystem.dto.request;

import com.bankingsystem.model.enums.TransactionType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho tìm kiếm giao dịch nâng cao với nhiều bộ lọc.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TransactionSearchRequest {
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private TransactionType type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    private String location;
    private Long accountId;
}
