// package com.waquarshamsi.api.telegram_notifer.service;

// import com.rabbitmq.client.AMQP;
// import com.rabbitmq.client.Channel;
// import com.rabbitmq.client.GetResponse;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.amqp.AmqpException;
// import org.springframework.amqp.core.Message;
// import org.springframework.amqp.core.MessageProperties;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;

// import java.io.IOException;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class DeadLetterServiceTest {

//     @Mock
//     private RabbitTemplate rabbitTemplate;

//     private DeadLetterService deadLetterService;

//     // Test values for constructor arguments, normally injected via @Value
//     private final String dlqName = "test.dlq";
//     private final String originalExchange = "test.exchange";
//     private final String originalRoutingKey = "test.routing-key";

//     @BeforeEach
//     void setUp() {
//         // Manually instantiate the service with mocks and test values
//         deadLetterService = new DeadLetterService(rabbitTemplate, dlqName, originalExchange, originalRoutingKey);
//     }

//     @Test
//     void reprocessOneMessage_shouldSendToOriginalExchange_whenMessageExists() {
//         // Arrange
//         Message message = new Message("test body".getBytes(), new MessageProperties());
//         when(rabbitTemplate.receive(dlqName)).thenReturn(message);
//         ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
//         ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
//         ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

//         // Act
//         boolean result = deadLetterService.reprocessOneMessage();

//         // Assert
//         assertThat(result).isTrue();
//         verify(rabbitTemplate, times(1)).send(exchangeCaptor.capture(), routingKeyCaptor.capture(), messageCaptor.capture());
//         assertThat(exchangeCaptor.getValue()).isEqualTo(originalExchange);
//         assertThat(routingKeyCaptor.getValue()).isEqualTo(originalRoutingKey);
//         assertThat(messageCaptor.getValue()).isEqualTo(message);
//     }

//     @Test
//     void reprocessOneMessage_shouldReturnFalse_whenNoMessageExists() {
//         // Arrange
//         when(rabbitTemplate.receive(dlqName)).thenReturn(null);

//         // Act
//         boolean result = deadLetterService.reprocessOneMessage();

//         // Assert
//         assertThat(result).isFalse();
//         verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
//     }

//     @Test
//     void reprocessAllMessages_shouldReprocessAllMessagesInTransaction() throws IOException {
//         // Arrange
//         // We use thenAnswer to simulate the channel interaction within the execute method
//         when(rabbitTemplate.execute(any())).thenAnswer(invocation -> {
//             Channel channel = mock(Channel.class);
//             GetResponse getResponse1 = mock(GetResponse.class);
//             GetResponse getResponse2 = mock(GetResponse.class);

//             // Simulate receiving two messages, then null to end the loop
//             when(channel.basicGet(dlqName, false)).thenReturn(getResponse1, getResponse2, null);

//             // Execute the callback lambda provided to rabbitTemplate.execute
//             return invocation.getArgument(0, RabbitTemplate.ChannelCallback.class).doInRabbit(channel);
//         });

//         // Act
//         long reprocessedCount = deadLetterService.reprocessAllMessages();

//         // Assert
//         assertThat(reprocessedCount).isEqualTo(2);
//     }

//     @Test
//     void reprocessAllMessages_shouldReturnZero_whenDlqIsEmpty() {
//         // Arrange
//         when(rabbitTemplate.execute(any())).thenAnswer(invocation -> {
//             Channel channel = mock(Channel.class);
//             when(channel.basicGet(dlqName, false)).thenReturn(null); // No messages
//             return invocation.getArgument(0, RabbitTemplate.ChannelCallback.class).doInRabbit(channel);
//         });

//         // Act
//         long reprocessedCount = deadLetterService.reprocessAllMessages();

//         // Assert
//         assertThat(reprocessedCount).isZero();
//     }


//     @Test
//     void getDlqMessageCount_shouldReturnCorrectCount() {
//         // Arrange
//         // Mock the behavior of the lambda passed to rabbitTemplate.execute
//         when(rabbitTemplate.execute(any())).thenAnswer(invocation -> {
//             Channel channel = mock(Channel.class);
//             AMQP.Queue.DeclareOk declareOk = mock(AMQP.Queue.DeclareOk.class);
//             when(declareOk.getMessageCount()).thenReturn(5);
//             when(channel.queueDeclarePassive(dlqName)).thenReturn(declareOk);

//             // Execute the callback provided to the mock
//             return invocation.getArgument(0, RabbitTemplate.ChannelCallback.class).doInRabbit(channel);
//         });

//         // Act
//         long count = deadLetterService.getDlqMessageCount();

//         // Assert
//         assertThat(count).isEqualTo(5L);
//     }

//     @Test
//     void getDlqMessageCount_shouldReturnZero_whenQueueDoesNotExist() {
//         // Arrange
//         // Simulate the case where queueDeclarePassive throws an exception
//         when(rabbitTemplate.execute(any())).thenThrow(new AmqpException("Queue not found"));

//         // Act
//         long count = deadLetterService.getDlqMessageCount();

//         // Assert
//         assertThat(count).isEqualTo(0L);
//     }

//     @Test
//     void purgeDlq_shouldCallExecuteWithPurgeLogic() {
//         // Arrange
//         when(rabbitTemplate.execute(any())).thenAnswer(invocation -> {
//             Channel channel = mock(Channel.class);
//             AMQP.Queue.PurgeOk purgeOk = mock(AMQP.Queue.PurgeOk.class);
//             when(purgeOk.getMessageCount()).thenReturn(10);
//             when(channel.queuePurge(dlqName)).thenReturn(purgeOk);
//             return invocation.getArgument(0, RabbitTemplate.ChannelCallback.class).doInRabbit(channel);
//         });

//         // Act
//         long count = deadLetterService.purgeDlq();

//         // Assert
//         assertThat(count).isEqualTo(10L);
//         verify(rabbitTemplate, times(1)).execute(any());
//     }
// }
