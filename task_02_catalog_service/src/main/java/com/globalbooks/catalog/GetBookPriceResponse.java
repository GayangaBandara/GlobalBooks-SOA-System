package com.globalbooks.catalog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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