// package com.waquarshamsi.api.telegram_notifer.service.impl;

// import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
// import com.waquarshamsi.api.telegram_notifer.exception.NotificationSendingException;
// import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
// import org.telegram.telegrambots.meta.bots.AbsSender;
// import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

// import java.time.Instant;
// import java.util.List;
// import java.util.Map;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class TelegramNotificationServiceTest {

//     @Mock
//     private AbsSender absSender;

//     private TelegramNotificationService telegramNotificationService;

//     @BeforeEach
//     void setUp() {
//         telegramNotificationService = new TelegramNotificationService(absSender);
//     }

//     @Test
//     void send_shouldConstructAndSendMessageSuccessfully() throws TelegramApiException {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         String recipientChatId = "123456789";
//         ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);

//         // Act
//         telegramNotificationService.send(request, recipientChatId);

//         // Assert
//         verify(absSender, times(1)).execute(messageCaptor.capture());
//         SendMessage capturedMessage = messageCaptor.getValue();

//         assertThat(capturedMessage.getChatId()).isEqualTo(recipientChatId);
//         assertThat(capturedMessage.getText()).isEqualTo(request.getBody());
//         assertThat(capturedMessage.getEnableMarkdown()).isTrue();
//     }

//     @Test
//     void send_shouldThrowRuntimeException_whenSenderThrowsTelegramApiException() throws TelegramApiException {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         String recipientChatId = "123456789";
//         TelegramApiException telegramApiException = new TelegramApiException("API Error");
//         doThrow(telegramApiException).when(absSender).execute(any(SendMessage.class));

//         // Act & Assert
//         RuntimeException thrown = assertThrows(RuntimeException.class,
//                 () -> telegramNotificationService.send(request, recipientChatId));

//         assertThat(thrown.getCause()).isEqualTo(telegramApiException);
//     }

//     @Test
//     void supports_shouldReturnTrue_forTelegramTarget() {
//         assertThat(telegramNotificationService.supports(NotificationTarget.TELEGRAM)).isTrue();
//     }

//     @Test
//     void supports_shouldReturnFalse_forNonTelegramTarget() {
//         assertThat(telegramNotificationService.supports(NotificationTarget.EMAIL)).isFalse();
//     }

//     @Test
//     void recover_shouldThrowNotificationSendingException() {
//         // Arrange
//         RuntimeException runtimeException = new RuntimeException("Final failure");

//         // Act & Assert
//         assertThrows(NotificationSendingException.class,
//                 () -> telegramNotificationService.recover(runtimeException, createNotificationRequest(), "123456789"));
//     }

//     @Test
//     void fallback_shouldThrowNotificationSendingException() {
//         // Arrange
//         Exception exception = new RuntimeException("Circuit open");

//         // Act & Assert
//         assertThrows(NotificationSendingException.class,
//                 () -> telegramNotificationService.fallback(createNotificationRequest(), "123456789", exception));
//     }

//     private NotificationRequest createNotificationRequest() {
//         NotificationRequest request = new NotificationRequest();
//         request.setBody("Test Telegram Body");
//         request.setOriginator("Test Telegram Originator");
//         request.setTimestamp(Instant.now());
//         request.setTargets(List.of(NotificationTarget.TELEGRAM));
//         request.setRecipients(Map.of(NotificationTarget.TELEGRAM, "123456789"));
//         return request;
//     }
// }