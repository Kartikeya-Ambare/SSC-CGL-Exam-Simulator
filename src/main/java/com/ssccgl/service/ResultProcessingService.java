package com.ssccgl.service;

import com.ssccgl.entity.*;
import com.ssccgl.enums.ExamStatus;
import com.ssccgl.enums.QuestionStatus;
import com.ssccgl.enums.Section;
import com.ssccgl.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResultProcessingService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResultProcessingService.class);

    public ResultProcessingService(UserResponseRepository responseRepository, ResultRepository resultRepository, ExamAttemptRepository attemptRepository) {
        this.responseRepository = responseRepository;
        this.resultRepository = resultRepository;
        this.attemptRepository = attemptRepository;
    }


    private final UserResponseRepository responseRepository;
    private final ResultRepository resultRepository;
    private final ExamAttemptRepository attemptRepository;

    /**
     * Process all responses for an attempt and generate a Result.
     * Called when the user submits or time expires.
     */
    public Result processResult(ExamAttempt attempt) {
        // Guard: don't reprocess
        if (attempt.getStatus() == ExamStatus.SUBMITTED ||
            attempt.getStatus() == ExamStatus.TIMED_OUT) {
            return resultRepository.findByAttemptId(attempt.getId()).orElse(null);
        }

        List<UserResponse> responses = responseRepository.findByAttempt(attempt);
        Exam exam = attempt.getExam();

        // ── Per-section accumulators ───────────────────────
        Map<Section, int[]> sectionStats = new EnumMap<>(Section.class);
        for (Section s : Section.values()) {
            // [correct, wrong, unattempted]
            sectionStats.put(s, new int[]{0, 0, 0});
        }

        int totalCorrect = 0, totalWrong = 0, totalUnattempted = 0;
        double totalNegative = 0.0;

        for (UserResponse r : responses) {
            Section section = r.getExamQuestion().getQuestion().getSection();
            int[] stats = sectionStats.get(section);

            if (r.getSelectedOption() == null || r.getSelectedOption().isBlank()) {
                totalUnattempted++;
                stats[2]++;
            } else if (r.isCorrect()) {
                totalCorrect++;
                stats[0]++;
            } else {
                totalWrong++;
                stats[1]++;
                totalNegative += exam.getNegativeMarks();
            }
        }

        // ── Score calculation ──────────────────────────────
        double rawScore    = totalCorrect * exam.getMarksPerCorrect();
        double totalScore  = Math.max(0, rawScore - totalNegative);
        double maxScore    = exam.getTotalMarks();
        double accuracy    = responses.isEmpty() ? 0 :
            ((double) totalCorrect / (totalCorrect + totalWrong)) * 100.0;
        if (totalCorrect + totalWrong == 0) accuracy = 0;

        // ── Time calculation ───────────────────────────────
        int totalTimeSec = 0;
        if (attempt.getStartedAt() != null) {
            LocalDateTime end = attempt.getSubmittedAt() != null
                ? attempt.getSubmittedAt() : LocalDateTime.now();
            totalTimeSec = (int) ChronoUnit.SECONDS.between(attempt.getStartedAt(), end);
        }

        // ── Percentile ────────────────────────────────────
        double percentile = calculatePercentile(totalScore);

        // ── Build result ───────────────────────────────────
        int[] gir = sectionStats.get(Section.GENERAL_INTELLIGENCE_REASONING);
        int[] ga  = sectionStats.get(Section.GENERAL_AWARENESS);
        int[] qa  = sectionStats.get(Section.QUANTITATIVE_APTITUDE);
        int[] ec  = sectionStats.get(Section.ENGLISH_COMPREHENSION);

        Result result = Result.builder()
            .attempt(attempt)
            .user(attempt.getUser())
            .totalScore(Math.round(totalScore * 100.0) / 100.0)
            .maxScore(maxScore)
            .correctCount(totalCorrect)
            .wrongCount(totalWrong)
            .unattemptedCount(totalUnattempted)
            .negativeMarks(Math.round(totalNegative * 100.0) / 100.0)
            .accuracyPercentage(Math.round(accuracy * 100.0) / 100.0)
            .percentile(percentile)
            .totalTimeSeconds(totalTimeSec)
            // GIR
            .girCorrect(gir[0]).girWrong(gir[1]).girUnattempted(gir[2])
            .girScore(sectionScore(gir[0], gir[1], exam))
            .girAccuracy(sectionAccuracy(gir[0], gir[1]))
            // GA
            .gaCorrect(ga[0]).gaWrong(ga[1]).gaUnattempted(ga[2])
            .gaScore(sectionScore(ga[0], ga[1], exam))
            .gaAccuracy(sectionAccuracy(ga[0], ga[1]))
            // QA
            .qaCorrect(qa[0]).qaWrong(qa[1]).qaUnattempted(qa[2])
            .qaScore(sectionScore(qa[0], qa[1], exam))
            .qaAccuracy(sectionAccuracy(qa[0], qa[1]))
            // EC
            .ecCorrect(ec[0]).ecWrong(ec[1]).ecUnattempted(ec[2])
            .ecScore(sectionScore(ec[0], ec[1], exam))
            .ecAccuracy(sectionAccuracy(ec[0], ec[1]))
            .build();

        Result saved = resultRepository.save(result);

        // Update attempt status
        attempt.setStatus(ExamStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        log.info("Result processed for attempt {}: score={}/{}, accuracy={}%",
            attempt.getId(), totalScore, maxScore, accuracy);
        return saved;
    }

    private double sectionScore(int correct, int wrong, Exam exam) {
        return Math.max(0, correct * exam.getMarksPerCorrect() -
                           wrong * exam.getNegativeMarks());
    }

    private double sectionAccuracy(int correct, int wrong) {
        if (correct + wrong == 0) return 0.0;
        return Math.round((double) correct / (correct + wrong) * 100.0 * 100.0) / 100.0;
    }

    private double calculatePercentile(double score) {
        long below = resultRepository.countResultsBelowScore(score);
        long total = resultRepository.countAllResults();
        if (total == 0) return 100.0;
        return Math.round((double) below / total * 100.0 * 100.0) / 100.0;
    }
}
