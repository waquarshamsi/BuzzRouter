package com.waquarshamsi.api.telegram_notifer.info;

import com.waquarshamsi.api.telegram_notifer.service.NotificationService;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActiveServicesInfoContributor implements InfoContributor {

    private final List<NotificationService> notificationServices;

    public ActiveServicesInfoContributor(List<NotificationService> notificationServices) {
        this.notificationServices = notificationServices;
    }

    @Override
    public void contribute(Info.Builder builder) {
        List<String> activeServiceNames = notificationServices.stream()
                .map(service -> service.getClass().getSimpleName())
                .sorted()
                .collect(Collectors.toList());

        builder.withDetail("activeNotificationServices", activeServiceNames);
    }
}