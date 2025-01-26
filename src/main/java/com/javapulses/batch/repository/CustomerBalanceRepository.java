package com.javapulses.batch.repository;

import com.javapulses.batch.model.CustomerBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerBalanceRepository extends JpaRepository<CustomerBalance, Long> {
    List<CustomerBalance> findAllByMonth(String month); // Replace with proper query
}
