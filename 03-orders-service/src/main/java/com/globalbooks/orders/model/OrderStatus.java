package com.globalbooks.orders.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_PROCESSING,
    PAYMENT_COMPLETED,
    SHIPPING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED
}