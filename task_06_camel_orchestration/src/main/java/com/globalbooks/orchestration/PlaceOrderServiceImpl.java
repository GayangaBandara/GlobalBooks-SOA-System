package com.globalbooks.orchestration;

import org.springframework.stereotype.Service;

import javax.jws.WebService;

@WebService(
    serviceName = "PlaceOrderService",
    portName = "PlaceOrderPort",
    targetNamespace = "http://bpel.globalbooks.com/",
    endpointInterface = "com.globalbooks.orchestration.PlaceOrderService"
)
@Service
public class PlaceOrderServiceImpl implements PlaceOrderService {

    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        // This method will be intercepted by Camel route
        // The actual processing is handled by the Camel route
        return null; // Camel will handle the response
    }
}