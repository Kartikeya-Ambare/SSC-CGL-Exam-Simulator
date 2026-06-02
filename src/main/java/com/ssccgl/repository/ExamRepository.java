package com.ssccgl.repository;

import com.ssccgl.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT COUNT(e) FROM Exam e WHERE e.active = true")
    long countActiveExams();
}
