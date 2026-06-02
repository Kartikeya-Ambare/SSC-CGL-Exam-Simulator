package com.ssccgl.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_questions",
    indexes = {
        @Index(name = "idx_eq_exam",     columnList = "exam_id"),
        @Index(name = "idx_eq_question", columnList = "question_id")
    })
public class ExamQuestion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "option_mapping", length = 10)
    private String optionMapping = "A,B,C,D";

    public ExamQuestion() {}

    public Long getId()              { return id; }
    public Exam getExam()            { return exam; }
    public Question getQuestion()    { return question; }
    public int getDisplayOrder()     { return displayOrder; }
    public String getOptionMapping() { return optionMapping; }

    public void setId(Long id)                     { this.id = id; }
    public void setExam(Exam exam)                 { this.exam = exam; }
    public void setQuestion(Question question)     { this.question = question; }
    public void setDisplayOrder(int displayOrder)  { this.displayOrder = displayOrder; }
    public void setOptionMapping(String v)         { this.optionMapping = v; }

    public static ExamQuestionBuilder builder() { return new ExamQuestionBuilder(); }
    public static class ExamQuestionBuilder {
        private final ExamQuestion eq = new ExamQuestion();
        public ExamQuestionBuilder exam(Exam v)            { eq.exam = v;          return this; }
        public ExamQuestionBuilder question(Question v)    { eq.question = v;      return this; }
        public ExamQuestionBuilder displayOrder(int v)     { eq.displayOrder = v;  return this; }
        public ExamQuestionBuilder optionMapping(String v) { eq.optionMapping = v; return this; }
        public ExamQuestion build()                        { return eq; }
    }
}
