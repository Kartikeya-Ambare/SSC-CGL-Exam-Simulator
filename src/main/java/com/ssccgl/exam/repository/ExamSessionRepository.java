package com.ssccgl.exam.repository;

import com.ssccgl.exam.entity.ExamSession;
import com.ssccgl.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    Optional<ExamSession> findByUserAndStatus(User user, ExamSession.Status status);

    List<ExamSession> findByUserOrderByStartTimeDesc(User user);

    @Query("SELECT COUNT(e) FROM ExamSession e WHERE e.user = :user")
    long countByUser(User user);

    @Modifying
    @Query("UPDATE ExamSession e SET e.timeRemainingSeconds = :remaining WHERE e.id = :id")
    void updateTimeRemaining(Long id, int remaining);

    @Modifying
    @Query("UPDATE ExamSession e SET e.currentQuestionIndex = :idx, e.currentSection = :section WHERE e.id = :id")
    void updateCurrentPosition(Long id, int idx, String section);

    List<ExamSession> findByStatusOrderByStartTimeAsc(ExamSession.Status status);

    @Query("SELECT MAX(e.attemptNumber) FROM ExamSession e WHERE e.user = :user")
    Integer findMaxAttemptNumberByUser(User user);
}
