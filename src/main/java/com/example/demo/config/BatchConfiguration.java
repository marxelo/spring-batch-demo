package com.example.demo.config;

import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.steps.chunklets.TransactionItemProcessor;
import com.example.demo.steps.chunklets.TransactionItemReader;
import com.example.demo.steps.chunklets.TransactionItemWriter;
import com.example.demo.steps.tasklets.FileDownloadTasklet;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableTask
@EnableBatchProcessing
public class BatchConfiguration {

  private static final int CHUNK_SIZE = 2500;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

  @Autowired
  private TransactionRepository transactionRepository;

  @Bean
  public FileDownloadTasklet fileDownloadTasklet() {
    return new FileDownloadTasklet();
  }

  @Bean
  public Step taskletStep() {
    return stepBuilderFactory.get("downloadFileStep").tasklet(fileDownloadTasklet()).build();
  }

  public Step chunkletStep() {
    return stepBuilderFactory.get("transactionProcessFileStep")
        .<Transaction, Transaction> chunk(CHUNK_SIZE)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .listener(new ChunkletStepExecutionListener())
        .build();
  }

  @Bean
  public ItemStreamReader<Transaction> reader() {
    return new TransactionItemReader();
  }

  private TransactionItemProcessor processor() {
    return new TransactionItemProcessor();
  }

  private TransactionItemWriter writer() {
    return new TransactionItemWriter(transactionRepository);
  }

  @Bean
  public Job souJavaJob(@Autowired JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("souJavaJob")
        .incrementer(new RunIdIncrementer())
        .start(taskletStep())
        .next(chunkletStep())
        .listener(new SouJavaJobExecutionListener())
        .build();
  }

}