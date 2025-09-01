package com.globalbooks.catalog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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