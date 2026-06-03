package com.ssccgl.exam.dto;

public class SectionScoreDto {
    private String sectionName;
    private String displayName;
    private int correct;
    private int incorrect;
    private int unattempted;
    private int attempted;
    private double score;
    private double maxScore;
    private double accuracy;

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public int getCorrect() { return correct; }
    public void setCorrect(int correct) { this.correct = correct; }

    public int getIncorrect() { return incorrect; }
    public void setIncorrect(int incorrect) { this.incorrect = incorrect; }

    public int getUnattempted() { return unattempted; }
    public void setUnattempted(int unattempted) { this.unattempted = unattempted; }

    public int getAttempted() { return attempted; }
    public void setAttempted(int attempted) { this.attempted = attempted; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public double getScorePercentage() {
        if (maxScore == 0) return 0;
        return score / maxScore * 100;
    }
}
