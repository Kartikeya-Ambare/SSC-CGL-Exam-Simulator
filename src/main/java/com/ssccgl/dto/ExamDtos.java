package com.ssccgl.dto;
 
import com.ssccgl.enums.QuestionStatus;
import com.ssccgl.enums.Section;
 
import java.util.List;
import java.util.Map;
 
/**
 * Container for all exam-related DTOs, exposed as static nested classes
 * (e.g. {@code ExamDtos.QuestionFormDto}). Keeping them here is legal Java
 * because the file now has exactly one public top-level type: ExamDtos.
 */
public class ExamDtos {
 
// ── Question DTO ──────────────────────────────────────────
    public static class QuestionDto {
    private Long examQuestionId;
    private int displayOrder;
    private Section section;
    private String topic;
    private String questionText;
    private List<OptionDto> options;
    private String selectedOption;
    private QuestionStatus status;
    private boolean hasMath;
 
    public QuestionDto() {}
    public Long getExamQuestionId()        { return examQuestionId; }
    public int getDisplayOrder()           { return displayOrder; }
    public Section getSection()            { return section; }
    public String getTopic()               { return topic; }
    public String getQuestionText()        { return questionText; }
    public List<OptionDto> getOptions()    { return options; }
    public String getSelectedOption()      { return selectedOption; }
    public QuestionStatus getStatus()      { return status; }
    public boolean isHasMath()             { return hasMath; }
 
    public void setExamQuestionId(Long v)       { this.examQuestionId = v; }
    public void setDisplayOrder(int v)          { this.displayOrder = v; }
    public void setSection(Section v)           { this.section = v; }
    public void setTopic(String v)              { this.topic = v; }
    public void setQuestionText(String v)       { this.questionText = v; }
    public void setOptions(List<OptionDto> v)   { this.options = v; }
    public void setSelectedOption(String v)     { this.selectedOption = v; }
    public void setStatus(QuestionStatus v)     { this.status = v; }
    public void setHasMath(boolean v)           { this.hasMath = v; }
 
    public static QuestionDtoBuilder builder() { return new QuestionDtoBuilder(); }
    public static class QuestionDtoBuilder {
        private final QuestionDto d = new QuestionDto();
        public QuestionDtoBuilder examQuestionId(Long v)       { d.examQuestionId = v; return this; }
        public QuestionDtoBuilder displayOrder(int v)          { d.displayOrder = v;   return this; }
        public QuestionDtoBuilder section(Section v)           { d.section = v;        return this; }
        public QuestionDtoBuilder topic(String v)              { d.topic = v;          return this; }
        public QuestionDtoBuilder questionText(String v)       { d.questionText = v;   return this; }
        public QuestionDtoBuilder options(List<OptionDto> v)   { d.options = v;        return this; }
        public QuestionDtoBuilder selectedOption(String v)     { d.selectedOption = v; return this; }
        public QuestionDtoBuilder status(QuestionStatus v)     { d.status = v;         return this; }
        public QuestionDtoBuilder hasMath(boolean v)           { d.hasMath = v;        return this; }
        public QuestionDto build()                             { return d; }
    }
}
 
// ── Option DTO ────────────────────────────────────────────
    public static class OptionDto {
    private String displayLabel;
    private String originalLetter;
    private String text;
 
    public OptionDto() {}
    public OptionDto(String displayLabel, String originalLetter, String text) {
        this.displayLabel   = displayLabel;
        this.originalLetter = originalLetter;
        this.text           = text;
    }
    public String getDisplayLabel()   { return displayLabel; }
    public String getOriginalLetter() { return originalLetter; }
    public String getText()           { return text; }
    public void setDisplayLabel(String v)   { this.displayLabel = v; }
    public void setOriginalLetter(String v) { this.originalLetter = v; }
    public void setText(String v)           { this.text = v; }
}
 
// ── Exam State DTO ────────────────────────────────────────
    public static class ExamStateDto {
    private Long attemptId;
    private Long examId;
    private String examTitle;
    private int totalQuestions;
    private int durationMinutes;
    private int remainingSeconds;
    private int currentQuestionIndex;
    private List<QuestionDto> questions;
    private Map<Integer, QuestionStatus> questionStatusMap;
    private Map<Section, Integer> sectionStartIndex;
 
