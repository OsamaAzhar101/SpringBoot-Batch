package com.javapulses.batch.repository;


import com.javapulses.batch.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
