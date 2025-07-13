package com.waquarshamsi.api.telegram_notifer.service.impl;

import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
import com.waquarshamsi.api.telegram_notifer.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public void send(NotificationRequest request) {
        log.info("Preparing to send Telegram message to chat ID: {}", request.getRecipientIdentifier());

        SendMessage message = new SendMessage();
        message.setChatId(request.getRecipientIdentifier());
        message.setText(request.getBody());
        message.enableMarkdown(true); // Allows for rich text formatting

        try {
            absSender.execute(message);
            log.info("Successfully sent Telegram message to chat ID: {}", request.getRecipientIdentifier());
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chat ID: {}", request.getRecipientIdentifier(), e);
        }
    }

    @Override
    public boolean supports(NotificationTarget target) {
        return target == NotificationTarget.TELEGRAM;
    }
}