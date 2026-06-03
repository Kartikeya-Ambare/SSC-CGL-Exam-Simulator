package com.ssccgl.exam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "results", indexes = {
    @Index(name = "idx_results_user", columnList = "user_id"),
    @Index(name = "idx_results_session", columnList = "session_id"),
    @Index(name = "idx_results_total_score", columnList = "total_score")
})
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ExamSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_score", nullable = false)
    private double totalScore;

    @Column(name = "max_marks", nullable = false)
    private double maxMarks = 200.0;

    @Column(name = "total_correct", nullable = false)
    private int totalCorrect;

    @Column(name = "total_incorrect", nullable = false)
    private int totalIncorrect;

    @Column(name = "total_unattempted", nullable = false)
    private int totalUnattempted;

    @Column(name = "total_attempted", nullable = false)
    private int totalAttempted;

    @Column(name = "accuracy_percentage")
    private double accuracyPercentage;

    @Column(name = "attempt_percentage")
    private double attemptPercentage;

    @Column(name = "percentile")
    private double percentile;

    @Column(name = "time_taken_seconds")
    private int timeTakenSeconds;

    // Section-wise scores stored as JSON
    @Column(name = "section_scores", columnDefinition = "TEXT")
    private String sectionScoresJson;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExamSession getSession() { return session; }
    public void setSession(ExamSession session) { this.session = session; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }

    public int getTotalCorrect() { return totalCorrect; }
    public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }

    public int getTotalIncorrect() { return totalIncorrect; }
    public void setTotalIncorrect(int totalIncorrect) { this.totalIncorrect = totalIncorrect; }

    public int getTotalUnattempted() { return totalUnattempted; }
    public void setTotalUnattempted(int totalUnattempted) { this.totalUnattempted = totalUnattempted; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public double getAccuracyPercentage() { return accuracyPercentage; }
    public void setAccuracyPercentage(double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }

    public double getAttemptPercentage() { return attemptPercentage; }
    public void setAttemptPercentage(double attemptPercentage) { this.attemptPercentage = attemptPercentage; }

    public double getPercentile() { return percentile; }
    public void setPercentile(double percentile) { this.percentile = percentile; }

    public int getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(int timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }

    public String getSectionScoresJson() { return sectionScoresJson; }
    public void setSectionScoresJson(String sectionScoresJson) { this.sectionScoresJson = sectionScoresJson; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
}
