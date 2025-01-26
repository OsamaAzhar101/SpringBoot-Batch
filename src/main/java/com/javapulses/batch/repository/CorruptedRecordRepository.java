package com.javapulses.batch.repository;

import com.javapulses.batch.model.CorruptedRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorruptedRecordRepository extends JpaRepository<CorruptedRecord, Long> {
}