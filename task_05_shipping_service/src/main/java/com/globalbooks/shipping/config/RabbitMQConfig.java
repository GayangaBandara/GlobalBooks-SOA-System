package com.globalbooks.shipping.config;

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
    public TopicExchange shippingExchange() {
        return new TopicExchange("shipping.exchange", true, false);
    }

    // Queues
    @Bean
    public Queue shippingQueue() {
        return QueueBuilder.durable("shipping.create")
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "shipping.dlq")
            .withArgument("x-message-ttl", 300000) // 5 minutes TTL
            .build();
    }

    @Bean
    public Queue shippingDeadLetterQueue() {
        return QueueBuilder.durable("dlq.shipping").build();
    }

    // Bindings
    @Bean
    public Binding shippingBinding() {
        return BindingBuilder
            .bind(shippingQueue())
            .to(ordersExchange())
            .with("order.shipping.required");
    }

    @Bean
    public Binding shippingDeadLetterBinding() {
        return BindingBuilder
            .bind(shippingDeadLetterQueue())
            .to(new DirectExchange("dlx.exchange", true, false))
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
        template.setMandatory(true);
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
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}