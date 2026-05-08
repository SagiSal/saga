package com.saga.common.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCancelledEvent(UUID orderId, UUID customerId, String reason, BigDecimal totalAmount) {
}
