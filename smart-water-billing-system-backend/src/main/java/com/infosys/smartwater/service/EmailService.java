package com.infosys.smartwater.service;

import com.infosys.smartwater.exception.InvalidOperationException;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${application.mail.from:}")
    private String mailFrom;

    @PostConstruct
    public void init() {
        String effectiveUsername = resolveUsername();
        String effectivePassword = resolvePassword();

        boolean userPresent = effectiveUsername != null && !effectiveUsername.isBlank();
        boolean passPresent = effectivePassword != null && !effectivePassword.isBlank();

        log.info("[ENVIRONMENT CHECK] SPRING_MAIL_USERNAME = {}", userPresent ? "PRESENT" : "MISSING");
        log.info("[ENVIRONMENT CHECK] SPRING_MAIL_PASSWORD = {}", passPresent ? "PRESENT" : "MISSING");

        if (userPresent && passPresent && mailSender instanceof JavaMailSenderImpl mailSenderImpl) {
            mailSenderImpl.setUsername(effectiveUsername);
            mailSenderImpl.setPassword(effectivePassword);
            log.info("JavaMailSenderImpl initialized with username '{}'", maskEmail(effectiveUsername));
        }
    }

    private String resolveUsername() {
        if (mailUsername != null && !mailUsername.isBlank()) return mailUsername.trim();
        String env = System.getenv("SPRING_MAIL_USERNAME");
        if (env != null && !env.isBlank()) return env.trim();
        String prop = System.getProperty("spring.mail.username");
        if (prop != null && !prop.isBlank()) return prop.trim();
        return "";
    }

    private String resolvePassword() {
        if (mailPassword != null && !mailPassword.isBlank()) return mailPassword.trim();
        String env = System.getenv("SPRING_MAIL_PASSWORD");
        if (env != null && !env.isBlank()) return env.trim();
        String prop = System.getProperty("spring.mail.password");
        if (prop != null && !prop.isBlank()) return prop.trim();
        return "";
    }

    public boolean sendEmailAlert(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new InvalidOperationException("Recipient email address is required.");
        }

        String recipientClean = toEmail.toLowerCase().trim();
        String maskedEmail = maskEmail(recipientClean);

        String username = resolveUsername();
        String password = resolvePassword();

        log.info("Attempting to send invitation email to recipient: {}", recipientClean);

        if (mailSender == null || username.isBlank() || password.isBlank()) {
            String errorMsg = "SMTP credentials missing! Set environment variables SPRING_MAIL_USERNAME (your Gmail address) and SPRING_MAIL_PASSWORD (your 16-character App Password 'HydroBill (2)').";
            log.error("[SMTP UNCONFIGURED] {}", errorMsg);
            throw new InvalidOperationException(errorMsg);
        }

        try {
            if (mailSender instanceof JavaMailSenderImpl mailSenderImpl) {
                mailSenderImpl.setUsername(username);
                mailSenderImpl.setPassword(password);
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String fromAddress = (mailFrom != null && !mailFrom.isBlank()) ? mailFrom : username;
            helper.setFrom(fromAddress);
            helper.setTo(recipientClean);
            helper.setSubject(subject);
            helper.setText(body, true);

            log.info("Executing JavaMailSender.send() via host '{}:587' with sender '{}' to recipient '{}'...", mailHost, fromAddress, recipientClean);
            mailSender.send(message);
            log.info("JavaMailSender.send() SUCCEEDED for recipient: {}", recipientClean);
            return true;
        } catch (Exception e) {
            log.error("JavaMailSender.send() FAILED for recipient {}: {}", recipientClean, e.getMessage(), e);
            throw new InvalidOperationException("Failed to send invitation email to " + recipientClean + " via SMTP: " + e.getMessage() + ". Please verify SMTP host, port, username, and App Password.");
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIdx = email.indexOf("@");
        if (atIdx <= 2) return "***" + email.substring(atIdx);
        return email.substring(0, 2) + "***" + email.substring(atIdx);
    }
}
