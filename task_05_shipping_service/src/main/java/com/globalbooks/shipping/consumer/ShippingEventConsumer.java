package com.globalbooks.shipping.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalbooks.shipping.model.Shipping;
import com.globalbooks.shipping.model.ShippingStatus;
import com.globalbooks.shipping.service.ShippingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ShippingEventConsumer {

    @Autowired
    private ShippingService shippingService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "shipping.create")
    @RabbitHandler
    public void handleShippingRequired(String message) {
        try {
            ShippingRequiredEvent event = objectMapper.readValue(message, ShippingRequiredEvent.class);

            System.out.println("Creating shipment for order: " + event.getOrderId());

            // Create shipping record
            Shipping shipping = new Shipping();
            shipping.setOrderId(Long.parseLong(event.getOrderId()));
            shipping.setShippingAddress(event.getShippingAddress());
            shipping.setStatus(ShippingStatus.PROCESSING);
            shipping.setCreatedAt(LocalDateTime.now());

            // Process shipping
            Shipping processedShipping = shippingService.createShipment(shipping);

            // Publish shipping result back to orders service
            publishShippingResult(processedShipping);

        } catch (Exception e) {
            System.err.println("Failed to process shipping event: " + e.getMessage());
            throw new RuntimeException("Shipping processing failed", e);
        }
    }

    private void publishShippingResult(Shipping shipping) {
        System.out.println("Shipment created with tracking: " + shipping.getTrackingNumber());
    }

    // Event class
    public static class ShippingRequiredEvent {
        private String orderId;
        private String shippingAddress;
        private List<?> items;

        public ShippingRequiredEvent() {}

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

        public List<?> getItems() { return items; }
        public void setItems(List<?> items) { this.items = items; }
    }
}