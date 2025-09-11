package com.globalbooks.catalog;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "price"
})
@XmlRootElement(name = "getBookPriceResponse", namespace = "http://catalog.globalbooks.com/")
public class GetBookPriceResponse {

    @XmlElement(namespace = "http://catalog.globalbooks.com/")
    protected double price;

    public double getPrice() {
        return price;
    }

    public void setPrice(double value) {
        this.price = value;
    }
}