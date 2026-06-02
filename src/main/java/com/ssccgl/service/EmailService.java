package com.ssccgl.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    private final JavaMailSender mailSender;

    @Async
    public void sendPasswordResetEmail(String to, String firstName, String resetLink) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject("SSC CGL Platform - Password Reset Request");
            msg.setText("""
                Dear %s,
                
                You requested a password reset for your SSC CGL Mock Test account.
                
                Click the link below to reset your password (valid for 24 hours):
                %s
                
                If you did not request this, please ignore this email.
                
                Regards,
                SSC CGL Platform Team
                """.formatted(firstName, resetLink));
            mailSender.send(msg);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }
}
