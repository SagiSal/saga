package com.saga.order.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.saga.common.events.InventoryReservationFailedEvent;
import com.saga.common.kafka.SagaTopics;
import com.saga.order.service.OrderSagaService;

@Component
public class InventoryEventsListener {
    private static final Logger logger = LoggerFactory.getLogger(InventoryEventsListener.class);

    private final OrderSagaService orderSagaService;

    public InventoryEventsListener(OrderSagaService orderSagaService) {
        this.orderSagaService = orderSagaService;
    }

     @KafkaListener(topics = SagaTopics.INVENTORY, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void onInventoryReservationFailed(InventoryReservationFailedEvent event) {
        logger.info("Processing InventoryReservationFailedEvent: {}", event);
        orderSagaService.handleInventoryReservationFailed(event);
    }
}
