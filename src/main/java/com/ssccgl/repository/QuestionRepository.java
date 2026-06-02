package com.ssccgl.repository;

import com.ssccgl.entity.Question;
import com.ssccgl.enums.Difficulty;
import com.ssccgl.enums.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // ── Admin queries ──────────────────────────────────────
    Page<Question> findByActiveTrue(Pageable pageable);

    Page<Question> findBySectionAndActiveTrue(Section section, Pageable pageable);

    Page<Question> findBySectionAndTopicAndActiveTrue(
        Section section, String topic, Pageable pageable);

    // ── Count queries ──────────────────────────────────────
    long countBySectionAndDifficultyAndActiveTrue(Section section, Difficulty difficulty);

    long countBySectionAndActiveTrue(Section section);

    // ── Random selection (O(1) approach using RAND()) ─────
    /**
     * Randomly select N questions for a section+difficulty combination.
     * RAND() is acceptable for MySQL for up to ~100k rows.
     */
    @Query(value = """
        SELECT * FROM questions
        WHERE section = :section
          AND difficulty = :difficulty
          AND is_active = true
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Question> findRandomBySectionAndDifficulty(
        @Param("section") String section,
        @Param("difficulty") String difficulty,
        @Param("limit") int limit
    );

    @Query(value = """
        SELECT * FROM questions
        WHERE section = :section
          AND is_active = true
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Question> findRandomBySection(
        @Param("section") String section,
        @Param("limit") int limit
    );

    /**
     * Exclude already-used question IDs to prevent repetition.
     */
    @Query(value = """
        SELECT * FROM questions
        WHERE section = :section
          AND difficulty = :difficulty
          AND is_active = true
          AND id NOT IN (:excludeIds)
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Question> findRandomExcluding(
        @Param("section") String section,
        @Param("difficulty") String difficulty,
        @Param("excludeIds") List<Long> excludeIds,
        @Param("limit") int limit
    );

    // ── Topic statistics ───────────────────────────────────
    @Query("SELECT DISTINCT q.topic FROM Question q WHERE q.section = :section AND q.active = true")
    List<String> findTopicsBySection(@Param("section") Section section);

    @Query("SELECT q.section, COUNT(q) FROM Question q WHERE q.active = true GROUP BY q.section")
    List<Object[]> countBySection();

    // ── Search (Admin panel) ───────────────────────────────
    @Query("""
        SELECT q FROM Question q
        WHERE q.active = true
          AND (LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(q.topic) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // List variants used by AdminController (no pagination needed for small results)
    @Query("""
        SELECT q FROM Question q
        WHERE (LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(q.topic) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY q.id DESC
        """)
    List<Question> searchByKeyword(@Param("keyword") String keyword);

    List<Question> findBySectionAndActiveTrue(Section section);

    List<Question> findBySectionAndDifficultyAndActiveTrue(Section section, Difficulty difficulty);
}
