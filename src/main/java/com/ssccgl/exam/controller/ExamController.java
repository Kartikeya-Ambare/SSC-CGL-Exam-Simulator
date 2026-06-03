package com.ssccgl.exam.controller;

import com.ssccgl.exam.dto.ExamStateDto;
import com.ssccgl.exam.dto.ResultDto;
import com.ssccgl.exam.entity.ExamSession;
import com.ssccgl.exam.entity.User;
import com.ssccgl.exam.security.CustomUserDetailsService.CustomUserDetails;
import com.ssccgl.exam.service.ExamService;
import com.ssccgl.exam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/exam")
public class ExamController {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    @GetMapping("/instructions")
    public String instructions(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getUserId());
        ExamSession activeSession = examService.getActiveSession(user);
        model.addAttribute("user", user);
        model.addAttribute("activeSession", activeSession);
        return "exam/instructions";
    }

    @PostMapping("/start")
    public String startExam(@AuthenticationPrincipal CustomUserDetails userDetails,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getUserId());
        String ip = getClientIp(request);

        try {
            ExamSession session = examService.startNewExam(user, ip);
            if (session == null) {
                redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to start exam. Time may have expired. Please try again.");
                return "redirect:/dashboard";
            }
            return "redirect:/exam/" + session.getId() + "/attempt";
        } catch (Exception e) {
            logger.error("Failed to start exam for user: {}", user.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to start exam: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/{sessionId}/attempt")
    public String attemptExam(@PathVariable Long sessionId,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getUserId());
        ExamSession session = examService.getSessionById(sessionId);

        if (session == null || !session.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid exam session.");
            return "redirect:/dashboard";
        }

        if (session.getStatus() != ExamSession.Status.IN_PROGRESS) {
            return "redirect:/exam/" + sessionId + "/result";
        }

        ExamStateDto examState = examService.getExamState(session);

        if (examState.getTimeRemainingSeconds() <= 0) {
            examService.autoSubmitExam(session);
            return "redirect:/exam/" + sessionId + "/result";
        }

        model.addAttribute("session", session);
        model.addAttribute("examState", examState);
        model.addAttribute("user", user);
        model.addAttribute("sessionId", sessionId);
        return "exam/attempt";
    }

    @GetMapping("/{sessionId}/review")
    public String reviewExam(@PathVariable Long sessionId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getUserId());
        ExamSession session = examService.getSessionById(sessionId);

        if (session == null || !session.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid exam session.");
            return "redirect:/dashboard";
        }

        if (session.getStatus() != ExamSession.Status.IN_PROGRESS) {
            return "redirect:/exam/" + sessionId + "/result";
        }

        ExamStateDto examState = examService.getExamState(session);
        model.addAttribute("session", session);
        model.addAttribute("examState", examState);
        model.addAttribute("user", user);
        model.addAttribute("sessionId", sessionId);
        return "exam/review";
    }

    @PostMapping("/{sessionId}/submit")
    public String submitExam(@PathVariable Long sessionId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getUserId());
        ExamSession session = examService.getSessionById(sessionId);

        if (session == null || !session.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid exam session.");
            return "redirect:/dashboard";
        }

        try {
            ResultDto result = examService.submitExam(session, false);
            return "redirect:/exam/" + sessionId + "/result";
        } catch (Exception e) {
            logger.error("Submit error for session {}: {}", sessionId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMsg", "Submission failed: " + e.getMessage());
            return "redirect:/exam/" + sessionId + "/attempt";
        }
    }

    @GetMapping("/{sessionId}/result")
    public String viewResult(@PathVariable Long sessionId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getUserId());
        ExamSession session = examService.getSessionById(sessionId);

        if (session == null || !session.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid exam session.");
            return "redirect:/dashboard";
        }

        if (session.getStatus() == ExamSession.Status.IN_PROGRESS) {
            return "redirect:/exam/" + sessionId + "/attempt";
        }

        ResultDto result = examService.getResultBySessionId(sessionId);
        if (result == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Result not found.");
            return "redirect:/dashboard";
        }

        model.addAttribute("result", result);
        model.addAttribute("user", user);
        model.addAttribute("sessionId", sessionId);
        return "exam/result";
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) return xfHeader.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
