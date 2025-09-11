package com.globalbooks.catalog;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;

import com.globalbooks.catalog.*;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public CatalogService catalogService() {
        return new CatalogServiceImpl();
    }

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "catalog")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema catalogSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("CatalogPortType");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://catalog.globalbooks.com/");
        wsdl11Definition.setSchema(catalogSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema catalogSchema() {
        return new SimpleXsdSchema(new ClassPathResource("wsdl/CatalogService.xsd"));
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(
            SearchBooksRequest.class,
            SearchBooksResponse.class,
            GetBookByIdRequest.class,
            GetBookByIdResponse.class,
            GetBookPriceRequest.class,
            GetBookPriceResponse.class,
            CheckAvailabilityRequest.class,
            CheckAvailabilityResponse.class,
            Book.class
        );
        return marshaller;
    }

}