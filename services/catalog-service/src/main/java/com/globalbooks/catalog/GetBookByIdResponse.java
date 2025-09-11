package com.globalbooks.catalog;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "book"
})
@XmlRootElement(name = "getBookByIdResponse", namespace = "http://catalog.globalbooks.com/")
public class GetBookByIdResponse {

    @XmlElement(namespace = "http://catalog.globalbooks.com/")
    protected Book book;

    public Book getBook() {
        return book;
    }

    public void setBook(Book value) {
        this.book = value;
    }
}