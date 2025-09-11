package com.globalbooks.integration.producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalbooks.integration.events.PaymentRequiredEvent;
import com.globalbooks.integration.events.ShippingRequiredEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ORDERS_EXCHANGE = "orders.exchange";

    public void publishPaymentRequired(PaymentRequiredEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                ORDERS_EXCHANGE,
                "order.payment.required",
                message
            );
            System.out.println("Published payment required event for order: " + event.getOrderId());
        } catch (Exception e) {
            System.err.println("Failed to publish payment event: " + e.getMessage());
        }
    }

    public void publishShippingRequired(ShippingRequiredEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                ORDERS_EXCHANGE,
                "order.shipping.required",
                message
            );
            System.out.println("Published shipping required event for order: " + event.getOrderId());
        } catch (Exception e) {
            System.err.println("Failed to publish shipping event: " + e.getMessage());
        }
    }
}