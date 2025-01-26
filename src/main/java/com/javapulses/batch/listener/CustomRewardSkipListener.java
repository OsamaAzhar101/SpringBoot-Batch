package com.javapulses.batch.listener;

import com.javapulses.batch.model.CustomerAccount;
import com.javapulses.batch.model.CustomerBalance;
import org.springframework.batch.core.listener.SkipListenerSupport;

public class CustomRewardSkipListener extends SkipListenerSupport<CustomerBalance, CustomerAccount> {
    @Override
    public void onSkipInProcess(CustomerBalance item, Throwable t) {
        System.out.println("Skipped record for customer ID: " + item.getCustomerId() + " due to: " + t.getMessage());
    }
}
