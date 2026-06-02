package com.ssccgl.repository;

import com.ssccgl.entity.Result;
import com.ssccgl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByAttemptId(Long attemptId);

    List<Result> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT AVG(r.totalScore) FROM Result r WHERE r.user = :user")
    Double findAverageScoreByUser(@Param("user") User user);

    @Query("SELECT MAX(r.totalScore) FROM Result r WHERE r.user = :user")
    Double findHighestScoreByUser(@Param("user") User user);

    @Query("SELECT AVG(r.accuracyPercentage) FROM Result r WHERE r.user = :user")
    Double findAverageAccuracyByUser(@Param("user") User user);

    // For percentile calculation
    @Query("SELECT COUNT(r) FROM Result r WHERE r.totalScore < :score")
    long countResultsBelowScore(@Param("score") double score);

    @Query("SELECT COUNT(r) FROM Result r")
    long countAllResults();

    // Dashboard trend data (last 10 attempts)
    @Query("""
        SELECT r FROM Result r
        WHERE r.user = :user
        ORDER BY r.createdAt DESC
        """)
    List<Result> findTop10ByUserOrderByCreatedAtDesc(@Param("user") User user,
        org.springframework.data.domain.Pageable pageable);
}
