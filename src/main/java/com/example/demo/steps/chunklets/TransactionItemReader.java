package com.example.demo.steps.chunklets;

import java.util.Objects;

import com.example.demo.models.RegTypeOne;
import com.example.demo.models.RegTypeThree;
import com.example.demo.models.RegTypeTwo;
import com.example.demo.models.Transaction;
import com.example.demo.steps.mappers.RegTypeOneFieldSetMapper;
import com.example.demo.steps.mappers.RegTypeThreeFieldSetMapper;
import com.example.demo.steps.mappers.RegTypeTwoFieldSetMapper;
import com.example.demo.steps.mappers.TransactionFieldSetMapper;
import com.example.demo.steps.tokenizers.TransactionCompositeLineTokenizer;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionItemReader implements ItemStreamReader<Transaction> {

    private SingleItemPeekableItemReader<FieldSet> delegate;
    private static final String HEADER_ID = "0000";

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        String baseDir = stepExecution.getJobParameters().getString("baseDir");
        String fileName = stepExecution.getJobParameters().getString("bafileNameseDir");

        if (Objects.isNull(baseDir)) {
            baseDir = "/input/";
        }

        if (Objects.isNull(fileName)) {
            fileName = "exemplo-sou-java.txt";
        }

        FlatFileItemReader<FieldSet> reader = new FlatFileItemReader<>();

        reader.setResource(new ClassPathResource(baseDir + fileName));
        final DefaultLineMapper<FieldSet> defaultLineMapper = new DefaultLineMapper<>();
        defaultLineMapper.setLineTokenizer(compositeLineTokenizer());
        defaultLineMapper.setFieldSetMapper(new PassThroughFieldSetMapper());
        reader.setLineMapper(defaultLineMapper);
        delegate = new SingleItemPeekableItemReader<>();
        delegate.setDelegate(reader);
    }

    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }

    @Override
    public void open(ExecutionContext ec) throws ItemStreamException {
        delegate.open(ec);
    }

    @Override
    public Transaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        log.info("Lendo os primeiros campos do Registro");

        Transaction record = null;

        FieldSet fieldSet;

        loop: while ((fieldSet = delegate.read()) != null) {
            String lineId = fieldSet.readString(0) + fieldSet.readString(1);
            switch (lineId) {
                case Transaction.LINE_ID:
                    log.info("início de um registro de transação");
                    record = new Transaction();
                    record = transactionMapper().mapFieldSet(fieldSet);
                    break;
                case RegTypeOne.LINE_ID:
                    record.setRegTypeOne(regTypeOneMapper().mapFieldSet(fieldSet));
                    break;
                case RegTypeTwo.LINE_ID:
                    record.setRegTypeTwo(regTypeTwoMapper().mapFieldSet(fieldSet));
                    break;
                case RegTypeThree.LINE_ID:
                    record.setRegTypeThree(regTypeThreeMapper().mapFieldSet(fieldSet));
                    break;
                default:
                    log.warn("Tipo de registro não reconhecido");
                    break;
            }
            FieldSet nextLine = delegate.peek();

            if (nextLine == null || ((nextLine.readString(0) + nextLine.readString(1)).equals(Transaction.LINE_ID)
                    && !lineId.equals(HEADER_ID))) {
                break loop;
            }

        }
        log.info("fim de um registro de transação ou do arquivo");
        return record;
    }

    @Override
    public void update(ExecutionContext ec) throws ItemStreamException {
        delegate.update(ec);
    }

    @Bean
    public TransactionFieldSetMapper transactionMapper() {
        return new TransactionFieldSetMapper();
    }

    @Bean
    public RegTypeOneFieldSetMapper regTypeOneMapper() {
        return new RegTypeOneFieldSetMapper();
    }

    @Bean
    public RegTypeTwoFieldSetMapper regTypeTwoMapper() {
        return new RegTypeTwoFieldSetMapper();
    }

    @Bean
    public RegTypeThreeFieldSetMapper regTypeThreeMapper() {
        return new RegTypeThreeFieldSetMapper();
    }

    @Bean
    public TransactionCompositeLineTokenizer compositeLineTokenizer() {
        return new TransactionCompositeLineTokenizer();
    }

}