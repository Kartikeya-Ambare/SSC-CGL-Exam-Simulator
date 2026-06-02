package com.ssccgl.service;

import com.ssccgl.entity.*;
import com.ssccgl.enums.ExamStatus;
import com.ssccgl.enums.QuestionStatus;
import com.ssccgl.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ExamSessionService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExamSessionService.class);

    public ExamSessionService(ExamAttemptRepository attemptRepository, UserResponseRepository responseRepository, ExamQuestionRepository examQuestionRepository, ResultProcessingService resultProcessingService) {
        this.attemptRepository = attemptRepository;
        this.responseRepository = responseRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.resultProcessingService = resultProcessingService;
    }


    private final ExamAttemptRepository attemptRepository;
    private final UserResponseRepository responseRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ResultProcessingService resultProcessingService;

    /**
     * Save or update a user's response to a single question.
     * Called via AJAX on every answer selection, mark-for-review, or clear.
     */
    public void saveResponse(Long attemptId, Long examQuestionId,
                             String selectedOption, QuestionStatus status,
                             int timeSpent, int remainingSeconds) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        if (attempt.getStatus() != ExamStatus.IN_PROGRESS) {
            log.warn("Save attempt on non-active attempt {}", attemptId);
            return;
        }

        ExamQuestion eq = examQuestionRepository.findById(examQuestionId)
            .orElseThrow(() -> new RuntimeException("ExamQuestion not found: " + examQuestionId));

        // Upsert response
        UserResponse response = responseRepository
            .findByAttemptAndExamQuestion_Id(attempt, examQuestionId)
            .orElseGet(() -> UserResponse.builder()
                .attempt(attempt)
                .examQuestion(eq)
                .build());

        response.setSelectedOption(selectedOption);
        response.setQuestionStatus(status);
        response.setTimeSpentSeconds(response.getTimeSpentSeconds() + timeSpent);
        responseRepository.save(response);

        // Update remaining time for recovery
        attempt.setRemainingSeconds(remainingSeconds);
        attemptRepository.save(attempt);
    }

    /**
     * Update just the timer (periodic heartbeat from frontend).
     */
    public void updateTimer(Long attemptId, int remainingSeconds) {
        attemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setRemainingSeconds(remainingSeconds);
            attemptRepository.save(attempt);
        });
    }

    /**
     * Track tab switches (anti-cheat).
     */
    public void recordTabSwitch(Long attemptId) {
        attemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setTabSwitchCount(attempt.getTabSwitchCount() + 1);
            attemptRepository.save(attempt);
            log.info("Tab switch #{} recorded for attempt {}",
                attempt.getTabSwitchCount(), attemptId);
        });
    }

    /**
     * Submit exam (user-initiated or timer expiry).
     */
    public Result submitExam(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        if (attempt.getStatus() == ExamStatus.SUBMITTED) {
            log.warn("Double submit attempt for {}", attemptId);
            return null;
        }

        return resultProcessingService.processResult(attempt);
    }

    /**
     * Auto-submit when timer reaches zero.
     */
    public Result autoSubmit(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        attempt.setStatus(ExamStatus.TIMED_OUT);
        return resultProcessingService.processResult(attempt);
    }

    /**
     * Recover exam state after browser refresh.
     * Returns null if attempt is expired or not in-progress.
     */
    @Transactional(readOnly = true)
    public Optional<ExamAttempt> recoverSession(Long attemptId, Long userId) {
        return attemptRepository.findById(attemptId)
            .filter(a -> a.getUser().getId().equals(userId))
            .filter(a -> a.getStatus() == ExamStatus.IN_PROGRESS)
            .filter(a -> a.getRemainingSeconds() > 0);
    }
}
