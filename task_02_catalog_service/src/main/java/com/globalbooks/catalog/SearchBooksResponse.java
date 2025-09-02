package com.globalbooks.catalog;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "books"
})
@XmlRootElement(name = "searchBooksResponse", namespace = "http://catalog.globalbooks.com/")
public class SearchBooksResponse {

    @XmlElement(namespace = "http://catalog.globalbooks.com/")
    protected List<Book> books;

    public List<Book> getBooks() {
        if (books == null) {
            books = new ArrayList<Book>();
        }
        return this.books;
    }
}