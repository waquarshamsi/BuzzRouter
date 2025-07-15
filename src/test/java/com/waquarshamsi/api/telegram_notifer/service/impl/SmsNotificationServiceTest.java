// package com.waquarshamsi.api.telegram_notifer.service.impl;

// import com.twilio.exception.ApiException;
// import com.twilio.rest.api.v2010.account.Message;
// import com.twilio.type.PhoneNumber;
// import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
// import com.waquarshamsi.api.telegram_notifer.exception.NotificationSendingException;
// import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.Instant;
// import java.util.List;
// import java.util.Map;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class SmsNotificationServiceTest {

//     private SmsNotificationService smsNotificationService;
//     private MockedStatic<Message> messageMockedStatic;

//     @Mock
//     private Message.Creator creator;

//     private final String fromNumber = "+15005550006";

//     @BeforeEach
//     void setUp() {
//         smsNotificationService = new SmsNotificationService(fromNumber);
//         // Mock the static Message.creator() method for the duration of the test
//         messageMockedStatic = mockStatic(Message.class);
//     }

//     @AfterEach
//     void tearDown() {
//         // It's important to close the static mock after each test to avoid side effects
//         messageMockedStatic.close();
//     }

//     @Test
//     void send_shouldCallTwilioCreatorSuccessfully() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         String recipient = "+12223334444";

//         // When Message.creator is called with any PhoneNumber, return our mock creator
//         messageMockedStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
//                 .thenReturn(creator);

//         // Act
//         smsNotificationService.send(request, recipient);

//         // Assert
//         // Verify that the static method was called with the correct parameters
//         messageMockedStatic.verify(() -> Message.creator(
//                 new PhoneNumber(recipient),
//                 new PhoneNumber(fromNumber),
//                 request.getBody()
//         ));
//         // Verify that the create() method was called on the returned creator instance
//         verify(creator, times(1)).create();
//     }

//     @Test
//     void send_shouldThrowApiException_whenCreatorFails() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         String recipient = "+12223334444";

//         messageMockedStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
//                 .thenReturn(creator);
//         when(creator.create()).thenThrow(new ApiException("Twilio API error"));

//         // Act & Assert
//         assertThrows(ApiException.class, () -> smsNotificationService.send(request, recipient));
//     }

//     @Test
//     void supports_shouldReturnTrue_forSmsTarget() {
//         assertThat(smsNotificationService.supports(NotificationTarget.SMS)).isTrue();
//     }

//     @Test
//     void supports_shouldReturnFalse_forNonSmsTarget() {
//         assertThat(smsNotificationService.supports(NotificationTarget.EMAIL)).isFalse();
//     }

//     @Test
//     void recover_shouldThrowNotificationSendingException() {
//         ApiException apiException = new ApiException("Final failure");
//         assertThrows(NotificationSendingException.class, () -> smsNotificationService.recover(apiException, createNotificationRequest(), "+12223334444"));
//     }

//     @Test
//     void fallback_shouldThrowNotificationSendingException() {
//         Exception exception = new RuntimeException("Circuit open");
//         assertThrows(NotificationSendingException.class, () -> smsNotificationService.fallback(createNotificationRequest(), "+12223334444", exception));
//     }

//     private NotificationRequest createNotificationRequest() {
//         NotificationRequest request = new NotificationRequest();
//         request.setBody("Test SMS Body");
//         request.setOriginator("Test SMS Originator");
//         request.setTimestamp(Instant.now());
//         request.setTargets(List.of(NotificationTarget.SMS));
//         request.setRecipients(Map.of(NotificationTarget.SMS, "+12223334444"));
//         return request;
//     }
// }