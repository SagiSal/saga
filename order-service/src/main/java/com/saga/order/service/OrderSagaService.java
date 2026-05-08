package com.saga.order.service;

import com.saga.common.events.InventoryReservationFailedEvent;
import com.saga.common.events.OrderCancelledEvent;
import com.saga.common.events.OrderCompletedEvent;
import com.saga.common.events.OrderCreatedEvent;
import com.saga.common.events.OrderItem;
import com.saga.common.events.PaymentFailedEvent;
import com.saga.common.events.PaymentProcessedEvent;
import com.saga.common.kafka.SagaTopics;
import com.saga.order.model.OrderEntity;
import com.saga.order.model.OrderItemEmbeddable;
import com.saga.order.model.OrderStatus;
import com.saga.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderSagaService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderSagaService(com.saga.order.repository.OrderRepository orderRepository,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = new OrderEntity(orderId,
                request.customerId(),
                convertItems(request.items()),
                request.totalAmount(),
                OrderStatus.CREATED);

        orderRepository.save(order);
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, order.getCustomerId(), request.items(), order.getTotalAmount());
        kafkaTemplate.send(SagaTopics.ORDERS, orderId.toString(), event);

        return new OrderResponse(orderId, order.getStatus().name(), order.getTotalAmount());
    }

    public OrderResponse getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> new OrderResponse(order.getId(), order.getStatus().name(), order.getTotalAmount()))
                .orElse(null);
    }

    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            kafkaTemplate.send(SagaTopics.NOTIFICATIONS,
                    event.orderId().toString(),
                    new OrderCompletedEvent(order.getId(), order.getCustomerId(), order.getTotalAmount()));
        });
    }

    @Transactional
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            kafkaTemplate.send(SagaTopics.NOTIFICATIONS,
                    event.orderId().toString(),
                    new OrderCancelledEvent(order.getId(), order.getCustomerId(), event.reason(), order.getTotalAmount()));
        });
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            kafkaTemplate.send(SagaTopics.NOTIFICATIONS,
                    event.orderId().toString(),
                    new OrderCancelledEvent(order.getId(), order.getCustomerId(), event.reason(), order.getTotalAmount()));
        });
    }

    private List<OrderItemEmbeddable> convertItems(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemEmbeddable(item.productId(), item.quantity()))
                .collect(Collectors.toList());
    }

    public static record CreateOrderRequest(UUID customerId, List<OrderItem> items, java.math.BigDecimal totalAmount) {
    }

    public static record OrderResponse(UUID orderId, String status, java.math.BigDecimal totalAmount) {
    }
}
