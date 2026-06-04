package com.ssccgl.exam.service;

import com.ssccgl.exam.dto.RegistrationDto;
import com.ssccgl.exam.entity.LoginAudit;
import com.ssccgl.exam.entity.User;
import com.ssccgl.exam.repository.LoginAuditRepository;
import com.ssccgl.exam.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginAuditRepository loginAuditRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim().toLowerCase());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName().trim());
        user.setMobile(dto.getMobile());
        user.setGender(dto.getGender());
        user.setState(dto.getState());
        user.setActive(true);

        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            try {
                user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth(), DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (Exception e) {
                logger.warn("Invalid date of birth format: {}", dto.getDateOfBirth());
            }
        }

        User saved = userRepository.save(user);
        logger.info("New user registered: {}", saved.getUsername());
        return saved;
    }

    @Transactional
    public void recordLogin(User user, HttpServletRequest request, LoginAudit.LoginStatus status) {
        LoginAudit audit = new LoginAudit();
        audit.setUser(user);
        audit.setUsername(user != null ? user.getUsername() : "unknown");
        audit.setIpAddress(getClientIp(request));
        audit.setLoginTime(LocalDateTime.now());
        audit.setStatus(status);
        audit.setUserAgent(request.getHeader("User-Agent"));
        loginAuditRepository.save(audit);

        if (user != null && status == LoginAudit.LoginStatus.SUCCESS) {
            userRepository.updateLastLogin(user.getId(), LocalDateTime.now());
        }
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updateUserStats(Long userId, Double score) {
        userRepository.incrementAttempts(userId);
        userRepository.updateBestScore(userId, score);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
