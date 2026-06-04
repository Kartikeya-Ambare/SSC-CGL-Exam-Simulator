package com.ssccgl.exam.controller;

import com.ssccgl.exam.entity.ExamSession;
import com.ssccgl.exam.entity.User;
import com.ssccgl.exam.security.CustomUserDetailsService.CustomUserDetails;
import com.ssccgl.exam.service.ExamService;
import com.ssccgl.exam.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class ExamApiController {

    private static final Logger logger = LoggerFactory.getLogger(ExamApiController.class);
    private static final int EXAM_DURATION_SECONDS = 3600;

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    @PostMapping("/{sessionId}/save-answer")
    public ResponseEntity<Map<String, Object>> saveAnswer(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findById(userDetails.getUserId());
            ExamSession session = examService.getSessionById(sessionId);

            if (session == null || !session.getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Invalid session");
                return ResponseEntity.badRequest().body(response);
            }

            if (session.getStatus() != ExamSession.Status.IN_PROGRESS) {
                response.put("success", false);
                response.put("message", "Exam already submitted");
                response.put("redirect", "/exam/" + sessionId + "/result");
                return ResponseEntity.ok(response);
            }

            // Check time
            long elapsed = ChronoUnit.SECONDS.between(session.getStartTime(), LocalDateTime.now());
            int remaining = (int) Math.max(0, EXAM_DURATION_SECONDS - elapsed);

            if (remaining <= 0) {
                examService.autoSubmitExam(session);
                response.put("success", false);
                response.put("timeUp", true);
                response.put("redirect", "/exam/" + sessionId + "/result");
                return ResponseEntity.ok(response);
            }

            Long examQuestionId = Long.parseLong(payload.get("examQuestionId").toString());
            Integer selectedOption = payload.get("selectedOption") != null
                ? Integer.parseInt(payload.get("selectedOption").toString()) : null;
            String action = payload.getOrDefault("action", "SAVE").toString();
            Long timeTaken = payload.get("timeTaken") != null
                ? Long.parseLong(payload.get("timeTaken").toString()) : null;

            examService.saveAnswer(session, examQuestionId, selectedOption, action, timeTaken);

            // Update position
            if (payload.containsKey("currentPosition")) {
                int pos = Integer.parseInt(payload.get("currentPosition").toString());
                String sec = payload.getOrDefault("currentSection", "GENERAL_AWARENESS").toString();
                examService.updateSessionPosition(sessionId, pos, sec);
            }

            response.put("success", true);
            response.put("timeRemaining", remaining);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Save answer error for session {}: {}", sessionId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{sessionId}/time")
    public ResponseEntity<Map<String, Object>> getTimeRemaining(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findById(userDetails.getUserId());
            ExamSession session = examService.getSessionById(sessionId);

            if (session == null || !session.getUser().getId().equals(user.getId())) {
                response.put("error", "Invalid session");
                return ResponseEntity.badRequest().body(response);
            }

            if (session.getStatus() != ExamSession.Status.IN_PROGRESS) {
                response.put("timeRemaining", 0);
                response.put("submitted", true);
                return ResponseEntity.ok(response);
            }

            long elapsed = ChronoUnit.SECONDS.between(session.getStartTime(), LocalDateTime.now());
            int remaining = (int) Math.max(0, EXAM_DURATION_SECONDS - elapsed);

            if (remaining <= 0) {
                examService.autoSubmitExam(session);
                response.put("timeRemaining", 0);
                response.put("timeUp", true);
                response.put("redirect", "/exam/" + sessionId + "/result");
            } else {
                response.put("timeRemaining", remaining);
                response.put("submitted", false);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/{sessionId}/auto-submit")
    public ResponseEntity<Map<String, Object>> autoSubmit(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findById(userDetails.getUserId());
            ExamSession session = examService.getSessionById(sessionId);

            if (session != null && session.getUser().getId().equals(user.getId())) {
                if (session.getStatus() == ExamSession.Status.IN_PROGRESS) {
                    examService.autoSubmitExam(session);
                }
            }

            response.put("success", true);
            response.put("redirect", "/exam/" + sessionId + "/result");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{sessionId}/progress")
    public ResponseEntity<Map<String, Object>> getProgress(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findById(userDetails.getUserId());
            ExamSession session = examService.getSessionById(sessionId);

            if (session == null || !session.getUser().getId().equals(user.getId())) {
                response.put("error", "Invalid session");
                return ResponseEntity.badRequest().body(response);
            }

            var examState = examService.getExamState(session);
            response.put("totalAnswered", examState.getTotalAnswered());
            response.put("totalNotAnswered", examState.getTotalNotAnswered());
            response.put("totalNotVisited", examState.getTotalNotVisited());
            response.put("totalMarkedForReview", examState.getTotalMarkedForReview());
            response.put("totalAnsweredMarked", examState.getTotalAnsweredMarked());
            response.put("timeRemaining", examState.getTimeRemainingSeconds());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
