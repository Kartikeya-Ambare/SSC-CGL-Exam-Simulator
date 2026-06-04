package com.ssccgl.exam.controller;

import com.ssccgl.exam.dto.RegistrationDto;
import com.ssccgl.exam.entity.User;
import com.ssccgl.exam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           @RequestParam(required = false) String expired,
                           Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "You have been successfully logged out.");
        }
        if (expired != null) {
            model.addAttribute("expiredMsg", "Your session has expired. Please login again.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registrationDto") RegistrationDto dto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("passwordError", "Passwords do not match");
            return "auth/register";
        }

        try {
            User user = userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMsg",
                "Registration successful! Please login with your credentials.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("registrationError", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            model.addAttribute("registrationError", "Registration failed. Please try again.");
            return "auth/register";
        }
    }
}
