package com.bankingsystem.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScheduledTransactionRequest {
    private Long fromAccountId;
    private Long toAccountId;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private String cronExpression;
    private String description;
    @NotNull
    private LocalDateTime nextExecution;
}
