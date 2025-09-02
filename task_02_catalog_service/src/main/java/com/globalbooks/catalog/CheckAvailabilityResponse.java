package com.globalbooks.catalog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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