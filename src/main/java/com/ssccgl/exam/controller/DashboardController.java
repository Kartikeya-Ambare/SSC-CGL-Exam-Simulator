package com.ssccgl.exam.controller;

import com.ssccgl.exam.dto.ResultDto;
import com.ssccgl.exam.entity.ExamSession;
import com.ssccgl.exam.entity.Question;
import com.ssccgl.exam.entity.User;
import com.ssccgl.exam.repository.LoginAuditRepository;
import com.ssccgl.exam.repository.QuestionRepository;
import com.ssccgl.exam.security.CustomUserDetailsService.CustomUserDetails;
import com.ssccgl.exam.service.ExamService;
import com.ssccgl.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired private ExamService examService;
    @Autowired private UserService userService;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private LoginAuditRepository loginAuditRepository;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getUserId());
        List<ResultDto> history = examService.getResultHistory(user);
        ExamSession activeSession = examService.getActiveSession(user);

        model.addAttribute("user", user);
        model.addAttribute("history", history);
        model.addAttribute("activeSession", activeSession);
        model.addAttribute("totalAttempts", user.getTotalAttempts());
        model.addAttribute("bestScore", user.getBestScore());
        model.addAttribute("gaCount", questionRepository.countBySection(Question.Section.GENERAL_AWARENESS));
        model.addAttribute("vaCount", questionRepository.countBySection(Question.Section.VERBAL_ABILITY));
        model.addAttribute("lrCount", questionRepository.countBySection(Question.Section.LOGICAL_REASONING));
        model.addAttribute("qaCount", questionRepository.countBySection(Question.Section.QUANTITATIVE_APTITUDE));
        return "dashboard/home";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getUserId());
        List<ResultDto> results = examService.getResultHistory(user);
        var loginHistory = loginAuditRepository.findTop10ByUser(user);

        double avgScore = results.stream().mapToDouble(ResultDto::getTotalScore).average().orElse(0.0);
        double avgAccuracy = results.stream().mapToDouble(ResultDto::getAccuracyPercentage).average().orElse(0.0);

        model.addAttribute("user", user);
        model.addAttribute("recentResults", results);
        model.addAttribute("loginHistory", loginHistory);
        model.addAttribute("avgScore", avgScore);
        model.addAttribute("avgAccuracy", avgAccuracy);
        model.addAttribute("bestScore", user.getBestScore());
        return "dashboard/profile";
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getUserId());
        List<ResultDto> results = examService.getResultHistory(user);

        double bestScore = results.stream().mapToDouble(ResultDto::getTotalScore).max().orElse(0.0);
        double avgScore = results.stream().mapToDouble(ResultDto::getTotalScore).average().orElse(0.0);
        double avgAccuracy = results.stream().mapToDouble(ResultDto::getAccuracyPercentage).average().orElse(0.0);

        model.addAttribute("user", user);
        model.addAttribute("results", results);
        model.addAttribute("bestScore", results.isEmpty() ? null : bestScore);
        model.addAttribute("avgScore", results.isEmpty() ? null : avgScore);
        model.addAttribute("avgAccuracy", results.isEmpty() ? null : avgAccuracy);
        return "dashboard/history";
    }
}
