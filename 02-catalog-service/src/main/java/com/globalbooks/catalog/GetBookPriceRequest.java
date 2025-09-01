package com.globalbooks.catalog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "bookId"
})
@XmlRootElement(name = "getBookPriceRequest", namespace = "http://catalog.globalbooks.com/")
public class GetBookPriceRequest {

    @XmlElement(namespace = "http://catalog.globalbooks.com/", required = true)
    protected String bookId;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String value) {
        this.bookId = value;
    }
}