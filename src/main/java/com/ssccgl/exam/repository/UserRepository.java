package com.ssccgl.exam.repository;

import com.ssccgl.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :time WHERE u.id = :id")
    void updateLastLogin(Long id, LocalDateTime time);

    @Modifying
    @Query("UPDATE User u SET u.totalAttempts = u.totalAttempts + 1 WHERE u.id = :id")
    void incrementAttempts(Long id);

    @Modifying
    @Query("UPDATE User u SET u.bestScore = :score WHERE u.id = :id AND (u.bestScore IS NULL OR u.bestScore < :score)")
    void updateBestScore(Long id, Double score);
}
