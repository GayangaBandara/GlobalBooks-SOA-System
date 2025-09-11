package com.globalbooks.orchestration;

import org.springframework.stereotype.Service;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jws.WebService;
import java.math.BigDecimal;

@WebService(
    serviceName = "PlaceOrderService",
    portName = "PlaceOrderPort",
    targetNamespace = "http://bpel.globalbooks.com/",
    endpointInterface = "com.globalbooks.orchestration.PlaceOrderService"
)
@Service
public class PlaceOrderServiceImpl implements PlaceOrderService {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        try {
            // Send request to Camel route
            String result = producerTemplate.requestBody("direct:placeOrderProcess", request, String.class);

            // Parse the result and create response
            PlaceOrderResponse response = new PlaceOrderResponse();
            // For now, return a simple response
            response.setOrderId("ORD-" + System.currentTimeMillis());
            response.setStatus("SUCCESS");
            response.setTotalAmount(BigDecimal.valueOf(0.0));
            response.setTrackingNumber("TRACK-" + System.currentTimeMillis());

            return response;
        } catch (Exception e) {
            PlaceOrderResponse response = new PlaceOrderResponse();
            response.setStatus("FAILED");
            return response;
        }
    }
}