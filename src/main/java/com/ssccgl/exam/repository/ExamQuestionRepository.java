package com.ssccgl.exam.repository;

import com.ssccgl.exam.entity.ExamQuestion;
import com.ssccgl.exam.entity.ExamSession;
import com.ssccgl.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findBySessionOrderByPosition(ExamSession session);

    List<ExamQuestion> findBySessionAndSectionOrderBySectionPosition(ExamSession session, Question.Section section);

    Optional<ExamQuestion> findBySessionAndPosition(ExamSession session, int position);

    long countBySession(ExamSession session);

    @Query("SELECT eq FROM ExamQuestion eq JOIN FETCH eq.question WHERE eq.session = :session ORDER BY eq.position")
    List<ExamQuestion> findBySessionWithQuestions(ExamSession session);
}
