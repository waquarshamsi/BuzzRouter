package com.waquarshamsi.api.telegram_notifer.consumer;

import com.waquarshamsi.api.telegram_notifer.dto.NotificationRequest;
import com.waquarshamsi.api.telegram_notifer.exception.NotificationSendingException;
import com.waquarshamsi.api.telegram_notifer.service.NotificationDispatcher;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationDispatcher notificationDispatcher;

    public NotificationConsumer(NotificationDispatcher notificationDispatcher) {
        this.notificationDispatcher = notificationDispatcher;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveNotification(@Valid NotificationRequest request) {
        // If a correlationId is not provided, generate a new one.
        String correlationId = request.getCorrelationId()
                .orElseGet(() -> UUID.randomUUID().toString());

        // Use try-with-resources to ensure the MDC is cleared automatically
        try (MDC.MDCCloseable mdc = MDC.putCloseable(NotificationRequest.CORRELATION_ID_KEY, correlationId)) {
            log.info("Received notification request: {}", request);
            try {
                notificationDispatcher.dispatch(request);
                log.info("Successfully processed notification request: {}", request);
            } catch (NotificationSendingException e) {
                log.error("Unrecoverable error processing notification request: {}. Message will be sent to DLQ.", request, e);
                // This tells RabbitMQ the message failed permanently and should be dead-lettered.
                throw new AmqpRejectAndDontRequeueException(e);
            } catch (Exception e) {
                log.error("Unexpected error processing notification request: {}. Message will be sent to DLQ.", request, e);
                throw new AmqpRejectAndDontRequeueException(e);
            }
        }
    }
}