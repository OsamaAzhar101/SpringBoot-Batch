package com.javapulses.batch.config;

import com.javapulses.batch.model.CorruptedRecord;
import com.javapulses.batch.model.Student;
import com.javapulses.batch.processor.CorruptedRecordProcessor;
import com.javapulses.batch.processor.StudentProcessor;
import com.javapulses.batch.repository.CorruptedRecordRepository;
import com.javapulses.batch.repository.StudentRepository;

import com.javapulses.batch.skipPolicy.CustomSkipListener;
import com.javapulses.batch.skipPolicy.CustomSkipPolicy;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    private final StudentRepository studentRepository;

    private final CorruptedRecordRepository corruptedRecordRepository;

    BatchConfig(StudentRepository studentRepository,
                JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                CorruptedRecordRepository corruptedRecordRepository) {

        this.studentRepository = studentRepository;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.corruptedRecordRepository = corruptedRecordRepository;
    }

    @Bean
    public FlatFileItemReader<Student> itemReader() {

        FlatFileItemReader<Student> flatFileItemReader = new FlatFileItemReader<>();

        flatFileItemReader.setResource(new ClassPathResource("students.csv"));
        flatFileItemReader.setName("CSV-Reader");
        flatFileItemReader.setLinesToSkip(1); // skip the first line as it contains headers.
        flatFileItemReader.setLineMapper(lineMapper());
        return flatFileItemReader;

    }

    @Bean
    public StudentProcessor itemProcessor() {
        return new StudentProcessor();

    }

    @Bean
    public CorruptedRecordProcessor corruptedRecordProcessor() {
        return new CorruptedRecordProcessor();

    }

    @Bean
    public RepositoryItemWriter<Student> itemWriter() {

        RepositoryItemWriter<Student> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(studentRepository);
        repositoryItemWriter.setMethodName("save");
        return repositoryItemWriter;
    }

    @Bean
    public ItemWriter<Student> itemCorruptedRecordWriter() {
        return items -> {
            for (Student student : items) {
                corruptedRecordRepository.deleteById(Long.valueOf(student.getId()));
                studentRepository.save(student);
            }
        };
    }


    @Bean
    public SkipPolicy skipPolicy() {
        return new CustomSkipPolicy();
    }

    @Bean
    public Step step() {
        return new StepBuilder("ETL-File-Load", jobRepository).
                <Student, Student>chunk(10, platformTransactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .faultTolerant()
                .skipPolicy(skipPolicy())
                .listener(new CustomSkipListener()) // Register the CustomSkipListener
                .build();


    }

    @Bean
    public Step processCorruptedRecordsStep() {
        return new StepBuilder("processCorruptedRecordsStep", jobRepository)
                .<CorruptedRecord, Student>chunk(10, platformTransactionManager)
                .reader(corruptedRecordReader()) // Reads from corrupted_record table
                .processor(corruptedRecordProcessor())      // Reprocess records
                .writer(itemCorruptedRecordWriter())            // Writes corrected data to the original table
                .faultTolerant()
                .skipPolicy(skipPolicy())        // Skips again if reprocessing fails
                .build();
    }

    @Bean
    public RepositoryItemReader<CorruptedRecord> corruptedRecordReader() {
        return new RepositoryItemReaderBuilder<CorruptedRecord>()
                .name("corruptedRecordReader")
                .repository(corruptedRecordRepository)
                .methodName("findAll") // Ensure this matches the repository method
                .pageSize(10)          // Batch size for each query
                .arguments()           // Any arguments for query parameters
                .build();
    }

    @Bean
    public Job job() {
        return new JobBuilder("ETL-Load", jobRepository)
                .start(step())
                .next(processCorruptedRecordsStep())
                .build();
    }

    private LineMapper<Student> lineMapper() {

        DefaultLineMapper<Student> defaultLineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer(); // DelimitedLineTokenizer is used to split the line into tokens.
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(new String[]{"id", "firstName", "lastName", "age"});

        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }

}

