package com.globalbooks.payments.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalbooks.payments.model.Payment;
import com.globalbooks.payments.model.PaymentStatus;
import com.globalbooks.payments.service.PaymentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentEventConsumer {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "payments.process")
    @RabbitHandler
    public void handlePaymentRequired(String message) {
        try {
            PaymentRequiredEvent event = objectMapper.readValue(message, PaymentRequiredEvent.class);

            System.out.println("Processing payment for order: " + event.getOrderId());

            // Create payment record
            Payment payment = new Payment();
            payment.setOrderId(Long.parseLong(event.getOrderId()));
            payment.setCustomerId(event.getCustomerId());
            payment.setAmount(event.getAmount());
            payment.setPaymentMethod(event.getPaymentMethod());
            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setCreatedAt(LocalDateTime.now());

            // Process payment
            Payment processedPayment = paymentService.processPayment(payment);

            // Publish payment result back to orders service
            publishPaymentResult(processedPayment);

        } catch (Exception e) {
            System.err.println("Failed to process payment event: " + e.getMessage());
            // Send to dead letter queue
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    private void publishPaymentResult(Payment payment) {
        // Implementation for publishing payment result back to orders service
        System.out.println("Payment " + payment.getStatus() + " for order: " + payment.getOrderId());
    }

    // Event class
    public static class PaymentRequiredEvent {
        private String orderId;
        private String customerId;
        private double amount;
        private String paymentMethod;

        public PaymentRequiredEvent() {}

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
}