package com.waquarshamsi.api.telegram_notifer.service.impl;

import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
import com.waquarshamsi.api.telegram_notifer.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    public void send(NotificationRequest request) {
        log.info("Preparing to send email to: {}", request.getRecipientIdentifier());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(request.getRecipientIdentifier());
        message.setSubject("Notification from " + request.getOriginator());
        message.setText(request.getBody());

        try {
            mailSender.send(message);
            log.info("Successfully sent email to: {}", request.getRecipientIdentifier());
        } catch (MailException e) {
            log.error("Failed to send email to: {}", request.getRecipientIdentifier(), e);
        }
    }

    @Override
    public boolean supports(NotificationTarget target) {
        return target == NotificationTarget.EMAIL;
    }
}