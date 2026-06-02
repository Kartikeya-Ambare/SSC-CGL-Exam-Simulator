package com.ssccgl.controller;

import com.ssccgl.entity.Result;
import com.ssccgl.entity.User;
import com.ssccgl.enums.Section;
import com.ssccgl.repository.ExamAttemptRepository;
import com.ssccgl.repository.ResultRepository;
import com.ssccgl.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    public DashboardController(UserService userService, ResultRepository resultRepository, ExamAttemptRepository attemptRepository) {
        this.userService = userService;
        this.resultRepository = resultRepository;
        this.attemptRepository = attemptRepository;
    }


    private final UserService userService;
    private final ResultRepository resultRepository;
    private final ExamAttemptRepository attemptRepository;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());

        // ── Statistics ─────────────────────────────────────
        long totalAttempts = attemptRepository.countSubmittedByUser(user);
        Double avgScore    = resultRepository.findAverageScoreByUser(user);
        Double highScore   = resultRepository.findHighestScoreByUser(user);
        Double avgAcc      = resultRepository.findAverageAccuracyByUser(user);

        // ── Recent Results (last 10) ───────────────────────
        List<Result> recent = resultRepository.findTop10ByUserOrderByCreatedAtDesc(
            user, PageRequest.of(0, 10));

        // ── Score and accuracy trends (for Chart.js) ──────
        List<Double> scoreTrend    = recent.stream()
            .map(Result::getTotalScore).collect(Collectors.toList());
        List<Double> accuracyTrend = recent.stream()
            .map(Result::getAccuracyPercentage).collect(Collectors.toList());

        // ── Section averages ──────────────────────────────
        Map<String, Double> sectionAvg = Map.of(
            "GIR", avgOf(recent, "gir"),
            "GA",  avgOf(recent, "ga"),
            "QA",  avgOf(recent, "qa"),
            "EC",  avgOf(recent, "ec")
        );

        model.addAttribute("user",         user);
        model.addAttribute("totalAttempts",totalAttempts);
        model.addAttribute("avgScore",     avgScore   != null ? String.format("%.1f", avgScore) : "N/A");
        model.addAttribute("highScore",    highScore  != null ? String.format("%.1f", highScore): "N/A");
        model.addAttribute("avgAccuracy",  avgAcc     != null ? String.format("%.1f", avgAcc)  : "N/A");
        model.addAttribute("recentResults",recent);
        model.addAttribute("scoreTrend",   scoreTrend);
        model.addAttribute("accuracyTrend",accuracyTrend);
        model.addAttribute("sectionAvg",   sectionAvg);

        return "dashboard/home";
    }

    private double avgOf(List<Result> results, String section) {
        if (results.isEmpty()) return 0.0;
        return results.stream()
            .mapToDouble(r -> switch (section) {
                case "gir" -> r.getGirScore();
                case "ga"  -> r.getGaScore();
                case "qa"  -> r.getQaScore();
                case "ec"  -> r.getEcScore();
                default    -> 0.0;
            })
            .average().orElse(0.0);
    }
}
