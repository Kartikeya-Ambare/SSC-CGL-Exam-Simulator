package com.ssccgl.controller;

import com.ssccgl.dto.RegistrationDto;
import com.ssccgl.exception.DuplicateEmailException;
import com.ssccgl.exception.PasswordMismatchException;
import com.ssccgl.exception.TokenExpiredException;
import com.ssccgl.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserService userService) {
        this.userService = userService;
    }


    private final UserService userService;

    // ── Login ──────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("loginError",
            "Invalid email or password. Please try again.");
        if (logout != null) model.addAttribute("logoutMsg",
            "You have been logged out successfully.");
        return "auth/login";
    }

    // ── Registration ───────────────────────────────────────
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("registrationDto") RegistrationDto dto,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttrs) {
        // Additional validation
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword",
                "Passwords do not match");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            redirectAttrs.addFlashAttribute("successMsg",
                "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (DuplicateEmailException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Registration failed. Please try again.");
            log.error("Registration error: {}", e.getMessage());
            return "auth/register";
        }
    }

    // ── Forgot Password ────────────────────────────────────
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email,
                                        RedirectAttributes redirectAttrs) {
        userService.initiatePasswordReset(email);
        redirectAttrs.addFlashAttribute("successMsg",
            "If this email exists in our system, a reset link has been sent.");
        return "redirect:/auth/forgot-password";
    }

    // ── Reset Password ─────────────────────────────────────
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes redirectAttrs) {
        try {
            userService.resetPassword(token, newPassword, confirmPassword);
            redirectAttrs.addFlashAttribute("successMsg",
                "Password reset successfully! Please login.");
            return "redirect:/auth/login";
        } catch (PasswordMismatchException | TokenExpiredException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }
}
