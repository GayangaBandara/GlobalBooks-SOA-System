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
        // Initialize sample data
        bookDatabase.put("978-0134685991", new Book("978-0134685991",
            "Effective Java", "Joshua Bloch", "Programming", 45.99, 100));
        bookDatabase.put("978-0596007126", new Book("978-0596007126",
            "Head First Design Patterns", "Eric Freeman", "Programming", 39.99, 75));
        bookDatabase.put("978-0321356680", new Book("978-0321356680",
            "Effective C++", "Scott Meyers", "Programming", 42.99, 50));
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