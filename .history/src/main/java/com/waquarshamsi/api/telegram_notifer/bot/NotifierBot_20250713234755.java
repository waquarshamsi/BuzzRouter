package com.waquarshamsi.api.telegram_notifer.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class NotifierBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(NotifierBot.class);

    public NotifierBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        // This bot is for sending notifications only.
        // We can add logic here to handle incoming messages,
        // for example, to get a user's chat ID.
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("Received message from user {} (chatId: {}): {}",
                    update.getMessage().getFrom().getUserName(),
                    update.getMessage().getChatId(),
                    update.getMessage().getText());
        }
    }

    @Override
    public String getBotUsername() {
        // You can set this to a value from properties if needed
        return "NotifierServiceBot";
    }
}