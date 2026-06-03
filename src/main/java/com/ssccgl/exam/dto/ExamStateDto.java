package com.ssccgl.exam.dto;

import java.util.List;
import java.util.Map;

public class ExamStateDto {
    private Long sessionId;
    private int currentPosition;
    private int timeRemainingSeconds;
    private List<QuestionDto> questions;
    private Map<Integer, AnswerDto> answers;
    private Map<String, SectionProgressDto> sectionProgress;
    private int totalAnswered;
    private int totalNotAnswered;
    private int totalNotVisited;
    private int totalMarkedForReview;
    private int totalAnsweredMarked;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public int getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }

    public int getTimeRemainingSeconds() { return timeRemainingSeconds; }
    public void setTimeRemainingSeconds(int timeRemainingSeconds) { this.timeRemainingSeconds = timeRemainingSeconds; }

    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }

    public Map<Integer, AnswerDto> getAnswers() { return answers; }
    public void setAnswers(Map<Integer, AnswerDto> answers) { this.answers = answers; }

    public Map<String, SectionProgressDto> getSectionProgress() { return sectionProgress; }
    public void setSectionProgress(Map<String, SectionProgressDto> sectionProgress) { this.sectionProgress = sectionProgress; }

    public int getTotalAnswered() { return totalAnswered; }
    public void setTotalAnswered(int totalAnswered) { this.totalAnswered = totalAnswered; }

    public int getTotalNotAnswered() { return totalNotAnswered; }
    public void setTotalNotAnswered(int totalNotAnswered) { this.totalNotAnswered = totalNotAnswered; }

    public int getTotalNotVisited() { return totalNotVisited; }
    public void setTotalNotVisited(int totalNotVisited) { this.totalNotVisited = totalNotVisited; }

    public int getTotalMarkedForReview() { return totalMarkedForReview; }
    public void setTotalMarkedForReview(int totalMarkedForReview) { this.totalMarkedForReview = totalMarkedForReview; }

    public int getTotalAnsweredMarked() { return totalAnsweredMarked; }
    public void setTotalAnsweredMarked(int totalAnsweredMarked) { this.totalAnsweredMarked = totalAnsweredMarked; }
}
