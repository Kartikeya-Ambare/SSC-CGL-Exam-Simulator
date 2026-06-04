package com.ssccgl.exam.repository;

import com.ssccgl.exam.entity.Result;
import com.ssccgl.exam.entity.ExamSession;
import com.ssccgl.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findBySession(ExamSession session);

    List<Result> findByUserOrderByCalculatedAtDesc(User user);

    @Query("SELECT COUNT(r) FROM Result r WHERE r.totalScore > :score")
    long countBetterScores(double score);

    @Query("SELECT COUNT(r) FROM Result r")
    long countTotal();

    Optional<Result> findBySessionId(Long sessionId);
}
