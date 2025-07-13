package com.waquarshamsi.api.telegram_notifer.service.impl;

import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.exception.NotificationSendingException;
import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
import com.waquarshamsi.api.telegram_notifer.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailNotificationService(JavaMailSender mailSender, @Value("${app.mail.from}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    @Retryable(
            value = {MailException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void send(NotificationRequest request, String recipientIdentifier) {
        log.info("Attempting to send email to: {}", recipientIdentifier);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientIdentifier);
        message.setSubject("Notification from " + request.getOriginator());
        message.setText(request.getBody());
        mailSender.send(message);
        log.info("Successfully sent email to: {}", recipientIdentifier);
    }

    @Override
    public boolean supports(NotificationTarget target) {
        return target == NotificationTarget.EMAIL;
    }

    @Recover
    public void recover(MailException e, NotificationRequest request, String recipientIdentifier) {
        log.error("All retries failed for sending email to: {}. Final error: {}", recipientIdentifier, e.getMessage());
        throw new NotificationSendingException("Failed to send email after multiple retries", e);
    }
}