    public ExamStateDto() {}
    public Long getAttemptId()                           { return attemptId; }
    public Long getExamId()                              { return examId; }
    public String getExamTitle()                         { return examTitle; }
    public int getTotalQuestions()                       { return totalQuestions; }
    public int getDurationMinutes()                      { return durationMinutes; }
    public int getRemainingSeconds()                     { return remainingSeconds; }
    public int getCurrentQuestionIndex()                 { return currentQuestionIndex; }
    public List<QuestionDto> getQuestions()              { return questions; }
    public Map<Integer,QuestionStatus> getQuestionStatusMap() { return questionStatusMap; }
    public Map<Section,Integer> getSectionStartIndex()   { return sectionStartIndex; }
 
    public void setAttemptId(Long v)                          { this.attemptId = v; }
    public void setExamId(Long v)                             { this.examId = v; }
    public void setExamTitle(String v)                        { this.examTitle = v; }
    public void setTotalQuestions(int v)                      { this.totalQuestions = v; }
    public void setDurationMinutes(int v)                     { this.durationMinutes = v; }
    public void setRemainingSeconds(int v)                    { this.remainingSeconds = v; }
    public void setCurrentQuestionIndex(int v)                { this.currentQuestionIndex = v; }
    public void setQuestions(List<QuestionDto> v)             { this.questions = v; }
    public void setQuestionStatusMap(Map<Integer,QuestionStatus> v){ this.questionStatusMap = v; }
    public void setSectionStartIndex(Map<Section,Integer> v)  { this.sectionStartIndex = v; }
}
 
// ── Save Answer Request ───────────────────────────────────
    public static class SaveAnswerRequest {
    private Long attemptId;
    private Long examQuestionId;
    private String selectedOption;
    private QuestionStatus questionStatus;
    private int timeSpentSeconds;
    private int remainingSeconds;
 
    public SaveAnswerRequest() {}
    public Long getAttemptId()             { return attemptId; }
    public Long getExamQuestionId()        { return examQuestionId; }
    public String getSelectedOption()      { return selectedOption; }
    public QuestionStatus getQuestionStatus(){ return questionStatus; }
    public int getTimeSpentSeconds()       { return timeSpentSeconds; }
    public int getRemainingSeconds()       { return remainingSeconds; }
 
    public void setAttemptId(Long v)               { this.attemptId = v; }
    public void setExamQuestionId(Long v)          { this.examQuestionId = v; }
    public void setSelectedOption(String v)        { this.selectedOption = v; }
    public void setQuestionStatus(QuestionStatus v){ this.questionStatus = v; }
    public void setTimeSpentSeconds(int v)         { this.timeSpentSeconds = v; }
    public void setRemainingSeconds(int v)         { this.remainingSeconds = v; }
}
 
// ── Palette Item ──────────────────────────────────────────
    public static class PaletteItemDto {
    private int displayOrder;
    private QuestionStatus status;
    private Section section;
 
    public PaletteItemDto() {}
    public int getDisplayOrder()    { return displayOrder; }
    public QuestionStatus getStatus(){ return status; }
    public Section getSection()     { return section; }
    public void setDisplayOrder(int v)      { this.displayOrder = v; }
    public void setStatus(QuestionStatus v) { this.status = v; }
    public void setSection(Section v)       { this.section = v; }
}
 
// ── Result DTO ────────────────────────────────────────────
    public static class ResultDto {
    private Long resultId;
    private Long attemptId;
    private String examTitle;
    private double totalScore;
    private double maxScore;
    private int correctCount;
    private int wrongCount;
    private int unattemptedCount;
    private double negativeMarks;
    private double accuracyPercentage;
    private double percentile;
    private int totalTimeSeconds;
    private Map<String, SectionResultDto> sectionResults;
    private List<QuestionReviewDto> questionReviews;
 
