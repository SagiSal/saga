package com.saga.common.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentFailedEvent(UUID orderId, BigDecimal amount, List<OrderItem> items, String reason) {
}
