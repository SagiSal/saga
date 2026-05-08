package com.saga.common.events;

import java.util.List;
import java.util.UUID;

public record InventoryReleaseEvent(UUID orderId, List<OrderItem> items, String reason) {
}
