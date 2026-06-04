package com.ssccgl.exam.dto;

public class SectionProgressDto {
    private String sectionName;
    private int total;
    private int answered;
    private int notAnswered;
    private int notVisited;
    private int markedForReview;
    private int answeredMarked;
    private int startPosition;
    private String DisplayName;
    
    public String getDisplayName() {
		return DisplayName;
	}
	public void setDisplayName(String displayName) {
		DisplayName = displayName;
	}
	public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getAnswered() { return answered; }
    public void setAnswered(int answered) { this.answered = answered; }

    public int getNotAnswered() { return notAnswered; }
    public void setNotAnswered(int notAnswered) { this.notAnswered = notAnswered; }

    public int getNotVisited() { return notVisited; }
    public void setNotVisited(int notVisited) { this.notVisited = notVisited; }

    public int getMarkedForReview() { return markedForReview; }
    public void setMarkedForReview(int markedForReview) { this.markedForReview = markedForReview; }

    public int getAnsweredMarked() { return answeredMarked; }
    public void setAnsweredMarked(int answeredMarked) { this.answeredMarked = answeredMarked; }

    public int getStartPosition() { return startPosition; }
    public void setStartPosition(int startPosition) { this.startPosition = startPosition; }

    public double getCompletionPercentage() {
        if (total == 0) return 0;
        return (double)(answered + answeredMarked) / total * 100;
    }
}
