package com.ssccgl.exam.dto;

public class AnswerDto {
    private Long examQuestionId;
    private Integer selectedOption;
    private String status;
    private boolean markedForReview;

    public Long getExamQuestionId() { return examQuestionId; }
    public void setExamQuestionId(Long examQuestionId) { this.examQuestionId = examQuestionId; }

    public Integer getSelectedOption() { return selectedOption; }
    public void setSelectedOption(Integer selectedOption) { this.selectedOption = selectedOption; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isMarkedForReview() { return markedForReview; }
    public void setMarkedForReview(boolean markedForReview) { this.markedForReview = markedForReview; }
}
