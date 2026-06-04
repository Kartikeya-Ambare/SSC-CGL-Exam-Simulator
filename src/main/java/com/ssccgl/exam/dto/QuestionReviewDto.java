package com.ssccgl.exam.dto;

import java.util.List;

public class QuestionReviewDto {
    private int position;
    private String section;
    private String sectionDisplayName;
    private String questionText;
    private List<String> options;
    private Integer selectedOption;
    private int correctOption;
    private String status;
    private double marksObtained;
    private String explanation;
    private String topic;
    private boolean correct;
    private boolean attempted;

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getSectionDisplayName() { return sectionDisplayName; }
    public void setSectionDisplayName(String sectionDisplayName) { this.sectionDisplayName = sectionDisplayName; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public Integer getSelectedOption() { return selectedOption; }
    public void setSelectedOption(Integer selectedOption) { this.selectedOption = selectedOption; }

    public int getCorrectOption() { return correctOption; }
    public void setCorrectOption(int correctOption) { this.correctOption = correctOption; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public boolean isAttempted() { return attempted; }
    public void setAttempted(boolean attempted) { this.attempted = attempted; }
}
