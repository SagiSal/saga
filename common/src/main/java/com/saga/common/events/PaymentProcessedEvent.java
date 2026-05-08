package com.saga.common.events;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentProcessedEvent(UUID orderId, BigDecimal amount, String paymentId) {
}
