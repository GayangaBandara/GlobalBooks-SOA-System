package com.globalbooks.catalog;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "book", namespace = "http://catalog.globalbooks.com/")
@XmlType(propOrder = {"isbn", "title", "author", "category", "price", "stock"})
public class Book {
    private String isbn;
    private String title;
    private String author;
    private String category;
    private double price;
    private int stock;

    public Book() {}

    public Book(String isbn, String title, String author,
               String category, double price, int stock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}