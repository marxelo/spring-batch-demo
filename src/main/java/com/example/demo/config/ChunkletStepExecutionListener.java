package com.example.demo.config;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class ChunkletStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Called beforeStep().");
        JobParameters parameters = stepExecution.getJobExecution().getJobParameters();
        System.out.println(parameters.toString());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("Called afterStep().");
        System.out.println("Step Summary: " + stepExecution.getSummary());
        return ExitStatus.COMPLETED;
    }
}