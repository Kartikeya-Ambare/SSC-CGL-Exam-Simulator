package com.ssccgl.repository;

import com.ssccgl.entity.ExamAttempt;
import com.ssccgl.entity.UserResponse;
import com.ssccgl.enums.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {

    List<UserResponse> findByAttempt(ExamAttempt attempt);

    Optional<UserResponse> findByAttemptAndExamQuestion_Id(ExamAttempt attempt, Long examQuestionId);

    long countByAttemptAndSelectedOptionIsNotNull(ExamAttempt attempt);

    long countByAttemptAndQuestionStatus(ExamAttempt attempt, QuestionStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserResponse ur WHERE ur.attempt = :attempt")
    void deleteByAttempt(@Param("attempt") ExamAttempt attempt);
}
