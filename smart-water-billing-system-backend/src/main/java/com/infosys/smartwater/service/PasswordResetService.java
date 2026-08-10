package com.infosys.smartwater.service;

import com.infosys.smartwater.entity.PasswordResetToken;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.exception.BusinessException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.repository.PasswordResetTokenRepository;
import com.infosys.smartwater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public String requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email address is required");
        }

        User user = userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);
        if (user == null) {
            // Do not reveal whether account exists
            return "If an account with that email exists, password reset instructions have been sent.";
        }

        // Delete any existing tokens for this user
        tokenRepository.deleteByUserId(user.getId());

        // Generate 6-digit OTP code / token
        String resetToken = String.format("%06d", (int)(Math.random() * 900000) + 100000);
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .user(user)
                .token(resetToken)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();

        tokenRepository.save(tokenEntity);

        String subject = "HydroBill — Password Reset OTP";
        String body = String.format(
                "Hello %s,\n\nYour password reset OTP is: %s\n\nThis token will expire in 15 minutes. If you did not request a password reset, please ignore this email.",
                user.getUsername(), resetToken
        );

        emailService.sendEmailAlert(user.getEmail(), subject, body);

        return "Password reset token sent to registered email if account exists.";
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Reset token is required");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("New password must be at least 6 characters long");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        if (Boolean.TRUE.equals(resetToken.getIsUsed())) {
            throw new BusinessException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new BusinessException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("Invalid current password");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("New password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("User {} successfully changed their password", user.getUsername());
    }
}