    public ResultDto() {}
    public Long getResultId()          { return resultId; }
    public Long getAttemptId()         { return attemptId; }
    public String getExamTitle()       { return examTitle; }
    public double getTotalScore()      { return totalScore; }
    public double getMaxScore()        { return maxScore; }
    public int getCorrectCount()       { return correctCount; }
    public int getWrongCount()         { return wrongCount; }
    public int getUnattemptedCount()   { return unattemptedCount; }
    public double getNegativeMarks()   { return negativeMarks; }
    public double getAccuracyPercentage(){ return accuracyPercentage; }
    public double getPercentile()      { return percentile; }
    public int getTotalTimeSeconds()   { return totalTimeSeconds; }
    public Map<String,SectionResultDto> getSectionResults() { return sectionResults; }
    public List<QuestionReviewDto> getQuestionReviews()     { return questionReviews; }
 
    public void setResultId(Long v)                    { this.resultId = v; }
    public void setAttemptId(Long v)                   { this.attemptId = v; }
    public void setExamTitle(String v)                 { this.examTitle = v; }
    public void setTotalScore(double v)                { this.totalScore = v; }
    public void setMaxScore(double v)                  { this.maxScore = v; }
    public void setCorrectCount(int v)                 { this.correctCount = v; }
    public void setWrongCount(int v)                   { this.wrongCount = v; }
    public void setUnattemptedCount(int v)             { this.unattemptedCount = v; }
    public void setNegativeMarks(double v)             { this.negativeMarks = v; }
    public void setAccuracyPercentage(double v)        { this.accuracyPercentage = v; }
    public void setPercentile(double v)                { this.percentile = v; }
    public void setTotalTimeSeconds(int v)             { this.totalTimeSeconds = v; }
    public void setSectionResults(Map<String,SectionResultDto> v){ this.sectionResults = v; }
    public void setQuestionReviews(List<QuestionReviewDto> v)    { this.questionReviews = v; }
}
 
    public static class SectionResultDto {
    private String sectionName;
    private double score;
    private int correct;
    private int wrong;
    private int unattempted;
    private double accuracy;
 
    public SectionResultDto() {}
    public String getSectionName() { return sectionName; }
    public double getScore()       { return score; }
    public int getCorrect()        { return correct; }
    public int getWrong()          { return wrong; }
    public int getUnattempted()    { return unattempted; }
    public double getAccuracy()    { return accuracy; }
    public void setSectionName(String v) { this.sectionName = v; }
    public void setScore(double v)       { this.score = v; }
    public void setCorrect(int v)        { this.correct = v; }
    public void setWrong(int v)          { this.wrong = v; }
    public void setUnattempted(int v)    { this.unattempted = v; }
    public void setAccuracy(double v)    { this.accuracy = v; }
}
 
    public static class QuestionReviewDto {
    private int displayOrder;
    private String questionText;
    private String selectedOption;
    private String correctAnswer;
    private boolean isCorrect;
    private double marksEarned;
    private String explanation;
    private Section section;
    private String topic;
 
