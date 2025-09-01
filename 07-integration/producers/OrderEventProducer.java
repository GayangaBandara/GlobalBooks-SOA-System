// OrderEventProducer.java - Message Producer for Order Events
package com.globalbooks.integration.producers;

import com.fasterxml.jackson.databind.ObjectMapper;
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

// PaymentEventConsumer.java - Payment Service Message Consumer
package com.globalbooks.payments.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalbooks.payments.service.PaymentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            
            // Process payment
            PaymentResult result = paymentService.processPayment(
                event.getOrderId(),
                event.getAmount(),
                event.getPaymentMethod()
            );
            
            // Publish payment result
            publishPaymentResult(result);
            
        } catch (Exception e) {
            System.err.println("Failed to process payment event: " + e.getMessage());
            // Send to dead letter queue
            throw new RuntimeException("Payment processing failed", e);
        }
    }
    
    private void publishPaymentResult(PaymentResult result) {
        // Implementation for publishing payment result back to orders service
        System.out.println("Payment " + result.getStatus() + " for order: " + result.getOrderId());
    }
}

// ShippingEventConsumer.java - Shipping Service Message Consumer
package com.globalbooks.shipping.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalbooks.shipping.service.ShippingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShippingEventConsumer {
    
    @Autowired
    private ShippingService shippingService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @RabbitListener(queues = "shipping.create")
    public void handleShippingRequired(String message) {
        try {
            ShippingRequiredEvent event = objectMapper.readValue(message, ShippingRequiredEvent.class);
            
            System.out.println("Creating shipment for order: " + event.getOrderId());
            
            // Create shipment
            ShipmentResult result = shippingService.createShipment(
                event.getOrderId(),
                event.getShippingAddress(),
                event.getItems()
            );
            
            // Publish shipment created event
            publishShipmentCreated(result);
            
        } catch (Exception e) {
            System.err.println("Failed to process shipping event: " + e.getMessage());
            throw new RuntimeException("Shipping processing failed", e);
        }
    }
    
    private void publishShipmentCreated(ShipmentResult result) {
        System.out.println("Shipment created with tracking: " + result.getTrackingNumber());
    }
}

// PaymentRequiredEvent.java - Event Model
package com.globalbooks.integration.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class PaymentRequiredEvent {
    private String orderId;
    private String customerId;
    private double amount;
    private PaymentMethod paymentMethod;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    public PaymentRequiredEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public PaymentRequiredEvent(String orderId, String customerId, 
                              double amount, PaymentMethod paymentMethod) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

// RabbitMQConfig.java - RabbitMQ Configuration
package com.globalbooks.integration.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    
    // Exchanges
    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange("orders.exchange", true, false);
    }
    
    @Bean
    public TopicExchange paymentsExchange() {
        return new TopicExchange("payments.exchange", true, false);
    }
    
    @Bean
    public TopicExchange shippingExchange() {
        return new TopicExchange("shipping.exchange", true, false);
    }
    
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx.exchange", true, false);
    }
    
    // Queues with Dead Letter Configuration
    @Bean
    public Queue paymentsQueue() {
        return QueueBuilder.durable("payments.process")
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "payments.dlq")
            .withArgument("x-message-ttl", 300000) // 5 minutes TTL
            .build();
    }
    
    @Bean
    public Queue shippingQueue() {
        return QueueBuilder.durable("shipping.create")
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "shipping.dlq")
            .withArgument("x-message-ttl", 300000) // 5 minutes TTL
            .build();
    }
    
    // Dead Letter Queues
    @Bean
    public Queue paymentsDeadLetterQueue() {
        return QueueBuilder.durable("dlq.payments").build();
    }
    
    @Bean
    public Queue shippingDeadLetterQueue() {
        return QueueBuilder.durable("dlq.shipping").build();
    }
    
    // Bindings
    @Bean
    public Binding paymentsBinding() {
        return BindingBuilder
            .bind(paymentsQueue())
            .to(ordersExchange())
            .with("order.payment.required");
    }
    
    @Bean
    public Binding shippingBinding() {
        return BindingBuilder
            .bind(shippingQueue())
            .to(ordersExchange())
            .with("order.shipping.required");
    }
    
    @Bean
    public Binding paymentsDeadLetterBinding() {
        return BindingBuilder
            .bind(paymentsDeadLetterQueue())
            .to(deadLetterExchange())
            .with("payments.dlq");
    }
    
    @Bean
    public Binding shippingDeadLetterBinding() {
        return BindingBuilder
            .bind(shippingDeadLetterQueue())
            .to(deadLetterExchange())
            .with("shipping.dlq");
    }
    
    // Message Converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // RabbitTemplate Configuration
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true); // Ensure messages are routed
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("Message sent successfully");
            } else {
                System.err.println("Message send failed: " + cause);
            }
        });
        return template;
    }
    
    // Listener Container Factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setDefaultRequeueRejected(false); // Send to DLQ on failure
        return factory;
    }
}

// ErrorHandlingService.java - Centralized Error Handling
package com.globalbooks.integration.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ErrorHandlingService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void handleDeadLetterMessage(String queueName, String message, Exception error) {
        System.err.println("Message failed processing in queue: " + queueName);
        System.err.println("Error: " + error.getMessage());
        System.err.println("Message content: " + message);
        
        // Log to monitoring system
        logToMonitoring(queueName, message, error);
        
        // Send notification if critical
        if (isCriticalError(error)) {
            sendAlertNotification(queueName, error);
        }
    }
    
    private void logToMonitoring(String queueName, String message, Exception error) {
        // Implementation for logging to monitoring system (e.g., ELK stack)
        System.out.println("Logged error to monitoring system");
    }
    
    private boolean isCriticalError(Exception error) {
        // Define criteria for critical errors
        return error instanceof RuntimeException && 
               error.getMessage().contains("CRITICAL");
    }
    
    private void sendAlertNotification(String queueName, Exception error) {
        // Send alert to operations team
        System.out.println("ALERT: Critical error in queue " + queueName + ": " + error.getMessage());
    }
}

// QoSConfiguration.java - Quality of Service Configuration
package com.globalbooks.integration.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QoSConfiguration {
    
    @Bean
    public SimpleMessageListenerContainer paymentsListenerContainer(
            ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("payments.process");
        
        // QoS Settings
        container.setPrefetchCount(10); // Prefetch up to 10 messages
        container.setConcurrentConsumers(2); // Start with 2 consumers
        container.setMaxConcurrentConsumers(5); // Scale up to 5 consumers
        container.setStartConsumerMinInterval(10000); // 10 seconds between consumer starts
        container.setStopConsumerMinInterval(60000); // 1 minute between consumer stops
        container.setConsecutiveActiveTrigger(10); // Trigger scaling after 10 active cycles
        container.setConsecutiveIdleTrigger(5); // Trigger scaling down after 5 idle cycles
        
        // Acknowledgment Settings
        container.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        
        return container;
    }
    
    @Bean 
    public SimpleMessageListenerContainer shippingListenerContainer(
            ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("shipping.create");
        
        // QoS Settings for Shipping (lower priority than payments)
        container.setPrefetchCount(5);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(3);
        container.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.AUTO);
        
        return container;
    }
}