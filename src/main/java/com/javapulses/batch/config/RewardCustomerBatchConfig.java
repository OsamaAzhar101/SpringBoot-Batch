package com.javapulses.batch.config;

import com.javapulses.batch.listener.CustomRewardChunkListener;
import com.javapulses.batch.listener.CustomRewardSkipListener;
import com.javapulses.batch.model.CustomerAccount;
import com.javapulses.batch.model.CustomerBalance;
import com.javapulses.batch.processor.CustomerBalanceProcessor;
import com.javapulses.batch.repository.CustomerAccountRepository;
import com.javapulses.batch.repository.CustomerBalanceRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class RewardCustomerBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CustomerBalanceRepository customerBalanceRepository;
    private final CustomerAccountRepository customerAccountRepository;

    public RewardCustomerBatchConfig(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     CustomerBalanceRepository customerBalanceRepository,
                                     CustomerAccountRepository customerAccountRepository) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.customerBalanceRepository = customerBalanceRepository;
        this.customerAccountRepository = customerAccountRepository;
    }

    @Bean
    public RepositoryItemReader<CustomerBalance> rewardCustomerItemReader() {

        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC); // Sort by 'id' in ascending order


        return new RepositoryItemReaderBuilder<CustomerBalance>()
                .name("customerBalanceReader")
                .repository(customerBalanceRepository)
                .methodName("findAllByMonth")
                .arguments(new HashMap<>()) // Pass arguments for filtering (e.g., month/year)
                .pageSize(10)
                .sorts(sorts)
                .build();
    }

    @Bean
    public CustomerBalanceProcessor rewaCustomerBalanceProcessor() {
        return new CustomerBalanceProcessor();
    }

    @Bean
    public RepositoryItemWriter<CustomerAccount> rewaCustomerAccountRepositoryItemWriter() {
        RepositoryItemWriter<CustomerAccount> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerAccountRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step calculateProfitStep() {
        return new StepBuilder("calculateProfitStep", jobRepository)
                .<CustomerBalance, CustomerAccount>chunk(10, transactionManager)
                .reader(rewardCustomerItemReader())
                .processor(rewaCustomerBalanceProcessor())
                .writer(rewaCustomerAccountRepositoryItemWriter())
                .faultTolerant()
                .skipPolicy((t, skipCount) -> skipCount <= 5) // Skip up to 5 records
                .retry(Exception.class) // Retry mechanism for transient errors
                .retryLimit(3)
                .listener(new CustomRewardChunkListener()) // Log chunk details
                .listener(new CustomRewardSkipListener())  // Log skipped records
                .build();
    }

    @Bean
    public Job profitCalculationJob() {
        return new JobBuilder("profitCalculationJob", jobRepository)
                .start(calculateProfitStep())
                .incrementer(new RunIdIncrementer()) // Allow job restarts
                .build();
    }
}
