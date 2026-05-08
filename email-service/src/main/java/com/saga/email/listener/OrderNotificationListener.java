package com.saga.email.listener;

import com.saga.common.events.OrderCancelledEvent;
import com.saga.common.events.OrderCompletedEvent;
import com.saga.common.kafka.SagaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationListener.class);

    @KafkaListener(topics = SagaTopics.NOTIFICATIONS, groupId = "email-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onNotification(Object payload) {
        if (payload instanceof OrderCompletedEvent completed) {
            logger.info("Processing OrderCompletedEvent. Order {} completed for customer {} and total amount {}. Sending confirmation email.", completed.orderId(), completed.customerId(), completed.totalAmount());
        } else if (payload instanceof OrderCancelledEvent cancelled) {
            logger.info("Processing OrderCancelledEvent. Order {} cancelled for customer {}. Reason: {}. Sending cancellation email.", cancelled.orderId(), cancelled.customerId(), cancelled.reason());
        } else {
            logger.warn("Expected OrderCompletedEvent or OrderCancelledEvent but got: {}", payload.getClass().getName());
        }
    }
}
