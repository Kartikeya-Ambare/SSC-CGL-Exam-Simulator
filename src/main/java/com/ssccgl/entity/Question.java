package com.ssccgl.entity;

import com.ssccgl.enums.Difficulty;
import com.ssccgl.enums.Section;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Question entity — uses explicit getters/setters/constructors
 * to avoid Lombok annotation-processor dependency issues.
 */
@Entity
@Table(name = "questions",
    indexes = {
        @Index(name = "idx_q_section",     columnList = "section"),
        @Index(name = "idx_q_topic",       columnList = "topic"),
        @Index(name = "idx_q_difficulty",  columnList = "difficulty"),
        @Index(name = "idx_q_section_diff",columnList = "section, difficulty")
    })
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Section section;

    @Column(nullable = false, length = 100)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "option_a", nullable = false, columnDefinition = "TEXT")
    private String optionA;

    @Column(name = "option_b", nullable = false, columnDefinition = "TEXT")
    private String optionB;

    @Column(name = "option_c", nullable = false, columnDefinition = "TEXT")
    private String optionC;

    @Column(name = "option_d", nullable = false, columnDefinition = "TEXT")
    private String optionD;

    /** Correct answer: 'A', 'B', 'C', or 'D' */
    @Column(name = "correct_answer", nullable = false, length = 1)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(length = 100)
    private String source;

    @Column(name = "exam_year")
    private Integer examYear;

    @Column(name = "is_active")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────
    public Question() {}

    // ── Getters ───────────────────────────────────────────────
    public Long getId()             { return id; }
    public Section getSection()     { return section; }
    public String getTopic()        { return topic; }
    public Difficulty getDifficulty(){ return difficulty; }
    public String getQuestionText() { return questionText; }
    public String getOptionA()      { return optionA; }
    public String getOptionB()      { return optionB; }
    public String getOptionC()      { return optionC; }
    public String getOptionD()      { return optionD; }
    public String getCorrectAnswer(){ return correctAnswer; }
    public String getExplanation()  { return explanation; }
    public String getSource()       { return source; }
    public Integer getExamYear()    { return examYear; }
    public boolean isActive()       { return active; }
    public boolean getActive()      { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Setters ───────────────────────────────────────────────
    public void setId(Long id)                    { this.id = id; }
    public void setSection(Section section)        { this.section = section; }
    public void setTopic(String topic)             { this.topic = topic; }
    public void setDifficulty(Difficulty d)        { this.difficulty = d; }
    public void setQuestionText(String t)          { this.questionText = t; }
    public void setOptionA(String o)               { this.optionA = o; }
    public void setOptionB(String o)               { this.optionB = o; }
    public void setOptionC(String o)               { this.optionC = o; }
    public void setOptionD(String o)               { this.optionD = o; }
    public void setCorrectAnswer(String a)         { this.correctAnswer = a; }
    public void setExplanation(String e)           { this.explanation = e; }
    public void setSource(String s)                { this.source = s; }
    public void setExamYear(Integer y)             { this.examYear = y; }
    public void setActive(boolean a)               { this.active = a; }

    // ── @Builder replacement (static builder) ─────────────────
    public static QuestionBuilder builder() { return new QuestionBuilder(); }

    public static class QuestionBuilder {
        private final Question q = new Question();
        public QuestionBuilder section(Section v)      { q.section = v;       return this; }
        public QuestionBuilder topic(String v)         { q.topic = v;         return this; }
        public QuestionBuilder difficulty(Difficulty v){ q.difficulty = v;    return this; }
        public QuestionBuilder questionText(String v)  { q.questionText = v;  return this; }
        public QuestionBuilder optionA(String v)       { q.optionA = v;       return this; }
        public QuestionBuilder optionB(String v)       { q.optionB = v;       return this; }
        public QuestionBuilder optionC(String v)       { q.optionC = v;       return this; }
        public QuestionBuilder optionD(String v)       { q.optionD = v;       return this; }
        public QuestionBuilder correctAnswer(String v) { q.correctAnswer = v; return this; }
        public QuestionBuilder explanation(String v)   { q.explanation = v;   return this; }
        public QuestionBuilder source(String v)        { q.source = v;        return this; }
        public QuestionBuilder examYear(Integer v)     { q.examYear = v;      return this; }
        public QuestionBuilder active(boolean v)       { q.active = v;        return this; }
        public Question build()                        { return q; }
    }

    // ── Helpers ───────────────────────────────────────────────
    public String getOptionByLetter(String letter) {
        return switch (letter.toUpperCase()) {
            case "A" -> optionA;
            case "B" -> optionB;
            case "C" -> optionC;
            case "D" -> optionD;
            default  -> null;
        };
    }

    public List<String> getAllOptions() {
        return List.of(optionA, optionB, optionC, optionD);
    }
}
