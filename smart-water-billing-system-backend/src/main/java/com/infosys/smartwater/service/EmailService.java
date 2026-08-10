package com.infosys.smartwater.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendEmailAlert(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Email alert skipped: Recipient email is blank.");
            return;
        }

        if (mailSender == null) {
            log.info("[DEVELOPMENT EMAIL LOG] To: {}, Subject: {}, Body: {}", toEmail, subject, body);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Successfully sent email alert to {}", toEmail);
        } catch (Exception e) {
            log.warn("Could not send email alert to {}. Cause: {}. In-app notification generated safely.", toEmail, e.getMessage());
        }
    }
}
