package com.waquarshamsi.api.telegram_notifer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    @Value("${features.notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${features.notification.sms.enabled:false}")
    private boolean smsEnabled;

    public boolean isEmailNotificationEnabled() {
        return emailEnabled;
    }

    public boolean isSmsNotificationEnabled() {
        return smsEnabled;
    }
}