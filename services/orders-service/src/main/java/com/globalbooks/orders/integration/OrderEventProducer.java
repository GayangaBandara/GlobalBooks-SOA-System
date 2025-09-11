package com.globalbooks.orders.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalbooks.orders.model.Order;
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

    public void publishPaymentRequired(Order order) {
        try {
            PaymentRequiredEvent event = new PaymentRequiredEvent(
                order.getId().toString(),
                order.getCustomerId(),
                order.getTotalAmount(),
                "CREDIT_CARD" // Default payment method
            );

            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                ORDERS_EXCHANGE,
                "order.payment.required",
                message
            );
            System.out.println("Published payment required event for order: " + order.getId());
        } catch (Exception e) {
            System.err.println("Failed to publish payment event: " + e.getMessage());
        }
    }

    public void publishShippingRequired(Order order) {
        try {
            ShippingRequiredEvent event = new ShippingRequiredEvent(
                order.getId().toString(),
                order.getShippingAddress(),
                order.getItems()
            );

            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                ORDERS_EXCHANGE,
                "order.shipping.required",
                message
            );
            System.out.println("Published shipping required event for order: " + order.getId());
        } catch (Exception e) {
            System.err.println("Failed to publish shipping event: " + e.getMessage());
        }
    }

    // Event classes
    public static class PaymentRequiredEvent {
        private String orderId;
        private String customerId;
        private double amount;
        private String paymentMethod;

        public PaymentRequiredEvent() {}

        public PaymentRequiredEvent(String orderId, String customerId, double amount, String paymentMethod) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
        }

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class ShippingRequiredEvent {
        private String orderId;
        private String shippingAddress;
        private java.util.List<?> items;

        public ShippingRequiredEvent() {}

        public ShippingRequiredEvent(String orderId, String shippingAddress, java.util.List<?> items) {
            this.orderId = orderId;
            this.shippingAddress = shippingAddress;
            this.items = items;
        }

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

        public java.util.List<?> getItems() { return items; }
        public void setItems(java.util.List<?> items) { this.items = items; }
    }
}