package com.ssccgl.exam.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ResultDto {
    private Long sessionId;
    private Long resultId;
    private String candidateName;
    private double totalScore;
    private double maxMarks;
    private int totalCorrect;
    private int totalIncorrect;
    private int totalUnattempted;
    private int totalAttempted;
    private double accuracyPercentage;
    private double attemptPercentage;
    private double percentile;
    private int timeTakenSeconds;
    private int attemptNumber;
    private LocalDateTime submittedAt;
    private Map<String, SectionScoreDto> sectionScores;
    private List<QuestionReviewDto> questionReviews;
    private boolean autoSubmitted;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

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

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Map<String, SectionScoreDto> getSectionScores() { return sectionScores; }
    public void setSectionScores(Map<String, SectionScoreDto> sectionScores) { this.sectionScores = sectionScores; }

    public List<QuestionReviewDto> getQuestionReviews() { return questionReviews; }
    public void setQuestionReviews(List<QuestionReviewDto> questionReviews) { this.questionReviews = questionReviews; }

    public boolean isAutoSubmitted() { return autoSubmitted; }
    public void setAutoSubmitted(boolean autoSubmitted) { this.autoSubmitted = autoSubmitted; }

    public String getTimeTakenFormatted() {
        int h = timeTakenSeconds / 3600;
        int m = (timeTakenSeconds % 3600) / 60;
        int s = timeTakenSeconds % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%02d:%02d", m, s);
    }

    public double getScorePercentage() {
        if (maxMarks == 0) return 0;
        return totalScore / maxMarks * 100;
    }
}
