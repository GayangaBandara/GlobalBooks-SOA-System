package com.globalbooks.catalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;

@Endpoint
public class CatalogEndpoint {

    private static final String NAMESPACE_URI = "http://catalog.globalbooks.com/";

    @Autowired
    private CatalogService catalogService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "searchBooksRequest")
    @ResponsePayload
    public SearchBooksResponse searchBooks(@RequestPayload SearchBooksRequest request) {
        List<Book> books = catalogService.searchBooks(request.getQuery(), request.getCategory());
        SearchBooksResponse response = new SearchBooksResponse();
        response.getBooks().addAll(books);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBookByIdRequest")
    @ResponsePayload
    public GetBookByIdResponse getBookById(@RequestPayload GetBookByIdRequest request) {
        Book book = catalogService.getBookById(request.getBookId());
        GetBookByIdResponse response = new GetBookByIdResponse();
        response.setBook(book);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBookPriceRequest")
    @ResponsePayload
    public GetBookPriceResponse getBookPrice(@RequestPayload GetBookPriceRequest request) {
        double price = catalogService.getBookPrice(request.getBookId());
        GetBookPriceResponse response = new GetBookPriceResponse();
        response.setPrice(price);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "checkAvailabilityRequest")
    @ResponsePayload
    public CheckAvailabilityResponse checkAvailability(@RequestPayload CheckAvailabilityRequest request) {
        boolean available = catalogService.checkAvailability(request.getBookId(), request.getQuantity());
        CheckAvailabilityResponse response = new CheckAvailabilityResponse();
        response.setAvailable(available);
        return response;
    }
}