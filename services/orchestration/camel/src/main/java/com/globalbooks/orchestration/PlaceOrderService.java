package com.globalbooks.orchestration;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace = "http://bpel.globalbooks.com/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public interface PlaceOrderService {

    @WebMethod(operationName = "placeOrder")
    @WebResult(name = "placeOrderResponse", targetNamespace = "http://bpel.globalbooks.com/")
    PlaceOrderResponse placeOrder(
        @WebParam(name = "placeOrderRequest", targetNamespace = "http://bpel.globalbooks.com/")
        PlaceOrderRequest request
    );
}