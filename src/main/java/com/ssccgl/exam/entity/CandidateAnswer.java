package com.ssccgl.exam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_answers", indexes = {
    @Index(name = "idx_candidate_answers_session", columnList = "session_id"),
    @Index(name = "idx_candidate_answers_exam_question", columnList = "exam_question_id")
})
public class CandidateAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ExamSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exam_question_id", nullable = false)
    private ExamQuestion examQuestion;

    @Column(name = "selected_option")
    private Integer selectedOption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnswerStatus answerStatus = AnswerStatus.NOT_VISITED;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "marks_obtained")
    private Double marksObtained;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "visited_at")
    private LocalDateTime visitedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "is_marked_for_review")
    private boolean markedForReview = false;

    public enum AnswerStatus {
        NOT_VISITED, NOT_ANSWERED, ANSWERED, MARKED_FOR_REVIEW, ANSWERED_MARKED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExamSession getSession() { return session; }
    public void setSession(ExamSession session) { this.session = session; }

    public ExamQuestion getExamQuestion() { return examQuestion; }
    public void setExamQuestion(ExamQuestion examQuestion) { this.examQuestion = examQuestion; }

    public Integer getSelectedOption() { return selectedOption; }
    public void setSelectedOption(Integer selectedOption) { this.selectedOption = selectedOption; }

    public AnswerStatus getAnswerStatus() { return answerStatus; }
    public void setAnswerStatus(AnswerStatus answerStatus) { this.answerStatus = answerStatus; }

    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public Integer getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(Integer timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }

    public LocalDateTime getVisitedAt() { return visitedAt; }
    public void setVisitedAt(LocalDateTime visitedAt) { this.visitedAt = visitedAt; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }

    public boolean isMarkedForReview() { return markedForReview; }
    public void setMarkedForReview(boolean markedForReview) { this.markedForReview = markedForReview; }
}
