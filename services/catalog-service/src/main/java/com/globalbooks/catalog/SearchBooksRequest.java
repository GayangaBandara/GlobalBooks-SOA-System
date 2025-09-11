package com.globalbooks.catalog;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "query",
    "category"
})
@XmlRootElement(name = "searchBooksRequest", namespace = "http://catalog.globalbooks.com/")
public class SearchBooksRequest {

    @XmlElement(namespace = "http://catalog.globalbooks.com/")
    protected String query;
    @XmlElement(namespace = "http://catalog.globalbooks.com/")
    protected String category;

    public String getQuery() {
        return query;
    }

    public void setQuery(String value) {
        this.query = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String value) {
        this.category = value;
    }
}