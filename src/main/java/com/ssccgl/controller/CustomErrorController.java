package com.ssccgl.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles Spring Boot's /error route so that our custom
 * templates/error/error.html is rendered instead of the
 * default whitelabel page.
 *
 * Bug fix: GlobalExceptionHandler was returning "error/error" but
 * Spring's internal error dispatch could not find the template
 * because it looked for a root-level "error" template.
 * This controller bridges that gap.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        int status = 500;
        if (statusObj != null) {
            status = Integer.parseInt(statusObj.toString());
        }

        model.addAttribute("status", status);

        if (status == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("title", "Page Not Found");
            model.addAttribute("message", "The page you are looking for doesn't exist.");
        } else if (status == HttpStatus.FORBIDDEN.value()) {
            model.addAttribute("title", "Access Denied");
            model.addAttribute("message", "You don't have permission to access this page.");
        } else if (status == HttpStatus.UNAUTHORIZED.value()) {
            model.addAttribute("title", "Unauthorized");
            model.addAttribute("message", "Please log in to continue.");
        } else {
            model.addAttribute("title", "Something Went Wrong");
            model.addAttribute("message", "An unexpected error occurred. Please try again.");
        }

        return "error/error";
    }
}
