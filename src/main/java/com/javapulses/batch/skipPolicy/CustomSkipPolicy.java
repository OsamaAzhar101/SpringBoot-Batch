package com.javapulses.batch.skipPolicy;

import com.javapulses.batch.model.CorruptedRecord;
import com.javapulses.batch.repository.CorruptedRecordRepository;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.item.file.FlatFileParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomSkipPolicy implements SkipPolicy {

    private static final Logger logger = LoggerFactory.getLogger(CustomSkipPolicy.class);

    @Autowired
    private CorruptedRecordRepository corruptedRecordRepository;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if (t instanceof FlatFileParseException) {
            FlatFileParseException ffpe = (FlatFileParseException) t;
            logger.error("Skipping record at line {} due to parsing error: {}", ffpe.getLineNumber(), ffpe.getInput());


            return true;
        }
        return false;
    }
}