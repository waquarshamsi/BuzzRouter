// package com.waquarshamsi.api.telegram_notifer.consumer;

// import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
// import com.waquarshamsi.api.telegram_notifer.exception.NotificationSendingException;
// import com.waquarshamsi.api.telegram_notifer.service.NotificationDispatcher;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.amqp.AmqpRejectAndDontRequeueException;

// import java.time.Instant;
// import java.util.List;
// import java.util.Map;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class NotificationConsumerTest {

//     @Mock
//     private NotificationDispatcher notificationDispatcher;

//     private NotificationConsumer notificationConsumer;

//     @BeforeEach
//     void setUp() {
//         notificationConsumer = new NotificationConsumer(notificationDispatcher);
//     }

//     @Test
//     void receiveNotification_shouldDispatchSuccessfully() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();

//         // Act
//         notificationConsumer.receiveNotification(request);

//         // Assert
//         verify(notificationDispatcher, times(1)).dispatch(request);
//     }

//     @Test
//     void receiveNotification_shouldThrowAmqpException_whenDispatcherThrowsNotificationSendingException() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         doThrow(new NotificationSendingException("Service unavailable", new RuntimeException()))
//                 .when(notificationDispatcher).dispatch(request);

//         // Act & Assert
//         assertThrows(AmqpRejectAndDontRequeueException.class, () -> notificationConsumer.receiveNotification(request));
//         verify(notificationDispatcher, times(1)).dispatch(request);
//     }

//     @Test
//     void receiveNotification_shouldThrowAmqpException_whenDispatcherThrowsGenericException() {
//         // Arrange
//         NotificationRequest request = createNotificationRequest();
//         doThrow(new RuntimeException("Unexpected error")).when(notificationDispatcher).dispatch(request);

//         // Act & Assert
//         assertThrows(AmqpRejectAndDontRequeueException.class, () -> notificationConsumer.receiveNotification(request));
//         verify(notificationDispatcher, times(1)).dispatch(request);
//     }

//     private NotificationRequest createNotificationRequest() {
//         NotificationRequest request = new NotificationRequest();
//         request.setBody("Test message");
//         request.setOriginator("TestSystem");
//         request.setTimestamp(Instant.now());
//         request.setTargets(List.of());
//         request.setRecipients(Map.of());
//         request.setMetadata(Map.of(NotificationRequest.CORRELATION_ID_KEY, UUID.randomUUID().toString()));
//         return request;
//     }
// }