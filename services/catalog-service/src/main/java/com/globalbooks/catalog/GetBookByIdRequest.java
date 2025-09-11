package com.globalbooks.catalog;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "bookId"
})
@XmlRootElement(name = "getBookByIdRequest", namespace = "http://catalog.globalbooks.com/")
public class GetBookByIdRequest {

    @XmlElement(namespace = "http://catalog.globalbooks.com/", required = true)
    protected String bookId;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String value) {
        this.bookId = value;
    }
}