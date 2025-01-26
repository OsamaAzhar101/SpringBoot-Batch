package com.javapulses.batch.processor;

import com.javapulses.batch.model.CustomerBalance;
import com.javapulses.batch.model.CustomerAccount;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class CustomerBalanceProcessor implements ItemProcessor<CustomerBalance, CustomerAccount> {

    private static final BigDecimal PROFIT_RATE = new BigDecimal("0.02"); // 2% profit rate

    @Override
    public CustomerAccount process(CustomerBalance customerBalance) throws Exception {
        if (customerBalance.getAccountBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid balance: " + customerBalance.getAccountBalance());
        }

        BigDecimal totalBalance = customerBalance.getAccountBalance();
        BigDecimal profit = totalBalance.multiply(PROFIT_RATE);

        return new CustomerAccount(customerBalance.getCustomerId(), profit);
    }
}
