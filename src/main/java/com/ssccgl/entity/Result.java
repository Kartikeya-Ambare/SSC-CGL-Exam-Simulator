package com.ssccgl.entity;

import com.ssccgl.enums.Section;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "results",
    indexes = {
        @Index(name = "idx_result_user",    columnList = "user_id"),
        @Index(name = "idx_result_attempt", columnList = "attempt_id")
    })
public class Result {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_score",      nullable = false) private double totalScore;
    @Column(name = "max_score",        nullable = false) private double maxScore;
    @Column(name = "correct_count",    nullable = false) private int    correctCount;
    @Column(name = "wrong_count",      nullable = false) private int    wrongCount;
    @Column(name = "unattempted_count",nullable = false) private int    unattemptedCount;
    @Column(name = "negative_marks",   nullable = false) private double negativeMarks;
    @Column(name = "accuracy_percentage")                private double accuracyPercentage;
    @Column(name = "percentile")                         private double percentile;

    @Column(name = "gir_score")       private double girScore;
    @Column(name = "gir_correct")     private int    girCorrect;
    @Column(name = "gir_wrong")       private int    girWrong;
    @Column(name = "gir_unattempted") private int    girUnattempted;
    @Column(name = "gir_accuracy")    private double girAccuracy;

    @Column(name = "ga_score")        private double gaScore;
    @Column(name = "ga_correct")      private int    gaCorrect;
    @Column(name = "ga_wrong")        private int    gaWrong;
    @Column(name = "ga_unattempted")  private int    gaUnattempted;
    @Column(name = "ga_accuracy")     private double gaAccuracy;

    @Column(name = "qa_score")        private double qaScore;
    @Column(name = "qa_correct")      private int    qaCorrect;
    @Column(name = "qa_wrong")        private int    qaWrong;
    @Column(name = "qa_unattempted")  private int    qaUnattempted;
    @Column(name = "qa_accuracy")     private double qaAccuracy;

    @Column(name = "ec_score")        private double ecScore;
    @Column(name = "ec_correct")      private int    ecCorrect;
    @Column(name = "ec_wrong")        private int    ecWrong;
    @Column(name = "ec_unattempted")  private int    ecUnattempted;
    @Column(name = "ec_accuracy")     private double ecAccuracy;

    @Column(name = "total_time_seconds") private int totalTimeSeconds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Result() {}

    // Getters
    public Long getId()                 { return id; }
    public ExamAttempt getAttempt()     { return attempt; }
    public User getUser()               { return user; }
    public double getTotalScore()       { return totalScore; }
    public double getMaxScore()         { return maxScore; }
    public int getCorrectCount()        { return correctCount; }
    public int getWrongCount()          { return wrongCount; }
    public int getUnattemptedCount()    { return unattemptedCount; }
    public double getNegativeMarks()    { return negativeMarks; }
    public double getAccuracyPercentage(){ return accuracyPercentage; }
    public double getPercentile()       { return percentile; }
    public double getGirScore()         { return girScore; }
    public int getGirCorrect()          { return girCorrect; }
    public int getGirWrong()            { return girWrong; }
    public int getGirUnattempted()      { return girUnattempted; }
    public double getGirAccuracy()      { return girAccuracy; }
    public double getGaScore()          { return gaScore; }
    public int getGaCorrect()           { return gaCorrect; }
    public int getGaWrong()             { return gaWrong; }
    public int getGaUnattempted()       { return gaUnattempted; }
    public double getGaAccuracy()       { return gaAccuracy; }
    public double getQaScore()          { return qaScore; }
    public int getQaCorrect()           { return qaCorrect; }
    public int getQaWrong()             { return qaWrong; }
    public int getQaUnattempted()       { return qaUnattempted; }
    public double getQaAccuracy()       { return qaAccuracy; }
    public double getEcScore()          { return ecScore; }
    public int getEcCorrect()           { return ecCorrect; }
    public int getEcWrong()             { return ecWrong; }
    public int getEcUnattempted()       { return ecUnattempted; }
    public double getEcAccuracy()       { return ecAccuracy; }
    public int getTotalTimeSeconds()    { return totalTimeSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id)                        { this.id = id; }
    public void setAttempt(ExamAttempt v)             { this.attempt = v; }
    public void setUser(User v)                       { this.user = v; }
    public void setTotalScore(double v)               { this.totalScore = v; }
    public void setMaxScore(double v)                 { this.maxScore = v; }
    public void setCorrectCount(int v)                { this.correctCount = v; }
    public void setWrongCount(int v)                  { this.wrongCount = v; }
    public void setUnattemptedCount(int v)            { this.unattemptedCount = v; }
    public void setNegativeMarks(double v)            { this.negativeMarks = v; }
    public void setAccuracyPercentage(double v)       { this.accuracyPercentage = v; }
    public void setPercentile(double v)               { this.percentile = v; }
    public void setGirScore(double v)                 { this.girScore = v; }
    public void setGirCorrect(int v)                  { this.girCorrect = v; }
    public void setGirWrong(int v)                    { this.girWrong = v; }
    public void setGirUnattempted(int v)              { this.girUnattempted = v; }
    public void setGirAccuracy(double v)              { this.girAccuracy = v; }
    public void setGaScore(double v)                  { this.gaScore = v; }
    public void setGaCorrect(int v)                   { this.gaCorrect = v; }
    public void setGaWrong(int v)                     { this.gaWrong = v; }
    public void setGaUnattempted(int v)               { this.gaUnattempted = v; }
    public void setGaAccuracy(double v)               { this.gaAccuracy = v; }
    public void setQaScore(double v)                  { this.qaScore = v; }
    public void setQaCorrect(int v)                   { this.qaCorrect = v; }
    public void setQaWrong(int v)                     { this.qaWrong = v; }
    public void setQaUnattempted(int v)               { this.qaUnattempted = v; }
    public void setQaAccuracy(double v)               { this.qaAccuracy = v; }
    public void setEcScore(double v)                  { this.ecScore = v; }
    public void setEcCorrect(int v)                   { this.ecCorrect = v; }
    public void setEcWrong(int v)                     { this.ecWrong = v; }
    public void setEcUnattempted(int v)               { this.ecUnattempted = v; }
    public void setEcAccuracy(double v)               { this.ecAccuracy = v; }
    public void setTotalTimeSeconds(int v)            { this.totalTimeSeconds = v; }

