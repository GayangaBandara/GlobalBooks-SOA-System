package com.globalbooks.catalog;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "bookId",
    "quantity"
})
@XmlRootElement(name = "checkAvailabilityRequest", namespace = "http://catalog.globalbooks.com/")
public class CheckAvailabilityRequest {

    @XmlElement(namespace = "http://catalog.globalbooks.com/", required = true)
    protected String bookId;
    @XmlElement(namespace = "http://catalog.globalbooks.com/")
    protected int quantity;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String value) {
        this.bookId = value;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int value) {
        this.quantity = value;
    }
}