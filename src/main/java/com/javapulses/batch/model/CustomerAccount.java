package com.javapulses.batch.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_account")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "profit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal profitAmount;

    public CustomerAccount(Long customerId, BigDecimal profit) {
        this.customerId = customerId;
        this.profitAmount = profit;
    }
}