    public QuestionReviewDto() {}
    public int getDisplayOrder()    { return displayOrder; }
    public String getQuestionText() { return questionText; }
    public String getSelectedOption(){ return selectedOption; }
    public String getCorrectAnswer(){ return correctAnswer; }
    public boolean isCorrect()      { return isCorrect; }
    public double getMarksEarned()  { return marksEarned; }
    public String getExplanation()  { return explanation; }
    public Section getSection()     { return section; }
    public String getTopic()        { return topic; }
    public void setDisplayOrder(int v)      { this.displayOrder = v; }
    public void setQuestionText(String v)   { this.questionText = v; }
    public void setSelectedOption(String v) { this.selectedOption = v; }
    public void setCorrectAnswer(String v)  { this.correctAnswer = v; }
    public void setCorrect(boolean v)       { this.isCorrect = v; }
    public void setMarksEarned(double v)    { this.marksEarned = v; }
    public void setExplanation(String v)    { this.explanation = v; }
    public void setSection(Section v)       { this.section = v; }
    public void setTopic(String v)          { this.topic = v; }
}
 
    public static class DashboardDto {
    private String userName;
    private long totalAttempts;
    private double averageScore;
    private double highestScore;
    private double averageAccuracy;
    private List<ResultDto> recentResults;
    private java.util.Map<Section, Double> sectionAverages;
    private List<Double> scoreTrend;
    private List<Double> accuracyTrend;
 
    public DashboardDto() {}
    public String getUserName()         { return userName; }
    public long getTotalAttempts()      { return totalAttempts; }
    public double getAverageScore()     { return averageScore; }
    public double getHighestScore()     { return highestScore; }
    public double getAverageAccuracy()  { return averageAccuracy; }
    public List<ResultDto> getRecentResults(){ return recentResults; }
    public java.util.Map<Section,Double> getSectionAverages(){ return sectionAverages; }
    public List<Double> getScoreTrend()    { return scoreTrend; }
    public List<Double> getAccuracyTrend() { return accuracyTrend; }
    public void setUserName(String v)            { this.userName = v; }
    public void setTotalAttempts(long v)         { this.totalAttempts = v; }
    public void setAverageScore(double v)        { this.averageScore = v; }
    public void setHighestScore(double v)        { this.highestScore = v; }
    public void setAverageAccuracy(double v)     { this.averageAccuracy = v; }
    public void setRecentResults(List<ResultDto> v){ this.recentResults = v; }
    public void setSectionAverages(java.util.Map<Section,Double> v){ this.sectionAverages = v; }
    public void setScoreTrend(List<Double> v)    { this.scoreTrend = v; }
    public void setAccuracyTrend(List<Double> v) { this.accuracyTrend = v; }
}
 
// ── Question Form DTO (Admin) ─────────────────────────────
    public static class QuestionFormDto {
    private Long id;
    private String section;
    private String topic;
    private String difficulty;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String explanation;
    private String source;
    private Integer examYear;
 
    public QuestionFormDto() {}
    public Long getId()             { return id; }
    public String getSection()      { return section; }
    public String getTopic()        { return topic; }
    public String getDifficulty()   { return difficulty; }
    public String getQuestionText() { return questionText; }
    public String getOptionA()      { return optionA; }
    public String getOptionB()      { return optionB; }
    public String getOptionC()      { return optionC; }
    public String getOptionD()      { return optionD; }
    public String getCorrectAnswer(){ return correctAnswer; }
    public String getExplanation()  { return explanation; }
    public String getSource()       { return source; }
    public Integer getExamYear()    { return examYear; }
 
    public void setId(Long v)             { this.id = v; }
    public void setSection(String v)      { this.section = v; }
    public void setTopic(String v)        { this.topic = v; }
    public void setDifficulty(String v)   { this.difficulty = v; }
    public void setQuestionText(String v) { this.questionText = v; }
    public void setOptionA(String v)      { this.optionA = v; }
    public void setOptionB(String v)      { this.optionB = v; }
    public void setOptionC(String v)      { this.optionC = v; }
    public void setOptionD(String v)      { this.optionD = v; }
    public void setCorrectAnswer(String v){ this.correctAnswer = v; }
    public void setExplanation(String v)  { this.explanation = v; }
    public void setSource(String v)       { this.source = v; }
    public void setExamYear(Integer v)    { this.examYear = v; }
}
}
 