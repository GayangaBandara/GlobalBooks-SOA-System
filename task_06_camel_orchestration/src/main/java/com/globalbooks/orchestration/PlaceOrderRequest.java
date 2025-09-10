package com.globalbooks.orchestration;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrderRequest", propOrder = {
    "customerId",
    "orderItems",
    "shippingAddress",
    "paymentMethod"
})
@XmlRootElement(name = "placeOrderRequest", namespace = "http://bpel.globalbooks.com/")
public class PlaceOrderRequest {

    @XmlElement(required = true)
    protected String customerId;

    @XmlElement(required = true)
    protected OrderItems orderItems;

    @XmlElement(required = true)
    protected ShippingAddress shippingAddress;

    @XmlElement(required = true)
    protected String paymentMethod;

    // Getters and setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OrderItems getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(OrderItems orderItems) {
        this.orderItems = orderItems;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}