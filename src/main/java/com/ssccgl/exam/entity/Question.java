package com.ssccgl.exam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions", indexes = {
    @Index(name = "idx_questions_section", columnList = "section"),
    @Index(name = "idx_questions_difficulty", columnList = "difficulty"),
    @Index(name = "idx_questions_topic", columnList = "topic"),
    @Index(name = "idx_questions_json_id", columnList = "json_id")
})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "json_id", nullable = false)
    private Long jsonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Section section;

    @Column(nullable = false, length = 100)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "option_a", nullable = false, columnDefinition = "TEXT")
    private String optionA;

    @Column(name = "option_b", nullable = false, columnDefinition = "TEXT")
    private String optionB;

    @Column(name = "option_c", nullable = false, columnDefinition = "TEXT")
    private String optionC;

    @Column(name = "option_d", nullable = false, columnDefinition = "TEXT")
    private String optionD;

    @Column(name = "correct_answer", nullable = false)
    private int correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Section {
        GENERAL_AWARENESS, VERBAL_ABILITY, LOGICAL_REASONING, QUANTITATIVE_APTITUDE
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJsonId() { return jsonId; }
    public void setJsonId(Long jsonId) { this.jsonId = jsonId; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getOption(int index) {
        return switch (index) {
            case 0 -> optionA;
            case 1 -> optionB;
            case 2 -> optionC;
            case 3 -> optionD;
            default -> "";
        };
    }
}
