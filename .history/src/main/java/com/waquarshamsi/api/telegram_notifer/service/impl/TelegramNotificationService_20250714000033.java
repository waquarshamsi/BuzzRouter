package com.waquarshamsi.api.telegram_notifer.service.impl;

import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.exception.NotificationSendingException;
import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
import com.waquarshamsi.api.telegram_notifer.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);
    private final AbsSender absSender;

    public TelegramNotificationService(AbsSender absSender) {
        this.absSender = absSender;
    }

    @Override
    @Retryable(
            value = {TelegramApiException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void send(NotificationRequest request, String recipientIdentifier) {
        log.info("Attempting to send Telegram message to chat ID: {}", recipientIdentifier);

        SendMessage message = new SendMessage();
        message.setChatId(recipientIdentifier);
        message.setText(request.getBody());
        message.enableMarkdown(true); // Allows for rich text formatting

        try { // We still need a try-catch here because TelegramApiException is a checked exception
            absSender.execute(message);
            log.info("Successfully sent Telegram message to chat ID: {}", recipientIdentifier);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e); // Re-throw as a runtime exception to be caught by @Retryable
        }
    }

    @Override
    public boolean supports(NotificationTarget target) {
        return target == NotificationTarget.TELEGRAM;
    }

    @Recover
    public void recover(RuntimeException e, NotificationRequest request, String recipientIdentifier) {
        log.error("All retries failed for sending Telegram message to chat ID: {}. Final error: {}", recipientIdentifier, e.getMessage());
        throw new NotificationSendingException("Failed to send Telegram message after multiple retries", e);
    }
}