    public double getSectionScore(Section section) {
        return switch (section) {
            case GENERAL_INTELLIGENCE_REASONING -> girScore;
            case GENERAL_AWARENESS             -> gaScore;
            case QUANTITATIVE_APTITUDE         -> qaScore;
            case ENGLISH_COMPREHENSION         -> ecScore;
        };
    }

    public static ResultBuilder builder() { return new ResultBuilder(); }
    public static class ResultBuilder {
        private final Result r = new Result();
        public ResultBuilder attempt(ExamAttempt v)        { r.attempt = v;            return this; }
        public ResultBuilder user(User v)                  { r.user = v;               return this; }
        public ResultBuilder totalScore(double v)          { r.totalScore = v;         return this; }
        public ResultBuilder maxScore(double v)            { r.maxScore = v;           return this; }
        public ResultBuilder correctCount(int v)           { r.correctCount = v;       return this; }
        public ResultBuilder wrongCount(int v)             { r.wrongCount = v;         return this; }
        public ResultBuilder unattemptedCount(int v)       { r.unattemptedCount = v;   return this; }
        public ResultBuilder negativeMarks(double v)       { r.negativeMarks = v;      return this; }
        public ResultBuilder accuracyPercentage(double v)  { r.accuracyPercentage = v; return this; }
        public ResultBuilder percentile(double v)          { r.percentile = v;         return this; }
        public ResultBuilder girScore(double v)            { r.girScore = v;           return this; }
        public ResultBuilder girCorrect(int v)             { r.girCorrect = v;         return this; }
        public ResultBuilder girWrong(int v)               { r.girWrong = v;           return this; }
        public ResultBuilder girUnattempted(int v)         { r.girUnattempted = v;     return this; }
        public ResultBuilder girAccuracy(double v)         { r.girAccuracy = v;        return this; }
        public ResultBuilder gaScore(double v)             { r.gaScore = v;            return this; }
        public ResultBuilder gaCorrect(int v)              { r.gaCorrect = v;          return this; }
        public ResultBuilder gaWrong(int v)                { r.gaWrong = v;            return this; }
        public ResultBuilder gaUnattempted(int v)          { r.gaUnattempted = v;      return this; }
        public ResultBuilder gaAccuracy(double v)          { r.gaAccuracy = v;         return this; }
        public ResultBuilder qaScore(double v)             { r.qaScore = v;            return this; }
        public ResultBuilder qaCorrect(int v)              { r.qaCorrect = v;          return this; }
        public ResultBuilder qaWrong(int v)                { r.qaWrong = v;            return this; }
        public ResultBuilder qaUnattempted(int v)          { r.qaUnattempted = v;      return this; }
        public ResultBuilder qaAccuracy(double v)          { r.qaAccuracy = v;         return this; }
        public ResultBuilder ecScore(double v)             { r.ecScore = v;            return this; }
        public ResultBuilder ecCorrect(int v)              { r.ecCorrect = v;          return this; }
        public ResultBuilder ecWrong(int v)                { r.ecWrong = v;            return this; }
        public ResultBuilder ecUnattempted(int v)          { r.ecUnattempted = v;      return this; }
        public ResultBuilder ecAccuracy(double v)          { r.ecAccuracy = v;         return this; }
        public ResultBuilder totalTimeSeconds(int v)       { r.totalTimeSeconds = v;   return this; }
        public Result build()                              { return r; }
    }
}
