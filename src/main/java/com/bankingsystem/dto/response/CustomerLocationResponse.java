package com.bankingsystem.dto.response;

import lombok.*;

/**
 * Response cho thống kê khách hàng theo địa điểm.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerLocationResponse {
    private String address;
    private long customerCount;
}
