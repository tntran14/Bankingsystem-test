package com.bankingsystem.dto.request;

import com.bankingsystem.model.enums.TransactionType;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TransactionRequest {
    private Long fromAccountId;
    private Long toAccountId;
    @NotNull @Positive
    private BigDecimal amount;
    @NotNull
    private TransactionType type;
    private String location;
    private String description;
}
