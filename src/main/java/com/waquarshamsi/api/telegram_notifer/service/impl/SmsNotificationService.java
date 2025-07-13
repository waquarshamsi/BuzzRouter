package com.waquarshamsi.api.telegram_notifer.service.impl;

import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.exception.NotificationSendingException;
import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
import com.waquarshamsi.api.telegram_notifer.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "feature-flags.sms.enabled", havingValue = "true")
public class SmsNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    private final String fromNumber;

    public SmsNotificationService(@Value("${twilio.from-number}") String fromNumber) {
        this.fromNumber = fromNumber;
    }

    @Override
    @Retryable(
            value = {ApiException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void send(NotificationRequest request, String recipientIdentifier) {
        log.info("Attempting to send SMS to: {}", recipientIdentifier);
        Message.creator(
                new PhoneNumber(recipientIdentifier),
                new PhoneNumber(fromNumber),
                request.getBody()
        ).create();
        log.info("Successfully sent SMS to: {}", recipientIdentifier);
    }

    @Override
    public boolean supports(NotificationTarget target) {
        return target == NotificationTarget.SMS;
    }

    @Recover
    public void recover(ApiException e, NotificationRequest request, String recipientIdentifier) {
        log.error("All retries failed for sending SMS to: {}. Final error: {}", recipientIdentifier, e.getMessage());
        throw new NotificationSendingException("Failed to send SMS after multiple retries", e);
    }
}