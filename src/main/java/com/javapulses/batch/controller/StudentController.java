package com.javapulses.batch.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job job;

    StudentController(JobLauncher jobLauncher, Job job, JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.jobExplorer = jobExplorer;
    }

    @PostMapping("/load")
    public void load() {
        try {

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @PostMapping("/restart")
    public ResponseEntity<String> restartJob(@RequestParam String jobName) {
        try {
            // Find the last job execution
            JobInstance lastJobInstance = jobExplorer.getLastJobInstance(jobName);
            if (lastJobInstance == null) {
                return ResponseEntity.badRequest().body("No job instance found with name: " + jobName);
            }

            JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);
            if (lastJobExecution == null) {
                return ResponseEntity.badRequest().body("No job execution found for job instance: " + jobName);
            }

            BatchStatus batchStatus = lastJobExecution.getStatus();
            if (batchStatus == BatchStatus.FAILED || batchStatus == BatchStatus.STOPPED) {
                // Restart the job
                JobParameters jobParameters = lastJobExecution.getJobParameters();
                jobLauncher.run(job, jobParameters);
                return ResponseEntity.ok("Job has been successfully restarted.");
            } else {
                return ResponseEntity.badRequest().body("Job is not in a restartable state. Status: " + batchStatus);
            }
        } catch (JobRestartException | JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            return ResponseEntity.status(500).body("Error restarting the job: " + e.getMessage());
        }

    }

    @PostMapping("/restartFailedRecords")
    public ResponseEntity<String> restartFailedRecords() {
        try {
            // Trigger only the step for processing failed records
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // Unique ID for restart
                    .toJobParameters();

            jobLauncher.run(job, jobParameters); // This will trigger the `processCorruptedRecordsStep`
            return ResponseEntity.ok("Job restarted to process only failed records.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error restarting the job: " + e.getMessage());
        }
    }

}