# Saga Microservices Example

This repository contains a Maven multi-module Spring Boot project implementing a saga choreography across four microservices:

- `order-service`
- `inventory-service`
- `payment-service`
- `email-service`

Kafka is used for asynchronous communication and saga orchestration. Each service uses its own PostgreSQL database.

## Services

- `order-service`: accepts orders and publishes `OrderCreatedEvent`.
- `inventory-service`: reserves inventory and publishes `InventoryReservedEvent`.
- `payment-service`: performs payment validation and publishes `PaymentProcessedEvent` or `PaymentFailedEvent`.
- `email-service`: sends notification events for completed and cancelled orders.

## Build and run

1. Build the project:

```bash
mvn -pl common,order-service,inventory-service,payment-service,email-service clean package
```

2. Start the full stack:

```bash
docker compose up --build
```

3. Create an order:

```bash
curl -X POST http://localhost:8081/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"3ec8f1ca-8b0c-4ae8-83d0-1d6a9cdc80c8","items":[{"productId":"product-1","quantity":2}],"totalAmount":450.00}'
```

## Saga flow

1. `order-service` publishes `OrderCreatedEvent`.
2. `inventory-service` reserves stock and publishes `InventoryReservedEvent`.
3. `payment-service` attempts payment and publishes either `PaymentProcessedEvent` or `PaymentFailedEvent`.
4. On payment success, `order-service` publishes `OrderCompletedEvent`; `email-service` sends confirmation.
5. On payment failure, `order-service` publishes `OrderCancelledEvent` and `inventory-service` releases reserved stock.

## Ports

- Order Service: `8081`
- Inventory Service: `8082`
- Payment Service: `8083`
- Email Service: `8084`

## Notes

- Kafka is exposed on `localhost:9092` and `localhost:29092`.
- PostgreSQL containers use ports `5432`, `5433`, and `5434`.
