package com.globalbooks.catalog;

import javax.jws.WebService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebService(endpointInterface = "com.globalbooks.catalog.CatalogService",
        serviceName = "CatalogService",
        targetNamespace = "http://catalog.globalbooks.com/")
public class CatalogServiceImpl implements CatalogService {

    private static final Map<String, Book> bookDatabase = new HashMap<>();

    static {
        bookDatabase.put("978-1491904244", new Book("978-1491904244",
                "Designing Data-Intensive Applications", "Martin Kleppmann", "Databases", 59.99, 150));
        bookDatabase.put("978-0132350884", new Book("978-0132350884",
                "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "Software Engineering", 49.99, 200));
        bookDatabase.put("978-0201633610", new Book("978-0201633610",
                "Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma", "Software Design", 54.99, 120));
    }

    @Override
    public List<Book> searchBooks(String query, String category) {
        return bookDatabase.values().stream()
                .filter(book -> (query == null || book.getTitle().toLowerCase()
                        .contains(query.toLowerCase()) ||
                        book.getAuthor().toLowerCase().contains(query.toLowerCase())))
                .filter(book -> (category == null || book.getCategory()
                        .equalsIgnoreCase(category)))
                .collect(Collectors.toList());
    }

    @Override
    public Book getBookById(String bookId) {
        return bookDatabase.get(bookId);
    }

    @Override
    public double getBookPrice(String bookId) {
        Book book = bookDatabase.get(bookId);
        return book != null ? book.getPrice() : 0.0;
    }

    @Override
    public boolean checkAvailability(String bookId, int quantity) {
        Book book = bookDatabase.get(bookId);
        return book != null && book.getStock() >= quantity;
    }
}