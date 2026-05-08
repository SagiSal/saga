package com.saga.common.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InventoryReservedEvent(UUID orderId, List<OrderItem> items, BigDecimal totalAmount, boolean reserved) {
}
