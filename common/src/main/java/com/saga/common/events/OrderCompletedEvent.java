package com.saga.common.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCompletedEvent(UUID orderId, UUID customerId, BigDecimal totalAmount) {
}
