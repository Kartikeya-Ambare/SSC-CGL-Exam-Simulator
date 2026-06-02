package com.ssccgl.service;

import com.ssccgl.dto.RegistrationDto;
import com.ssccgl.entity.Role;
import com.ssccgl.entity.User;
import com.ssccgl.exception.DuplicateEmailException;
import com.ssccgl.exception.PasswordMismatchException;
import com.ssccgl.exception.TokenExpiredException;
import com.ssccgl.repository.RoleRepository;
import com.ssccgl.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService    = emailService;
    }

    // ── Registration ───────────────────────────────────────
    public User register(RegistrationDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> {
                Role r = new Role(null, "ROLE_USER");
                return roleRepository.save(r);
            });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
            .firstName(dto.getFirstName().trim())
            .lastName(dto.getLastName().trim())
            .email(dto.getEmail().toLowerCase().trim())
            .mobileNumber(dto.getMobileNumber().trim())
            .password(passwordEncoder.encode(dto.getPassword()))
            .roles(roles)
            .active(true)
            .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} (id={})", saved.getEmail(), saved.getId());
        return saved;
    }

    // ── Forgot Password ────────────────────────────────────
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email.toLowerCase().trim())
            .ifPresent(user -> {
                String token = UUID.randomUUID().toString();
                user.setPasswordResetToken(token);
                user.setPasswordResetExpiry(LocalDateTime.now().plusHours(tokenExpiryHours));
                userRepository.save(user);

                String resetLink = baseUrl + "/auth/reset-password?token=" + token;
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);
                log.info("Password reset initiated for: {}", email);
            });
        // Always succeeds silently to prevent email enumeration
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Reset link has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
        log.info("Password reset completed for user: {}", user.getEmail());
    }

    // ── Helpers ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public void ensureAdminExists() {
        if (!userRepository.existsByEmail("admin@ssccgl.com")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role(null, "ROLE_ADMIN");
                    return roleRepository.save(r);
                });
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role r = new Role(null, "ROLE_USER");
                    return roleRepository.save(r);
                });

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);

            User admin = User.builder()
                .firstName("Admin")
                .lastName("SSC CGL")
                .email("admin@ssccgl.com")
                .mobileNumber("9000000000")
                .password(passwordEncoder.encode("Admin@2026!"))
                .roles(roles)
                .active(true)
                .emailVerified(true)
                .build();

            userRepository.save(admin);
            log.info("Default admin created: admin@ssccgl.com / Admin@2026!");
        }
    }
}
