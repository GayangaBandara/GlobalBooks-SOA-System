package com.globalbooks.orchestration;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderItems", propOrder = {
    "item"
})
public class OrderItems {

    @XmlElement(required = true)
    protected List<OrderItem> item;

    public List<OrderItem> getItem() {
        return item;
    }

    public void setItem(List<OrderItem> item) {
        this.item = item;
    }
}