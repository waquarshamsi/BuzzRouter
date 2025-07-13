package com.waquarshamsi.api.telegram_notifer.service;

import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Mock
    private NotificationService telegramService;

    @Mock
    private NotificationService emailService;

    @Mock
    private NotificationService smsService;

    private NotificationDispatcher notificationDispatcher;

    @BeforeEach
    void setUp() {
        // Configure the mocks to identify which target they support
        when(telegramService.supports(NotificationTarget.TELEGRAM)).thenReturn(true);
        when(emailService.supports(NotificationTarget.EMAIL)).thenReturn(true);
        when(smsService.supports(NotificationTarget.SMS)).thenReturn(true);

        // The dispatcher is initialized with our mock services
        notificationDispatcher = new NotificationDispatcher(List.of(telegramService, emailService, smsService));
    }

    @Test
    void dispatch_shouldCallCorrectService_forSingleTelegramTarget() {
        // Arrange
        String telegramChatId = "12345";
        NotificationRequest request = createNotificationRequest(
                List.of(NotificationTarget.TELEGRAM),
                Map.of(NotificationTarget.TELEGRAM, telegramChatId)
        );

        // Act
        notificationDispatcher.dispatch(request);

        // Assert
        verify(telegramService, times(1)).send(request, telegramChatId);
        verify(emailService, never()).send(any(), anyString());
        verify(smsService, never()).send(any(), anyString());
    }

    @Test
    void dispatch_shouldCallCorrectServices_forMultipleTargets() {
        // Arrange
        String emailAddress = "test@example.com";
        String phoneNumber = "+15551234567";
        NotificationRequest request = createNotificationRequest(
                List.of(NotificationTarget.EMAIL, NotificationTarget.SMS),
                Map.of(
                        NotificationTarget.EMAIL, emailAddress,
                        NotificationTarget.SMS, phoneNumber
                )
        );

        // Act
        notificationDispatcher.dispatch(request);

        // Assert
        verify(emailService, times(1)).send(request, emailAddress);
        verify(smsService, times(1)).send(request, phoneNumber);
        verify(telegramService, never()).send(any(), anyString());
    }

    @Test
    void dispatch_shouldNotCallService_whenRecipientIdentifierIsMissing() {
        // Arrange
        NotificationRequest request = createNotificationRequest(
                List.of(NotificationTarget.EMAIL),
                Map.of() // Recipient map is empty
        );

        // Act
        notificationDispatcher.dispatch(request);

        // Assert
        verifyNoInteractions(emailService, smsService, telegramService);
    }

    @Test
    void dispatch_shouldDoNothing_forEmptyTargetsList() {
        // Arrange
        NotificationRequest request = createNotificationRequest(
                List.of(), // Empty targets
                Map.of(NotificationTarget.EMAIL, "test@example.com")
        );

        // Act
        notificationDispatcher.dispatch(request);

        // Assert
        verifyNoInteractions(telegramService, emailService, smsService);
    }

    // Helper method to reduce boilerplate in tests
    private NotificationRequest createNotificationRequest(List<NotificationTarget> targets, Map<NotificationTarget, String> recipients) {
        NotificationRequest request = new NotificationRequest();
        request.setBody("Test message");
        request.setOriginator("TestSystem");
        request.setTimestamp(Instant.now());
        request.setTargets(targets);
        request.setRecipients(recipients);
        return request;
    }
}