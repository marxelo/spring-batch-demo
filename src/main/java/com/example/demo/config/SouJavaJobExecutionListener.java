package com.example.demo.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
 
public class SouJavaJobExecutionListener implements JobExecutionListener {
 
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("Called beforeJob().");
    }
 
    public void afterJob(JobExecution jobExecution) {
        System.out.println("Called afterJob().");
        System.out.println("Job execution info:" + jobExecution.toString());
    }
}