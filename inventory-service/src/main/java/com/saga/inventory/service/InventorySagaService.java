package com.saga.inventory.service;

import com.saga.common.events.InventoryReleaseEvent;
import com.saga.common.events.InventoryReservationFailedEvent;
import com.saga.common.events.InventoryReservedEvent;
import com.saga.common.events.OrderCreatedEvent;
import com.saga.common.events.OrderItem;
import com.saga.common.events.PaymentFailedEvent;
import com.saga.common.kafka.SagaTopics;
import com.saga.inventory.model.ProductInventory;
import com.saga.inventory.repository.InventoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventorySagaService {
    private static final Logger logger = LoggerFactory.getLogger(InventorySagaService.class);

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventorySagaService(InventoryRepository inventoryRepository,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void reserveInventory(OrderCreatedEvent event) {
        Map<String, Integer> requested = toQuantities(event.items());
        Map<String, ProductInventory> inventoryMap = inventoryRepository.findAllById(requested.keySet())
                .stream()
                .collect(Collectors.toMap(ProductInventory::getProductId, item -> item));

        for (OrderItem item : event.items()) {
            ProductInventory inventory = inventoryMap.get(item.productId());
            if (inventory == null || inventory.getAvailableQuantity() < item.quantity()) {
                logger.warn("Inventory reservation failed for order {}: insufficient inventory for product {}",
                        event.orderId(), item.productId());
                kafkaTemplate.send(SagaTopics.INVENTORY,
                        event.orderId().toString(),
                        new InventoryReservationFailedEvent(event.orderId(), event.items(), "Inventory reservation failed"));
                return;
            }
        }

        inventoryMap.values().forEach(inventory -> {
            int requestedAmount = requested.getOrDefault(inventory.getProductId(), 0);
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - requestedAmount);
        });

        inventoryRepository.saveAll(inventoryMap.values());
        kafkaTemplate.send(SagaTopics.INVENTORY,
                event.orderId().toString(),
                new InventoryReservedEvent(event.orderId(), event.items(), event.totalAmount(), true));
    }

    @Transactional
    public void compensateInventory(PaymentFailedEvent event) {
        List<OrderItem> items = event.items();
        if (items == null || items.isEmpty()) {
            return;
        }

        var restoredInventory = inventoryRepository.findAllById(items.stream().map(OrderItem::productId).toList())
                .stream()
                .peek(inventory -> {
                    int quantityToRestore = items.stream()
                            .filter(item -> item.productId().equals(inventory.getProductId()))
                            .mapToInt(OrderItem::quantity)
                            .sum();
                    inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantityToRestore);
                })
                .toList();

        inventoryRepository.saveAll(restoredInventory);
        kafkaTemplate.send(SagaTopics.INVENTORY,
                event.orderId().toString(),
                new InventoryReleaseEvent(event.orderId(), items, event.reason()));
    }

    private Map<String, Integer> toQuantities(List<OrderItem> items) {
        Map<String, Integer> quantities = new HashMap<>();
        for (OrderItem item : items) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return quantities;
    }
}
