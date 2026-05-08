package com.saga.order.controller;

import com.saga.order.service.OrderSagaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderSagaService orderSagaService;

    public OrderController(OrderSagaService orderSagaService) {
        this.orderSagaService = orderSagaService;
    }

    @PostMapping
    public ResponseEntity<OrderSagaService.OrderResponse> createOrder(@RequestBody OrderSagaService.CreateOrderRequest request) {
        return ResponseEntity.ok(orderSagaService.createOrder(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderSagaService.OrderResponse> getOrder(@PathVariable UUID orderId) {
        var order = orderSagaService.getOrder(orderId);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }
}
