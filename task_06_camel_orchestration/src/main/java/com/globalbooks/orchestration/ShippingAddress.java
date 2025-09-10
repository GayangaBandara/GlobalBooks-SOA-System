package com.globalbooks.orchestration;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShippingAddress", propOrder = {
    "street",
    "city",
    "country",
    "postalCode"
})
public class ShippingAddress {

    @XmlElement(required = true)
    protected String street;

    @XmlElement(required = true)
    protected String city;

    @XmlElement(required = true)
    protected String country;

    @XmlElement(required = true)
    protected String postalCode;

    // Getters and setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}