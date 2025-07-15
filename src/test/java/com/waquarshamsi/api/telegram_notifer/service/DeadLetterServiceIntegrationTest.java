// package com.waquarshamsi.api.telegram_notifer.service;

// import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
// import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import com.waquarshamsi.api.telegram_notifer.service.impl.EmailNotificationService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.mail.MailAuthenticationException;
// import org.testcontainers.containers.RabbitMQContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;

// import java.time.Instant;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.TimeUnit;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.awaitility.Awaitility.await;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.reset;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;

// @Testcontainers
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// class DeadLetterServiceIntegrationTest {

//     @Container
//     @ServiceConnection
//     static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.13-management");

//     @Autowired
//     private TestRestTemplate restTemplate;

//     @Autowired
//     private DeadLetterService deadLetterService;

//     @Value("${app.rabbitmq.exchange}")
//     private String exchangeName;

//     @Value("${app.rabbitmq.routing-key}")
//     private String routingKey;

//     @MockBean
//     private EmailNotificationService emailNotificationService;

//     @BeforeEach
//     void setUp() {
//         // Reset mock interactions before each test to ensure isolation
//         reset(emailNotificationService);
//     }

//     @Test
//     void shouldReprocessMessageFromDlqAndFailAgain() {
//         // Arrange: Configure the mock email service to always fail
//         doThrow(new MailAuthenticationException("Simulated auth failure"))
//                 .when(emailNotificationService).send(any(NotificationRequest.class), anyString());

//         // Arrange: Create a notification request that targets the failing service
//         NotificationRequest request = createFailingNotificationRequest();

//         // Act: Send the message to the main queue
//         restTemplate.postForEntity("amqp://" + exchangeName + "/" + routingKey, request, Void.class);

//         // Assert: Wait for the message to fail all retries and land in the DLQ
//         await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
//             assertThat(deadLetterService.getDlqMessageCount()).isEqualTo(1L);
//         });

//         // Act: Call the reprocess endpoint via the REST API
//         ResponseEntity<Map> reprocessResponse = restTemplate.postForEntity("/api/dlq/reprocess-one", null, Map.class);
//         assertThat(reprocessResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
//         assertThat(reprocessResponse.getBody()).containsEntry("message", "One message from DLQ has been re-queued for processing.");

//         // Assert: The DLQ should be empty immediately after reprocessing
//         assertThat(deadLetterService.getDlqMessageCount()).isEqualTo(0L);

//         // Assert: Wait for the re-processed message to fail again and land back in the DLQ
//         await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
//             assertThat(deadLetterService.getDlqMessageCount()).isEqualTo(1L);
//         });

//         // Verify that the failing service was called the correct number of times.
//         // 1 initial attempt + 3 retries = 4 calls for the first processing run.
//         // 1 re-processed attempt + 3 retries = 4 calls for the second processing run.
//         // Total = 8 calls.
//         verify(emailNotificationService, times(8)).send(any(NotificationRequest.class), anyString());
//     }

//     @Test
//     void shouldReprocessAllMessagesFromDlqAndFailAgain() {
//         // Arrange: Configure the mock email service to always fail
//         doThrow(new MailAuthenticationException("Simulated auth failure"))
//                 .when(emailNotificationService).send(any(NotificationRequest.class), anyString());

//         // Arrange: Send 3 messages to the main queue
//         int messageCount = 3;
//         for (int i = 0; i < messageCount; i++) {
//             NotificationRequest request = createFailingNotificationRequest();
//             restTemplate.postForEntity("amqp://" + exchangeName + "/" + routingKey, request, Void.class);
//         }

//         // Assert: Wait for all messages to land in the DLQ
//         await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
//             assertThat(deadLetterService.getDlqMessageCount()).isEqualTo(messageCount);
//         });

//         // Act: Call the reprocess-all endpoint
//         ResponseEntity<Map> reprocessResponse = restTemplate.postForEntity("/api/dlq/reprocess-all", null, Map.class);
//         assertThat(reprocessResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
//         assertThat(reprocessResponse.getBody()).containsEntry("message", "Attempted to reprocess " + messageCount + " message(s) from the DLQ.");

//         // Assert: The DLQ should be empty immediately after reprocessing
//         assertThat(deadLetterService.getDlqMessageCount()).isEqualTo(0L);

//         // Assert: Wait for all re-processed messages to fail again and land back in the DLQ
//         await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
//             assertThat(deadLetterService.getDlqMessageCount()).isEqualTo(messageCount);
//         });

//         // Verify that the failing service was called the correct number of times (3 messages * 2 runs * 4 calls/run = 24 calls)
//         verify(emailNotificationService, times(24)).send(any(NotificationRequest.class), anyString());
//     }

//     private NotificationRequest createFailingNotificationRequest() {
//         NotificationRequest request = new NotificationRequest();
//         request.setBody("This message will fail.");
//         request.setOriginator("IntegrationTest");
//         request.setTimestamp(Instant.now());
//         request.setTargets(List.of(NotificationTarget.EMAIL));
//         request.setRecipients(Map.of(NotificationTarget.EMAIL, "fail@example.com"));
//         return request;
//     }
// }