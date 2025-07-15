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
// import org.springframework.mail.MailException;
// import org.springframework.mail.MailSendException;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;

// import java.time.Instant;
// import java.util.List;
// import java.util.Map;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class EmailNotificationServiceTest {

//     @Mock
//     private JavaMailSender mailSender;

//     private EmailNotificationService emailNotificationService;

//     private final String fromEmail = "test@sender.com";

//     @BeforeEach
//     void setUp() {
//         emailNotificationService = new EmailNotificationService(mailSender, fromEmail);
//     }

//     @Test
//     void send_shouldConstructAndSendEmailSuccessfully() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         String recipient = "recipient@example.com";
//         ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

//         // Act
//         emailNotificationService.send(request, recipient);

//         // Assert
//         verify(mailSender, times(1)).send(messageCaptor.capture());
//         SimpleMailMessage capturedMessage = messageCaptor.getValue();

//         assertThat(capturedMessage.getFrom()).isEqualTo(fromEmail);
//         assertThat(capturedMessage.getTo()).containsExactly(recipient);
//         assertThat(capturedMessage.getSubject()).isEqualTo("Notification from " + request.getOriginator());
//         assertThat(capturedMessage.getText()).isEqualTo(request.getBody());
//     }

//     @Test
//     void send_shouldThrowMailException_whenMailSenderFails() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         String recipient = "recipient@example.com";
//         doThrow(new MailSendException("Failed to send")).when(mailSender).send(any(SimpleMailMessage.class));

//         // Act & Assert
//         assertThrows(MailSendException.class, () -> emailNotificationService.send(request, recipient));
//     }

//     @Test
//     void supports_shouldReturnTrue_forEmailTarget() {
//         // Act
//         boolean result = emailNotificationService.supports(NotificationTarget.EMAIL);
//         // Assert
//         assertThat(result).isTrue();
//     }

//     @Test
//     void supports_shouldReturnFalse_forNonEmailTarget() {
//         // Act
//         boolean result = emailNotificationService.supports(NotificationTarget.SMS);
//         // Assert
//         assertThat(result).isFalse();
//     }

//     @Test
//     void recover_shouldThrowNotificationSendingException() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         MailException mailException = new MailSendException("Final failure");

//         // Act & Assert
//         NotificationSendingException thrown = assertThrows(NotificationSendingException.class,
//                 () -> emailNotificationService.recover(mailException, request, "recipient@example.com"));

//         assertThat(thrown.getMessage()).isEqualTo("Failed to send email after multiple retries");
//         assertThat(thrown.getCause()).isEqualTo(mailException);
//     }

//     @Test
//     void fallback_shouldThrowNotificationSendingException() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         Exception exception = new RuntimeException("Circuit open");

//         // Act & Assert
//         NotificationSendingException thrown = assertThrows(NotificationSendingException.class,
//                 () -> emailNotificationService.fallback(request, "recipient@example.com", exception));

//         assertThat(thrown.getMessage()).isEqualTo("Circuit breaker open for email service. Not retrying.");
//         assertThat(thrown.getCause()).isEqualTo(exception);
//     }

//     private NotificationRequest createNotificationRequest() {
//         NotificationRequest request = new NotificationRequest();
//         request.setBody("Test Body");
//         request.setOriginator("Test Originator");
//         request.setTimestamp(Instant.now());
//         request.setTargets(List.of(NotificationTarget.EMAIL));
//         request.setRecipients(Map.of(NotificationTarget.EMAIL, "recipient@example.com"));
//         return request;
//     }
// }