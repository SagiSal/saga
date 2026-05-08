package com.saga.common.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, UUID customerId, List<OrderItem> items, BigDecimal totalAmount) {
}
