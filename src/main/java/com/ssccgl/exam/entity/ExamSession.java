package com.ssccgl.exam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_sessions", indexes = {
    @Index(name = "idx_exam_sessions_user", columnList = "user_id"),
    @Index(name = "idx_exam_sessions_status", columnList = "status"),
    @Index(name = "idx_exam_sessions_start_time", columnList = "start_time")
})
public class ExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds = 3600;

    @Column(name = "time_remaining_seconds")
    private int timeRemainingSeconds = 3600;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.IN_PROGRESS;

    @Column(name = "current_question_index")
    private int currentQuestionIndex = 0;

    @Column(name = "current_section")
    private String currentSection = "GENERAL_AWARENESS";

    @Column(name = "is_auto_submitted")
    private boolean autoSubmitted = false;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    public enum Status {
        IN_PROGRESS, SUBMITTED, TIMED_OUT, ABANDONED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getTimeRemainingSeconds() { return timeRemainingSeconds; }
    public void setTimeRemainingSeconds(int timeRemainingSeconds) { this.timeRemainingSeconds = timeRemainingSeconds; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }

    public String getCurrentSection() { return currentSection; }
    public void setCurrentSection(String currentSection) { this.currentSection = currentSection; }

    public boolean isAutoSubmitted() { return autoSubmitted; }
    public void setAutoSubmitted(boolean autoSubmitted) { this.autoSubmitted = autoSubmitted; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
}
