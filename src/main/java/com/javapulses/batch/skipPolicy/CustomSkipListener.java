package com.javapulses.batch.skipPolicy;

import com.javapulses.batch.model.CorruptedRecord;
import com.javapulses.batch.model.Student;
import com.javapulses.batch.repository.CorruptedRecordRepository;
import org.springframework.batch.core.listener.SkipListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.item.file.FlatFileParseException;

public class CustomSkipListener extends SkipListenerSupport<Student, Student> {

    @Autowired
    private CorruptedRecordRepository corruptedRecordRepository;

    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof FlatFileParseException) {
            FlatFileParseException ffpe = (FlatFileParseException) t;
            String[] fields = ffpe.getInput().split(",");
            CorruptedRecord corruptedRecord = new CorruptedRecord();
            corruptedRecord.setStudentId(Integer.parseInt(fields[0]));
            corruptedRecord.setFirstName(fields[1]);
            corruptedRecord.setLastName(fields[2]);
            corruptedRecord.setAge(fields[3]);
            corruptedRecord.setErrorMessage(t.getMessage());
            corruptedRecordRepository.save(corruptedRecord);
        }
    }
}