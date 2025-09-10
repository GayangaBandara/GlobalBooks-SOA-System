package com.globalbooks.orchestration;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrderResponse", propOrder = {
    "orderId",
    "totalAmount",
    "status",
    "trackingNumber",
    "errorMessage"
})
@XmlRootElement(name = "placeOrderResponse", namespace = "http://bpel.globalbooks.com/")
public class PlaceOrderResponse {

    @XmlElement(required = true)
    protected String orderId;

    @XmlElement(required = true)
    protected BigDecimal totalAmount;

    @XmlElement(required = true)
    protected String status;

    @XmlElement
    protected String trackingNumber;

    @XmlElement
    protected String errorMessage;

    // Getters and setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}