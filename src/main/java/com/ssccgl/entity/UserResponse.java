package com.ssccgl.entity;

import com.ssccgl.enums.QuestionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_responses",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attempt_id", "exam_question_id"})
    },
    indexes = {
        @Index(name = "idx_ur_attempt", columnList = "attempt_id"),
        @Index(name = "idx_ur_eq",      columnList = "exam_question_id")
    })
public class UserResponse {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exam_question_id", nullable = false)
    private ExamQuestion examQuestion;

    @Column(name = "selected_option", length = 1)
    private String selectedOption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus questionStatus = QuestionStatus.NOT_VISITED;

    @Column(name = "time_spent_seconds")
    private int timeSpentSeconds = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserResponse() {}

    public Long getId()                     { return id; }
    public ExamAttempt getAttempt()         { return attempt; }
    public ExamQuestion getExamQuestion()   { return examQuestion; }
    public String getSelectedOption()       { return selectedOption; }
    public QuestionStatus getQuestionStatus(){ return questionStatus; }
    public int getTimeSpentSeconds()        { return timeSpentSeconds; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }

    public void setId(Long id)                           { this.id = id; }
    public void setAttempt(ExamAttempt attempt)          { this.attempt = attempt; }
    public void setExamQuestion(ExamQuestion eq)         { this.examQuestion = eq; }
    public void setSelectedOption(String v)              { this.selectedOption = v; }
    public void setQuestionStatus(QuestionStatus v)      { this.questionStatus = v; }
    public void setTimeSpentSeconds(int v)               { this.timeSpentSeconds = v; }

    public boolean isCorrect() {
        if (selectedOption == null) return false;
        return selectedOption.equalsIgnoreCase(examQuestion.getQuestion().getCorrectAnswer());
    }

    public boolean isAttempted() {
        return selectedOption != null && !selectedOption.isBlank();
    }

    public static UserResponseBuilder builder() { return new UserResponseBuilder(); }
    public static class UserResponseBuilder {
        private final UserResponse r = new UserResponse();
        public UserResponseBuilder attempt(ExamAttempt v)       { r.attempt = v;        return this; }
        public UserResponseBuilder examQuestion(ExamQuestion v)  { r.examQuestion = v;   return this; }
        public UserResponseBuilder selectedOption(String v)     { r.selectedOption = v; return this; }
        public UserResponseBuilder questionStatus(QuestionStatus v){ r.questionStatus = v; return this; }
        public UserResponseBuilder timeSpentSeconds(int v)      { r.timeSpentSeconds = v; return this; }
        public UserResponse build()                             { return r; }
    }
}
