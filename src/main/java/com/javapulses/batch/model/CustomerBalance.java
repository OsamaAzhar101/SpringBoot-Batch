package com.javapulses.batch.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customer_balance")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    @Column(name = "account_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal accountBalance;

    private String month;

}
