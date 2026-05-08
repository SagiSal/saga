package com.saga.inventory.listener;

import com.saga.common.events.OrderCreatedEvent;
import com.saga.common.kafka.SagaTopics;
import com.saga.inventory.service.InventorySagaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventsListener {
    private static final Logger logger = LoggerFactory.getLogger(InventoryEventsListener.class);

    private final InventorySagaService inventorySagaService;

    public InventoryEventsListener(InventorySagaService inventorySagaService) {
        this.inventorySagaService = inventorySagaService;
    }

    @KafkaListener(topics = SagaTopics.ORDERS, groupId = "inventory-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCreated(OrderCreatedEvent event) {
        logger.info("Processing OrderCreatedEvent: {}", event);
        inventorySagaService.reserveInventory(event);
    }
}
