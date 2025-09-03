package com.globalbooks.catalog;

import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
@Import(WSSecurityConfig.class)
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/catalog/soap/*");
    }

    @Bean(name = "catalog")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema catalogSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("CatalogPortType");
        wsdl11Definition.setLocationUri("/catalog/soap");
        wsdl11Definition.setTargetNamespace("http://catalog.globalbooks.com/");
        wsdl11Definition.setSchema(catalogSchema);
        return wsdl11Definition;
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.globalbooks.catalog");
        return marshaller;
    }

    @Bean
    public XsdSchema catalogSchema() {
        return new SimpleXsdSchema(new ClassPathResource("wsdl/CatalogService.xsd"));
    }

    @Bean
    public CatalogService catalogService() {
        return new CatalogServiceImpl();
    }

    // WS-Security Interceptors Integration
    @Bean
    public org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor payloadLoggingInterceptor() {
        return new org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor();
    }

    @Bean
    public org.springframework.ws.client.support.interceptor.ClientInterceptor[] clientInterceptors(
            WSS4JOutInterceptor wss4jOutInterceptor) {
        return new org.springframework.ws.client.support.interceptor.ClientInterceptor[]{wss4jOutInterceptor};
    }
}