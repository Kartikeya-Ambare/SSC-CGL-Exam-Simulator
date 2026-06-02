package com.ssccgl.repository;

import com.ssccgl.entity.Exam;
import com.ssccgl.entity.ExamAttempt;
import com.ssccgl.entity.User;
import com.ssccgl.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findByUserOrderByCreatedAtDesc(User user);

    Optional<ExamAttempt> findByUserAndExamAndStatus(User user, Exam exam, ExamStatus status);

    long countByUser(User user);

    @Query("SELECT COUNT(ea) FROM ExamAttempt ea WHERE ea.user = :user AND ea.status = 'SUBMITTED'")
    long countSubmittedByUser(@Param("user") User user);

    @Query("SELECT ea FROM ExamAttempt ea WHERE ea.user = :user AND ea.status = 'IN_PROGRESS'")
    Optional<ExamAttempt> findInProgressByUser(@Param("user") User user);

    List<ExamAttempt> findByStatusOrderByCreatedAtDesc(ExamStatus status);
}
