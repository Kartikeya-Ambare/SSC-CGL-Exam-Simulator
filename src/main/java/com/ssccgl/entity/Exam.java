package com.ssccgl.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "total_questions")
    private int totalQuestions = 100;

    @Column(name = "total_marks")
    private int totalMarks = 200;

    @Column(name = "duration_minutes")
    private int durationMinutes = 60;

    @Column(name = "marks_per_correct")
    private double marksPerCorrect = 2.0;

    @Column(name = "negative_marks")
    private double negativeMarks = 0.33;

    @Column(name = "is_active")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamQuestion> examQuestions = new ArrayList<>();

    public Exam() {}

    public Long getId()                            { return id; }
    public String getTitle()                       { return title; }
    public int getTotalQuestions()                 { return totalQuestions; }
    public int getTotalMarks()                     { return totalMarks; }
    public int getDurationMinutes()                { return durationMinutes; }
    public double getMarksPerCorrect()             { return marksPerCorrect; }
    public double getNegativeMarks()               { return negativeMarks; }
    public boolean isActive()                      { return active; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public User getCreatedBy()                     { return createdBy; }
    public List<ExamQuestion> getExamQuestions()   { return examQuestions; }

    public void setId(Long id)                             { this.id = id; }
    public void setTitle(String title)                     { this.title = title; }
    public void setTotalQuestions(int v)                   { this.totalQuestions = v; }
    public void setTotalMarks(int v)                       { this.totalMarks = v; }
    public void setDurationMinutes(int v)                  { this.durationMinutes = v; }
    public void setMarksPerCorrect(double v)               { this.marksPerCorrect = v; }
    public void setNegativeMarks(double v)                 { this.negativeMarks = v; }
    public void setActive(boolean v)                       { this.active = v; }
    public void setCreatedBy(User v)                       { this.createdBy = v; }
    public void setExamQuestions(List<ExamQuestion> v)     { this.examQuestions = v; }

    public static ExamBuilder builder() { return new ExamBuilder(); }
    public static class ExamBuilder {
        private final Exam e = new Exam();
        public ExamBuilder title(String v)            { e.title = v;           return this; }
        public ExamBuilder totalQuestions(int v)      { e.totalQuestions = v;  return this; }
        public ExamBuilder totalMarks(int v)          { e.totalMarks = v;      return this; }
        public ExamBuilder durationMinutes(int v)     { e.durationMinutes = v; return this; }
        public ExamBuilder marksPerCorrect(double v)  { e.marksPerCorrect = v; return this; }
        public ExamBuilder negativeMarks(double v)    { e.negativeMarks = v;   return this; }
        public ExamBuilder active(boolean v)          { e.active = v;          return this; }
        public ExamBuilder createdBy(User v)          { e.createdBy = v;       return this; }
        public Exam build()                           { return e; }
    }
}
