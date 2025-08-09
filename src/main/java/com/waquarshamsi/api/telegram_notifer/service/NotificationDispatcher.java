package com.waquarshamsi.api.telegram_notifer.service;

import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private final List<NotificationService> notificationServices;

    public NotificationDispatcher(List<NotificationService> notificationServices) {
        this.notificationServices = notificationServices;
        log.info("NotificationDispatcher initialized with {} services.", notificationServices.size());
    }

    public void dispatch(NotificationRequest request) {
        log.info("Dispatching notification for originator: {}", request.getOriginator());
        final Map<NotificationTarget, String> recipients = request.getRecipients();

        request.getTargets().forEach(target -> {
            String recipientIdentifier = recipients.get(target);
            if (recipientIdentifier == null || recipientIdentifier.isBlank()) {
                log.warn("No recipient identifier found for target: {}. Skipping.", target);
                return; // Skips this target and continues to the next
            }

            notificationServices.stream().filter(service -> service.supports(target)).forEach(
                    service -> {
                        log.debug("Found service {} for target {}. Sending to {}",
                                service.getClass().getSimpleName(),
                                target, recipientIdentifier);
                        service.send(request, recipientIdentifier);
                    });
//  how to print this log () -> log.warn("No notification service found for target: {}", target)
        });
    }
}