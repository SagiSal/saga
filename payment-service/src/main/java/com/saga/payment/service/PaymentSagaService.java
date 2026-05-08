package com.saga.payment.service;

import com.saga.common.events.InventoryReservedEvent;
import com.saga.common.events.PaymentFailedEvent;
import com.saga.common.events.PaymentProcessedEvent;
import com.saga.common.kafka.SagaTopics;
import com.saga.payment.model.PaymentEntity;
import com.saga.payment.model.PaymentStatus;
import com.saga.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentSagaService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BigDecimal failureThreshold;

    public PaymentSagaService(PaymentRepository paymentRepository,
                              KafkaTemplate<String, Object> kafkaTemplate,
                              @Value("${payment.fail.threshold:1000}") BigDecimal failureThreshold) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.failureThreshold = failureThreshold;
    }

    @Transactional
    public void processReservation(InventoryReservedEvent event) {
        BigDecimal amount = event.totalAmount();

        UUID paymentId = UUID.randomUUID();
        PaymentStatus status = amount.compareTo(failureThreshold) > 0 ? PaymentStatus.FAILED : PaymentStatus.PROCESSED;
        paymentRepository.save(new PaymentEntity(paymentId, event.orderId(), amount, status));

        if (status == PaymentStatus.PROCESSED) {
            kafkaTemplate.send(SagaTopics.PAYMENTS,
                    event.orderId().toString(),
                    new PaymentProcessedEvent(event.orderId(), amount, paymentId.toString()));
        } else {
            kafkaTemplate.send(SagaTopics.PAYMENTS,
                    event.orderId().toString(),
                    new PaymentFailedEvent(event.orderId(), amount, event.items(), "Payment validation failed"));
        }
    }
}
