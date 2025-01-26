package com.javapulses.batch.processor;

import com.javapulses.batch.model.CorruptedRecord;
import com.javapulses.batch.model.Student;
import com.javapulses.batch.repository.CorruptedRecordRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class StudentProcessor implements ItemProcessor<Student, Student> {

    @Autowired
    private CorruptedRecordRepository corruptedRecordRepository;

    @Override
    public Student process(Student student) throws Exception {
        try {
            System.out.println("Processing Student: " + student);
            System.out.println("CALL API  " + student);

            if (student.getId() == 26) {
                throw new Exception("Invalid ID");
            }
            return student;
        } catch (Exception e) {
            CorruptedRecord corruptedRecord = new CorruptedRecord();
            corruptedRecord.setStudentId(student.getId());
            corruptedRecord.setFirstName(student.getFirstName());
            corruptedRecord.setLastName(student.getLastName());
            corruptedRecord.setAge(student.getAge());
            corruptedRecord.setErrorMessage(e.getMessage());
            corruptedRecordRepository.save(corruptedRecord);
            throw e; // Re-throw the exception to trigger skip policy
        }
    }

}
