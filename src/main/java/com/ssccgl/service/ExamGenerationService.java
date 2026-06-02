package com.ssccgl.service;

import com.ssccgl.entity.*;
import com.ssccgl.enums.Difficulty;
import com.ssccgl.enums.ExamStatus;
import com.ssccgl.enums.Section;
import com.ssccgl.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Exam Generation Engine
 *
 * Distribution per section (25 questions each):
 *   EASY   : 8  questions
 *   MEDIUM : 12 questions
 *   HARD   : 5  questions
 *
 * Total: 100 questions, 200 marks
 * Marking: +2 correct, -0.33 wrong, 0 unattempted
 */
@Service
@Transactional
public class ExamGenerationService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExamGenerationService.class);

    public ExamGenerationService(QuestionRepository questionRepository, ExamRepository examRepository, ExamQuestionRepository examQuestionRepository, ExamAttemptRepository attemptRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
    }


    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    @Value("${app.exam.duration-minutes:60}")
    private int durationMinutes;

    // Difficulty distribution per section
    private static final Map<Difficulty, Integer> DIFFICULTY_DIST = Map.of(
        Difficulty.EASY,   8,
        Difficulty.MEDIUM, 12,
        Difficulty.HARD,   5
    );

    /**
     * Generate a new exam for a user with fully randomized questions and options.
     */
    public ExamAttempt generateAndStartExam(User user, String title) {
        // Build the Exam header
        Exam exam = Exam.builder()
            .title(title)
            .totalQuestions(100)
            .totalMarks(200)
            .durationMinutes(durationMinutes)
            .marksPerCorrect(2.0)
            .negativeMarks(0.33)
            .active(true)
            .createdBy(user)
            .build();
        exam = examRepository.save(exam);

        // Select questions per section per difficulty
        List<ExamQuestion> examQuestions = new ArrayList<>();
        int displayOrder = 1;

        for (Section section : Section.values()) {
            List<Question> sectionQuestions = selectQuestionsForSection(section);
            // Shuffle question order within section
            Collections.shuffle(sectionQuestions);

            for (Question q : sectionQuestions) {
                String optionMapping = generateOptionMapping();
                ExamQuestion eq = ExamQuestion.builder()
                    .exam(exam)
                    .question(q)
                    .displayOrder(displayOrder++)
                    .optionMapping(optionMapping)
                    .build();
                examQuestions.add(eq);
            }
        }

        // Optionally shuffle ALL questions across sections (for mixed-order)
        // Collections.shuffle(examQuestions);
        // Re-assign display orders after shuffle
        // for (int i = 0; i < examQuestions.size(); i++) examQuestions.get(i).setDisplayOrder(i+1);

        examQuestionRepository.saveAll(examQuestions);
        log.info("Generated exam '{}' with {} questions for user {}",
            title, examQuestions.size(), user.getEmail());

        // Create attempt
        ExamAttempt attempt = ExamAttempt.builder()
            .user(user)
            .exam(exam)
            .status(ExamStatus.IN_PROGRESS)
            .startedAt(LocalDateTime.now())
            .remainingSeconds(durationMinutes * 60)
            .currentQuestionIndex(0)
            .build();

        return attemptRepository.save(attempt);
    }

    /**
     * Select 25 questions for a section using difficulty distribution.
     * Falls back gracefully if bank has fewer questions.
     */
    private List<Question> selectQuestionsForSection(Section section) {
        List<Question> selected = new ArrayList<>();

        for (Map.Entry<Difficulty, Integer> entry : DIFFICULTY_DIST.entrySet()) {
            Difficulty diff   = entry.getKey();
            int needed        = entry.getValue();
            long available    = questionRepository.countBySectionAndDifficultyAndActiveTrue(section, diff);

            if (available == 0) {
                log.warn("No {} questions for section {}. Using MEDIUM fallback.", diff, section);
                // Fallback: pick from any difficulty
                List<Question> fallback = questionRepository.findRandomBySection(
                    section.name(), needed);
                selected.addAll(fallback);
            } else {
                int limit = (int) Math.min(needed, available);
                List<Question> qs = questionRepository.findRandomBySectionAndDifficulty(
                    section.name(), diff.name(), limit);
                selected.addAll(qs);

                // If fewer than needed, top up from same section
                if (qs.size() < needed) {
                    int gap = needed - qs.size();
                    List<Long> usedIds = selected.stream().map(Question::getId).toList();
                    List<Question> topUp = questionRepository.findRandomExcluding(
                        section.name(), diff.name(), usedIds, gap);
                    selected.addAll(topUp);
                }
            }
        }

        // Remove duplicates
        Map<Long, Question> deduped = new LinkedHashMap<>();
        selected.forEach(q -> deduped.put(q.getId(), q));
        List<Question> result = new ArrayList<>(deduped.values());

        // Ensure exactly 25
        if (result.size() > 25) result = result.subList(0, 25);

        log.debug("Selected {} questions for section {}", result.size(), section);
        return result;
    }

    /**
     * Generate shuffled option mapping string like "B,D,A,C"
     * Position 1→B, position 2→D, position 3→A, position 4→C
     */
    private String generateOptionMapping() {
        List<String> options = new ArrayList<>(List.of("A", "B", "C", "D"));
        Collections.shuffle(options);
        return String.join(",", options);
    }

    /**
     * Translate a displayed position (1-4) back to original letter (A-D)
     * given a mapping string like "B,D,A,C"
     */
    public static String displayPositionToOriginal(String mapping, int displayPosition) {
        String[] parts = mapping.split(",");
        return parts[displayPosition - 1]; // 1-indexed
    }

    /**
     * Translate original letter to displayed position given a mapping
     */
    public static int originalToDisplayPosition(String mapping, String originalLetter) {
        String[] parts = mapping.split(",");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase(originalLetter)) return i + 1;
        }
        return -1;
    }
}
