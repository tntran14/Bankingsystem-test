package com.bankingsystem.model;

import com.bankingsystem.model.enums.CustomerCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customer_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CustomerType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CustomerCategory category;

    @Column(nullable = false)
    private String description;

    /** Hạn mức giao dịch tối đa cho loại khách hàng này */
    @Column(nullable = false)
    private BigDecimal maxTransactionLimit;
}
