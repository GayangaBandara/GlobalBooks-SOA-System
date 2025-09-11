package com.globalbooks.catalog;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "available"
})
@XmlRootElement(name = "checkAvailabilityResponse", namespace = "http://catalog.globalbooks.com/")
public class CheckAvailabilityResponse {

    @XmlElement(namespace = "http://catalog.globalbooks.com/")
    protected boolean available;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean value) {
        this.available = value;
    }
}