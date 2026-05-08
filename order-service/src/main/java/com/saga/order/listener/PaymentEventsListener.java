package com.saga.order.listener;

import com.saga.common.events.PaymentFailedEvent;
import com.saga.common.events.PaymentProcessedEvent;
import com.saga.common.kafka.SagaTopics;
import com.saga.order.service.OrderSagaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = SagaTopics.PAYMENTS, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
public class PaymentEventsListener {
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventsListener.class);

    private final OrderSagaService orderSagaService;

    public PaymentEventsListener(OrderSagaService orderSagaService) {
        this.orderSagaService = orderSagaService;
    }

    @KafkaHandler
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        logger.info("Processing PaymentProcessedEvent: {}", event);
        orderSagaService.handlePaymentProcessed(event);
    }

    @KafkaHandler
    public void handlePaymentFailed(PaymentFailedEvent event) {
        logger.info("Processing PaymentFailedEvent: {}", event);
        orderSagaService.handlePaymentFailed(event);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknownEvent(Object event) {
        logger.warn("Received unknown event type: {}", event.getClass().getName());
    }
    
}
