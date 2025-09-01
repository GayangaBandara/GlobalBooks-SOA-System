package com.globalbooks.catalog;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.List;

@WebService(name = "CatalogService",
           targetNamespace = "http://catalog.globalbooks.com/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface CatalogService {

    @WebMethod(operationName = "searchBooks")
    List<Book> searchBooks(@WebParam(name = "query") String query,
                          @WebParam(name = "category") String category);

    @WebMethod(operationName = "getBookById")
    Book getBookById(@WebParam(name = "bookId") String bookId);

    @WebMethod(operationName = "getBookPrice")
    double getBookPrice(@WebParam(name = "bookId") String bookId);

    @WebMethod(operationName = "checkAvailability")
    boolean checkAvailability(@WebParam(name = "bookId") String bookId,
                             @WebParam(name = "quantity") int quantity);
}