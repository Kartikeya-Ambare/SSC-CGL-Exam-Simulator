package com.ssccgl.exam.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_questions", indexes = {
    @Index(name = "idx_exam_questions_session", columnList = "session_id"),
    @Index(name = "idx_exam_questions_question", columnList = "question_id"),
    @Index(name = "idx_exam_questions_position", columnList = "session_id, position")
})
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ExamSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Question.Section section;

    // Shuffled option mapping: stores indices of original options in shuffled order
    // e.g., "2,0,3,1" means shuffled[0]=original[2], shuffled[1]=original[0], etc.
    @Column(name = "option_shuffle_map", length = 20)
    private String optionShuffleMap;

    // The correct answer index in the SHUFFLED order
    @Column(name = "shuffled_correct_answer")
    private int shuffledCorrectAnswer;

    @Column(name = "section_position")
    private int sectionPosition;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExamSession getSession() { return session; }
    public void setSession(ExamSession session) { this.session = session; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public Question.Section getSection() { return section; }
    public void setSection(Question.Section section) { this.section = section; }

    public String getOptionShuffleMap() { return optionShuffleMap; }
    public void setOptionShuffleMap(String optionShuffleMap) { this.optionShuffleMap = optionShuffleMap; }

    public int getShuffledCorrectAnswer() { return shuffledCorrectAnswer; }
    public void setShuffledCorrectAnswer(int shuffledCorrectAnswer) { this.shuffledCorrectAnswer = shuffledCorrectAnswer; }

    public int getSectionPosition() { return sectionPosition; }
    public void setSectionPosition(int sectionPosition) { this.sectionPosition = sectionPosition; }

    public int[] getShuffleMapArray() {
        if (optionShuffleMap == null || optionShuffleMap.isEmpty()) {
            return new int[]{0, 1, 2, 3};
        }
        String[] parts = optionShuffleMap.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    public String getShuffledOption(int shuffledIndex) {
        int[] map = getShuffleMapArray();
        if (shuffledIndex < 0 || shuffledIndex >= map.length) return "";
        return question.getOption(map[shuffledIndex]);
    }
}
