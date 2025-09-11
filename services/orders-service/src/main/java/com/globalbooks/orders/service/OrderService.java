package com.globalbooks.orders.service;

import com.globalbooks.orders.integration.OrderEventProducer;
import com.globalbooks.orders.model.Order;
import com.globalbooks.orders.model.OrderItem;
import com.globalbooks.orders.model.OrderStatus;
import com.globalbooks.orders.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderEventProducer orderEventProducer;

    public Order createOrder(Order order) {
        if (order == null || order.getCustomerId() == null || order.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order and customerId cannot be null or empty");
        }

        // Set bidirectional relationship and calculate subtotals
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                if (item == null || item.getBookId() == null || item.getBookTitle() == null) {
                    throw new IllegalArgumentException("OrderItem and its bookId/bookTitle cannot be null");
                }
                item.setOrder(order);
                item.setSubtotal(item.getQuantity() * item.getUnitPrice());
            }
        } else {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Calculate total amount
        double total = order.getItems().stream()
            .mapToDouble(OrderItem::getSubtotal)
            .sum();
        order.setTotalAmount(total);

        // Set timestamps
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        // Publish events for payment and shipping processing
        try {
            orderEventProducer.publishPaymentRequired(savedOrder);
            orderEventProducer.publishShippingRequired(savedOrder);
        } catch (Exception e) {
            // Log error but don't fail the order creation
            System.err.println("Failed to publish order events: " + e.getMessage());
        }

        return savedOrder;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public List<Order> getOrders(String customerId, String status) {
        if (customerId != null && status != null) {
            return orderRepository.findByCustomerIdAndStatus(
                customerId, OrderStatus.valueOf(status.toUpperCase()));
        } else if (customerId != null) {
            return orderRepository.findByCustomerId(customerId);
        } else if (status != null) {
            return orderRepository.findByStatus(OrderStatus.valueOf(status.toUpperCase()));
        }
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        return null;
    }

    public boolean cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            return true;
        }
        return false;
    }
}