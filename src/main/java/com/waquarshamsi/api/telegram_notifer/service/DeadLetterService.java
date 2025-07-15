package com.waquarshamsi.api.telegram_notifer.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterService.class);

    private final RabbitTemplate rabbitTemplate;
    private final String dlqName;
    private final String originalExchange;
    private final String originalRoutingKey;

    public DeadLetterService(RabbitTemplate rabbitTemplate,
                             @Value("${app.rabbitmq.dlq}") String dlqName,
                             @Value("${app.rabbitmq.exchange}") String originalExchange,
                             @Value("${app.rabbitmq.routing-key}") String originalRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.dlqName = dlqName;
        this.originalExchange = originalExchange;
        this.originalRoutingKey = originalRoutingKey;
    }

    public boolean reprocessOneMessage() {
        Message message = rabbitTemplate.receive(dlqName);
        if (message != null) {
            log.info("Reprocessing one message from DLQ.");
            // Republish the message to the original exchange with the original routing key
            rabbitTemplate.send(originalExchange, originalRoutingKey, message);
            return true;
        }
        log.info("DLQ is empty, no message to reprocess.");
        return false;
    }

    public long reprocessAllMessages() {
        log.info("Attempting to reprocess all messages from DLQ in a single transaction.");

        Long reprocessedCount = rabbitTemplate.execute(channel -> {
            long count = 0;
            // Start a transaction on the channel for all-or-nothing processing
            channel.txSelect();
            try {
                // Use a while loop to drain the queue completely
                while (true) {
                    GetResponse response = channel.basicGet(dlqName, false); // autoAck = false
                    if (response == null) {
                        // No more messages in the queue, exit the loop
                        break;
                    }
                    // Republish the message to the original exchange
                    channel.basicPublish(originalExchange, originalRoutingKey, response.getProps(), response.getBody());
                    // Acknowledge the message from the DLQ. This will be committed at the end.
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false); // multiple = false
                    count++;
                }
                // If the loop completes without error, commit the transaction.
                // All messages are published and acknowledged atomically.
                channel.txCommit();
                return count;
            } catch (Exception e) {
                log.error("Error during bulk reprocessing of DLQ messages. Rolling back transaction.", e);
                // If any error occurs, roll back the entire transaction.
                // No messages will be republished or acknowledged.
                channel.txRollback();
                throw new AmqpException("Failed to reprocess all messages from DLQ due to an error.", e);
            }
        });

        long finalCount = reprocessedCount != null ? reprocessedCount : 0;
        if (finalCount > 0) {
            log.info("Successfully reprocessed {} message(s) from DLQ.", finalCount);
        } else {
            log.info("DLQ was empty, no messages were reprocessed.");
        }
        return finalCount;
    }

    public long getDlqMessageCount() {
        try {
            Long count = rabbitTemplate.execute(channel -> {
                AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(dlqName);
                return (long) declareOk.getMessageCount();
            });
            return Objects.requireNonNullElse(count, 0L);
        } catch (AmqpException e) {
            // This is an expected scenario if the queue doesn't exist yet, so we log at a lower level.
            if (log.isTraceEnabled()) {
                log.trace("Could not get message count for DLQ '{}', it may not exist yet. Cause: {}", dlqName, e.getMessage());
            }
            return 0L;
        }
    }

    public long purgeDlq() {
        Integer purgedCount = rabbitTemplate.execute(channel -> channel.queuePurge(dlqName).getMessageCount());
        long count = purgedCount != null ? purgedCount : 0;
        log.warn("Purged {} message(s) from DLQ.", count);
        return count;
    }
}