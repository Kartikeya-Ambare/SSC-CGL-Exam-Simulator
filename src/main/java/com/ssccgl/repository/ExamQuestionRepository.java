package com.ssccgl.repository;

import com.ssccgl.entity.Exam;
import com.ssccgl.entity.ExamQuestion;
import com.ssccgl.enums.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExamOrderByDisplayOrder(Exam exam);

    Optional<ExamQuestion> findByExamAndDisplayOrder(Exam exam, int displayOrder);

    @Query("""
        SELECT eq FROM ExamQuestion eq
        WHERE eq.exam = :exam AND eq.question.section = :section
        ORDER BY eq.displayOrder
        """)
    List<ExamQuestion> findByExamAndSection(
        @Param("exam") Exam exam,
        @Param("section") Section section
    );
}
