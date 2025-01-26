package com.javapulses.batch.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.batch.core.explore.JobExplorer;

@RestController
@RequestMapping("/jobs")
public class RewardJobController {

    private final JobLauncher jobLauncher;
    private final Job profitCalculationJob;

    private final JobExplorer jobExplorer;

    @Autowired
    public RewardJobController(JobLauncher jobLauncher, Job profitCalculationJob, JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.profitCalculationJob = profitCalculationJob;
        this.jobExplorer = jobExplorer;
    }

    @PostMapping("/run")
    public ResponseEntity<String> runJob(@RequestParam(required = false) String month) {
        try {
            // Build JobParameters to pass the month if provided
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("month", month != null ? month : "currentMonth") // Default to "currentMonth" if not provided
                    .addLong("timestamp", System.currentTimeMillis()) // Ensure job uniqueness
                    .toJobParameters();

            // Launch the job
            jobLauncher.run(profitCalculationJob, jobParameters);
            return ResponseEntity.ok("Job has been successfully started.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error starting the job: " + e.getMessage());
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
                jobLauncher.run(profitCalculationJob, jobParameters);
                return ResponseEntity.ok("Job has been successfully restarted.");
            } else {
                return ResponseEntity.badRequest().body("Job is not in a restartable state. Status: " + batchStatus);
            }
        } catch (JobRestartException | JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            return ResponseEntity.status(500).body("Error restarting the job: " + e.getMessage());
        }

    }

}
