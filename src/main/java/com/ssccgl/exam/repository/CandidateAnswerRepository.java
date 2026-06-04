package com.ssccgl.exam.repository;

import com.ssccgl.exam.entity.CandidateAnswer;
import com.ssccgl.exam.entity.ExamQuestion;
import com.ssccgl.exam.entity.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateAnswerRepository extends JpaRepository<CandidateAnswer, Long> {

    Optional<CandidateAnswer> findBySessionAndExamQuestion(ExamSession session, ExamQuestion examQuestion);

    List<CandidateAnswer> findBySession(ExamSession session);

    @Query("SELECT COUNT(ca) FROM CandidateAnswer ca WHERE ca.session = :session AND ca.answerStatus = 'ANSWERED'")
    long countAnsweredBySession(ExamSession session);

    @Query("SELECT COUNT(ca) FROM CandidateAnswer ca WHERE ca.session = :session AND ca.answerStatus = 'ANSWERED_MARKED'")
    long countAnsweredMarkedBySession(ExamSession session);

    @Query("SELECT COUNT(ca) FROM CandidateAnswer ca WHERE ca.session = :session AND ca.answerStatus = 'MARKED_FOR_REVIEW'")
    long countMarkedBySession(ExamSession session);

    @Query("SELECT COUNT(ca) FROM CandidateAnswer ca WHERE ca.session = :session AND ca.answerStatus = 'NOT_ANSWERED'")
    long countNotAnsweredBySession(ExamSession session);

    @Modifying
    @Query("DELETE FROM CandidateAnswer ca WHERE ca.session = :session")
    void deleteBySession(ExamSession session);

    @Query("SELECT ca FROM CandidateAnswer ca JOIN FETCH ca.examQuestion eq JOIN FETCH eq.question WHERE ca.session = :session")
    List<CandidateAnswer> findBySessionWithDetails(ExamSession session);
}
