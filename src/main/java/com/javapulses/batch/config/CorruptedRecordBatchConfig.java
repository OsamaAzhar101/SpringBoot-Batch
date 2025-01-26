package com.javapulses.batch.config;

import com.javapulses.batch.model.CorruptedRecord;
import com.javapulses.batch.processor.CorruptedRecordProcessor;
import com.javapulses.batch.repository.CorruptedRecordRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

@Configuration
@EnableBatchProcessing
public class CorruptedRecordBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final CorruptedRecordRepository corruptedRecordRepository;

    CorruptedRecordBatchConfig(CorruptedRecordRepository corruptedRecordRepository,
                               JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        this.corruptedRecordRepository = corruptedRecordRepository;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public RepositoryItemReader<CorruptedRecord> corruptedRecordReader() {
        RepositoryItemReader<CorruptedRecord> reader = new RepositoryItemReader<>();
        reader.setRepository(corruptedRecordRepository);
        reader.setMethodName("findAll");
        reader.setSort(Collections.singletonMap("id", Sort.Direction.ASC));
        return reader;
    }

    @Bean
    public CorruptedRecordProcessor corruptedRecordProcessor() {
        return new CorruptedRecordProcessor();
    }

    @Bean
    public RepositoryItemWriter<CorruptedRecord> corruptedRecordWriter() {
        RepositoryItemWriter<CorruptedRecord> writer = new RepositoryItemWriter<>();
        writer.setRepository(corruptedRecordRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step corruptedRecordStep() {
        return new StepBuilder("CorruptedRecordStep", jobRepository)
                .<CorruptedRecord, CorruptedRecord>chunk(10, platformTransactionManager)
                .reader(corruptedRecordReader())
                .processor(corruptedRecordProcessor())
                .writer(corruptedRecordWriter())
                .build();
    }

    @Bean
    public Job corruptedRecordJob() {
        return new JobBuilder("CorruptedRecordJob", jobRepository)
                .start(corruptedRecordStep())
                .build();
    }
}