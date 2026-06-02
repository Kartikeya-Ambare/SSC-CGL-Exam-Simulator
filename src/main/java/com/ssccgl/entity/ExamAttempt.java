package com.ssccgl.entity;

import com.ssccgl.enums.ExamStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_attempts",
    indexes = {
        @Index(name = "idx_ea_user",   columnList = "user_id"),
        @Index(name = "idx_ea_exam",   columnList = "exam_id"),
        @Index(name = "idx_ea_status", columnList = "status")
    })
public class ExamAttempt {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status = ExamStatus.NOT_STARTED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "remaining_seconds")
    private Integer remainingSeconds;

    @Column(name = "current_question_index")
    private int currentQuestionIndex = 0;

    @Column(name = "tab_switch_count")
    private int tabSwitchCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserResponse> responses = new ArrayList<>();

    @OneToOne(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Result result;

    public ExamAttempt() {}

    public Long getId()                       { return id; }
    public User getUser()                     { return user; }
    public Exam getExam()                     { return exam; }
    public ExamStatus getStatus()             { return status; }
    public LocalDateTime getStartedAt()       { return startedAt; }
    public LocalDateTime getSubmittedAt()     { return submittedAt; }
    public Integer getRemainingSeconds()      { return remainingSeconds; }
    public int getCurrentQuestionIndex()      { return currentQuestionIndex; }
    public int getTabSwitchCount()            { return tabSwitchCount; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }
    public List<UserResponse> getResponses()  { return responses; }
    public Result getResult()                 { return result; }

    public void setId(Long id)                            { this.id = id; }
    public void setUser(User user)                        { this.user = user; }
    public void setExam(Exam exam)                        { this.exam = exam; }
    public void setStatus(ExamStatus status)              { this.status = status; }
    public void setStartedAt(LocalDateTime v)             { this.startedAt = v; }
    public void setSubmittedAt(LocalDateTime v)           { this.submittedAt = v; }
    public void setRemainingSeconds(Integer v)            { this.remainingSeconds = v; }
    public void setCurrentQuestionIndex(int v)            { this.currentQuestionIndex = v; }
    public void setTabSwitchCount(int v)                  { this.tabSwitchCount = v; }
    public void setResponses(List<UserResponse> v)        { this.responses = v; }
    public void setResult(Result v)                       { this.result = v; }

    public static ExamAttemptBuilder builder() { return new ExamAttemptBuilder(); }
    public static class ExamAttemptBuilder {
        private final ExamAttempt a = new ExamAttempt();
        public ExamAttemptBuilder user(User v)                 { a.user = v;                 return this; }
        public ExamAttemptBuilder exam(Exam v)                 { a.exam = v;                 return this; }
        public ExamAttemptBuilder status(ExamStatus v)         { a.status = v;               return this; }
        public ExamAttemptBuilder startedAt(LocalDateTime v)   { a.startedAt = v;            return this; }
        public ExamAttemptBuilder remainingSeconds(Integer v)  { a.remainingSeconds = v;     return this; }
        public ExamAttemptBuilder currentQuestionIndex(int v)  { a.currentQuestionIndex = v; return this; }
        public ExamAttempt build()                             { return a; }
    }
}
