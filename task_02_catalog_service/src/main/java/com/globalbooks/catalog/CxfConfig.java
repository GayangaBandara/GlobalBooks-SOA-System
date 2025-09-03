package com.globalbooks.catalog;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

@Configuration
public class CxfConfig {

    @Autowired
    private Bus cxfBus;

    @Autowired
    private WSSecurityConfig wsSecurityConfig;

    @Bean
    public Endpoint catalogEndpoint(CatalogService catalogService) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, catalogService);
        endpoint.setAddress("/catalog");

        // Add WS-Security interceptors
        endpoint.getInInterceptors().add(wsSecurityConfig.wss4jInInterceptor());
        endpoint.getOutInterceptors().add(wsSecurityConfig.wss4jOutInterceptor());

        return endpoint;
    }
}