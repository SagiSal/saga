package com.saga.payment.listener;

import com.saga.common.events.InventoryReservedEvent;
import com.saga.common.kafka.SagaTopics;
import com.saga.payment.service.PaymentSagaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventsListener {
    private static final Logger logger = LoggerFactory.getLogger(InventoryEventsListener.class);

    private final PaymentSagaService paymentSagaService;

    public InventoryEventsListener(PaymentSagaService paymentSagaService) {
        this.paymentSagaService = paymentSagaService;
    }

    @KafkaListener(topics = SagaTopics.INVENTORY, groupId = "payment-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onInventoryReserved(InventoryReservedEvent event) {
        logger.info("Processing InventoryReservedEvent: {}", event);
        paymentSagaService.processReservation(event);
    }
}
