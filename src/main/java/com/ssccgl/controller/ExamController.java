package com.ssccgl.controller;

import com.ssccgl.entity.*;
import com.ssccgl.enums.QuestionStatus;
import com.ssccgl.enums.Section;
import com.ssccgl.repository.*;
import com.ssccgl.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exam")
public class ExamController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExamController.class);

    public ExamController(ExamGenerationService generationService, ExamSessionService sessionService, UserService userService, ResultRepository resultRepository, ExamAttemptRepository attemptRepository, ExamQuestionRepository eqRepository, UserResponseRepository responseRepository) {
        this.generationService = generationService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.resultRepository = resultRepository;
        this.attemptRepository = attemptRepository;
        this.eqRepository = eqRepository;
        this.responseRepository = responseRepository;
    }


    private final ExamGenerationService generationService;
    private final ExamSessionService    sessionService;
    private final UserService           userService;
    private final ResultRepository      resultRepository;
    private final ExamAttemptRepository attemptRepository;
    private final ExamQuestionRepository eqRepository;
    private final UserResponseRepository responseRepository;

    // ── Start New Exam ─────────────────────────────────────
    @PostMapping("/start")
    public String startExam(@AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttrs) {
        User user = userService.findByEmail(userDetails.getUsername());

        // Check if already in progress
        Optional<ExamAttempt> inProgress = attemptRepository.findInProgressByUser(user);
        if (inProgress.isPresent()) {
            return "redirect:/exam/" + inProgress.get().getId();
        }

        String title = "SSC CGL Mock Test - " +
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        ExamAttempt attempt = generationService.generateAndStartExam(user, title);
        return "redirect:/exam/" + attempt.getId();
    }

    // ── Exam Page ──────────────────────────────────────────
    @GetMapping("/{attemptId}")
    public String examPage(@PathVariable Long attemptId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = userService.findByEmail(userDetails.getUsername());

        ExamAttempt attempt = sessionService.recoverSession(attemptId, user.getId())
            .orElse(null);

        if (attempt == null) {
            // Check if submitted - redirect to result
            return attemptRepository.findById(attemptId)
                .map(a -> "redirect:/exam/result/" + attemptId)
                .orElse("redirect:/dashboard");
        }

        Exam exam = attempt.getExam();
        List<ExamQuestion> eqs = eqRepository.findByExamOrderByDisplayOrder(exam);
        List<UserResponse> responses = responseRepository.findByAttempt(attempt);

        // Map: examQuestionId → userResponse
        Map<Long, UserResponse> responseMap = responses.stream()
            .collect(Collectors.toMap(
                r -> r.getExamQuestion().getId(),
                r -> r
            ));

        // Build question palette data (status map)
        Map<Integer, String> statusMap = new LinkedHashMap<>();
        for (ExamQuestion eq : eqs) {
            UserResponse r = responseMap.get(eq.getId());
            String status = (r == null) ? "NOT_VISITED" : r.getQuestionStatus().name();
            statusMap.put(eq.getDisplayOrder(), status);
        }

        // Section indices for section navigation
        Map<String, Integer> sectionStart = new LinkedHashMap<>();
        for (ExamQuestion eq : eqs) {
            String sectionName = eq.getQuestion().getSection().name();
            sectionStart.putIfAbsent(sectionName, eq.getDisplayOrder());
        }

        model.addAttribute("attempt",     attempt);
        model.addAttribute("exam",        exam);
        model.addAttribute("questions",   eqs);
        model.addAttribute("responseMap", responseMap);
        model.addAttribute("statusMap",   statusMap);
        model.addAttribute("sectionStart",sectionStart);
        model.addAttribute("totalQ",      eqs.size());
        model.addAttribute("sections",    Section.values());

        return "exam/exam-page";
    }

    // ── AJAX: Save Answer ──────────────────────────────────
    @PostMapping("/api/save-answer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveAnswer(
            @RequestBody Map<String, Object> payload) {
        try {
            Long attemptId       = Long.valueOf(payload.get("attemptId").toString());
            Long examQuestionId  = Long.valueOf(payload.get("examQuestionId").toString());
            String selectedOpt   = payload.get("selectedOption") != null
                ? payload.get("selectedOption").toString() : null;
            QuestionStatus status = QuestionStatus.valueOf(
                payload.get("questionStatus").toString());
            int timeSpent        = Integer.parseInt(payload.get("timeSpentSeconds").toString());
            int remaining        = Integer.parseInt(payload.get("remainingSeconds").toString());

            sessionService.saveResponse(attemptId, examQuestionId,
                selectedOpt, status, timeSpent, remaining);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Save answer error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ── AJAX: Timer heartbeat ──────────────────────────────
    @PostMapping("/api/heartbeat")
    @ResponseBody
    public ResponseEntity<Void> heartbeat(@RequestBody Map<String, Object> payload) {
        Long attemptId = Long.valueOf(payload.get("attemptId").toString());
        int remaining  = Integer.parseInt(payload.get("remainingSeconds").toString());
        sessionService.updateTimer(attemptId, remaining);
        return ResponseEntity.ok().build();
    }

    // ── AJAX: Tab switch ───────────────────────────────────
    @PostMapping("/api/tab-switch")
    @ResponseBody
    public ResponseEntity<Void> tabSwitch(@RequestBody Map<String, Object> payload) {
        Long attemptId = Long.valueOf(payload.get("attemptId").toString());
        sessionService.recordTabSwitch(attemptId);
        return ResponseEntity.ok().build();
    }

    // ── Submit Exam ────────────────────────────────────────
    @PostMapping("/submit/{attemptId}")
    public String submitExam(@PathVariable Long attemptId,
                              @RequestParam(defaultValue = "false") boolean timedOut) {
        Result result = timedOut
            ? sessionService.autoSubmit(attemptId)
            : sessionService.submitExam(attemptId);

        return "redirect:/exam/result/" + attemptId;
    }

    // ── Result Page ────────────────────────────────────────
    @GetMapping("/result/{attemptId}")
    public String resultPage(@PathVariable Long attemptId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = userService.findByEmail(userDetails.getUsername());

        ExamAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found"));

        // Security check
        if (!attempt.getUser().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }

        Result result = resultRepository.findByAttemptId(attemptId)
            .orElseThrow(() -> new RuntimeException("Result not found"));

        // Load questions with responses for review
        List<ExamQuestion> eqs = eqRepository.findByExamOrderByDisplayOrder(attempt.getExam());
        List<UserResponse> responses = responseRepository.findByAttempt(attempt);
        Map<Long, UserResponse> responseMap = responses.stream()
            .collect(Collectors.toMap(r -> r.getExamQuestion().getId(), r -> r));

        model.addAttribute("attempt",    attempt);
        model.addAttribute("result",     result);
        model.addAttribute("questions",  eqs);
        model.addAttribute("responseMap",responseMap);
        model.addAttribute("sections",   Section.values());

        return "exam/result";
    }
}
