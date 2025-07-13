package com.waquarshamsi.api.telegram_notifer.service;

import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;

/**
 * Defines the contract for a notification service.
 * This follows the Strategy design pattern.
 */
public interface NotificationService {

    void send(NotificationRequest request);

    boolean supports(NotificationTarget target);
}