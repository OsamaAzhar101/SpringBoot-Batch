package com.javapulses.batch.repository;

import com.javapulses.batch.model.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {
}
