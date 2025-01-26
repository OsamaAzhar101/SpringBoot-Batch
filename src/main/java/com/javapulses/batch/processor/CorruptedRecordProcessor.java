package com.javapulses.batch.processor;

import com.javapulses.batch.model.CorruptedRecord;
import com.javapulses.batch.model.Student;
import org.springframework.batch.item.ItemProcessor;

public class CorruptedRecordProcessor implements ItemProcessor<CorruptedRecord, Student> {

    @Override
    public Student process(CorruptedRecord corruptedRecord) throws Exception {
        // Implement your logic to process the corrupted record
        Student student = new Student();
        student.setId(corruptedRecord.getStudentId());
        student.setFirstName(corruptedRecord.getFirstName());
        student.setLastName(corruptedRecord.getLastName());
        student.setAge(corruptedRecord.getAge());
        return student;
    }
}