package com.ssccgl.exam.repository;

import com.ssccgl.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    boolean existsByJsonId(Long jsonId);

    long countBySection(Question.Section section);

    @Query(value = "SELECT * FROM questions WHERE section = :#{#section.name()} ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomBySection(Question.Section section, int limit);

    List<Question> findBySection(Question.Section section);

    @Query("SELECT DISTINCT q.topic FROM Question q WHERE q.section = :section")
    List<String> findDistinctTopicsBySection(Question.Section section);
}
