package com.ssccgl.exam.dto;

import java.util.List;

public class QuestionDto {
    private Long examQuestionId;
    private int position;
    private String section;
    private int sectionPosition;
    private String questionText;
    private List<String> options;
    private String topic;
    private String difficulty;

    public Long getExamQuestionId() { return examQuestionId; }
    public void setExamQuestionId(Long examQuestionId) { this.examQuestionId = examQuestionId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public int getSectionPosition() { return sectionPosition; }
    public void setSectionPosition(int sectionPosition) { this.sectionPosition = sectionPosition; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}
