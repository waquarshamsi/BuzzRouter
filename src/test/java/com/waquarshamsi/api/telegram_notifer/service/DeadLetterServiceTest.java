package com.waquarshamsi.api.telegram_notifer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadLetterServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private DeadLetterService deadLetterService;

    // Test values for constructor arguments, normally injected via @Value
    private final String dlqName = "test.dlq";
    private final String originalExchange = "test.exchange";
    private final String originalRoutingKey = "test.routing-key";

    @BeforeEach
    void setUp() {
        // Manually instantiate the service with mocks and test values
        deadLetterService = new DeadLetterService(rabbitTemplate, dlqName, originalExchange, originalRoutingKey);
    }

    @Test
    void reprocessOneMessage_shouldSendToOriginalExchange_whenMessageExists() {
        // Arrange
        Message message = new Message("test body".getBytes(), new MessageProperties());
        when(rabbitTemplate.receive(dlqName)).thenReturn(message);
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        // Act
        boolean result = deadLetterService.reprocessOneMessage();

        // Assert
        assertThat(result).isTrue();
        verify(rabbitTemplate, times(1)).send(exchangeCaptor.capture(), routingKeyCaptor.capture(), messageCaptor.capture());
        assertThat(exchangeCaptor.getValue()).isEqualTo(originalExchange);
        assertThat(routingKeyCaptor.getValue()).isEqualTo(originalRoutingKey);
        assertThat(messageCaptor.getValue()).isEqualTo(message);
    }

    @Test
    void reprocessOneMessage_shouldReturnFalse_whenNoMessageExists() {
        // Arrange
        when(rabbitTemplate.receive(dlqName)).thenReturn(null);

        // Act
        boolean result = deadLetterService.reprocessOneMessage();

        // Assert
        assertThat(result).isFalse();
        verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
    }

    @Test
    void getDlqMessageCount_shouldReturnCorrectCount() {
        // Arrange
        Properties queueProperties = new Properties();
        queueProperties.put(RabbitTemplate.QUEUE_MESSAGE_COUNT, 5);
        when(rabbitTemplate.getQueueProperties(dlqName)).thenReturn(queueProperties);

        // Act
        long count = deadLetterService.getDlqMessageCount();

        // Assert
        assertThat(count).isEqualTo(5L);
    }

    @Test
    void purgeDlq_shouldCallExecuteWithPurgeLogic() {
        // Arrange
        when(rabbitTemplate.execute(any())).thenAnswer(invocation -> {
            // Simulate the lambda execution for purging
            return 10; // Simulate purging 10 messages
        });

        // Act
        long count = deadLetterService.purgeDlq();

        // Assert
        assertThat(count).isEqualTo(10L);
        verify(rabbitTemplate, times(1)).execute(any());
    }
}