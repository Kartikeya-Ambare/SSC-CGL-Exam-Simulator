package com.ssccgl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Centralized exception handling.
 * Maps exceptions to user-friendly error pages and logs the details server-side.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ── 403 Access Denied ───────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("status",  403);
        model.addAttribute("title",   "Access Denied");
        model.addAttribute("message", "You don't have permission to access this page.");
        return "error/error";
    }

    // ── 404 Not Found ────────────────────────────────────────────
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        model.addAttribute("status",  404);
        model.addAttribute("title",   "Page Not Found");
        model.addAttribute("message", "The page you are looking for doesn't exist.");
        return "error/error";
    }

    // ── Resource not found ───────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("status",  404);
        model.addAttribute("title",   "Not Found");
        model.addAttribute("message", ex.getMessage());
        return "error/error";
    }

    // ── Duplicate registration ───────────────────────────────────
    @ExceptionHandler(DuplicateEmailException.class)
    public String handleDuplicateEmail(DuplicateEmailException ex,
                                       HttpServletRequest request, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "auth/register";
    }

    // ── Token expired ────────────────────────────────────────────
    @ExceptionHandler(TokenExpiredException.class)
    public String handleTokenExpired(TokenExpiredException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "auth/forgot-password";
    }

    // ── Generic / 500 ────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, HttpServletRequest request, Model model) {
        //log.error("Unhandled exception on [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("status",  500);
        model.addAttribute("title",   "Something Went Wrong");
        model.addAttribute("message", "An unexpected error occurred. Please try again.");
        return "error/error";
    }
}
