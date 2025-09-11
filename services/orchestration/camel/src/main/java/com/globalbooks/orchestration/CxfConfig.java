package com.globalbooks.orchestration;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.xml.ws.Endpoint;

@Configuration
public class CxfConfig {

    private final Bus bus;

    public CxfConfig(Bus bus) {
        this.bus = bus;
    }

    @Bean
    public Endpoint placeOrderEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, new PlaceOrderServiceImpl());
        endpoint.setAddress("/placeOrder");
        endpoint.setWsdlLocation("classpath:wsdl/PlaceOrderProcess.wsdl");
        endpoint.publish();
        return endpoint;
    }
}