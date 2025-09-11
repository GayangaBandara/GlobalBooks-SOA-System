package main.java.com.globalbooks.simpleorchestration;

import java.util.List;
import java.util.Map;

public class OrchestrationRequest {
    private String customerId;
    private List<Map<String, Object>> orderItems;
    private Map<String, Object> shippingAddress;
    private String paymentMethod;

    // Getters and setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<Map<String, Object>> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<Map<String, Object>> orderItems) {
        this.orderItems = orderItems;
    }

    public Map<String, Object> getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Map<String, Object> shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}