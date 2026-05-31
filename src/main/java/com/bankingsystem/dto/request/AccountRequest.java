package com.bankingsystem.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AccountRequest {
    @NotNull
    private Long customerId;
    @NotNull
    private BigDecimal transactionLimit;
    private BigDecimal initialBalance;
}
