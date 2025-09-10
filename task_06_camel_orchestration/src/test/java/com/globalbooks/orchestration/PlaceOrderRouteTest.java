package com.globalbooks.orchestration;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest(classes = CamelOrchestrationApplication.class)
class PlaceOrderRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Test
    void testPlaceOrderRouteExists() {
        // Test that the main orchestration route is properly registered
        assertThat(camelContext.getRoute("placeOrderProcess")).isNotNull();
        assertThat(camelContext.getRoute("placeOrderProcess").getId()).isEqualTo("placeOrderProcess");
    }

    @Test
    void testCatalogServiceRouteExists() {
        // Test that catalog service route is available
        assertThat(camelContext.getRoute("catalogServiceRoute")).isNotNull();
    }

    @Test
    void testOrdersServiceRouteExists() {
        // Test that orders service route is available
        assertThat(camelContext.getRoute("ordersServiceRoute")).isNotNull();
    }

    @Test
    void testPaymentsServiceRouteExists() {
        // Test that payments service route is available
        assertThat(camelContext.getRoute("paymentsServiceRoute")).isNotNull();
    }

    @Test
    void testShippingServiceRouteExists() {
        // Test that shipping service route is available
        assertThat(camelContext.getRoute("shippingServiceRoute")).isNotNull();
    }

    @Test
    void testCamelContextIsStarted() {
        // Test that Camel context is running
        assertThat(camelContext.isStarted()).isTrue();
    }

    @Test
    void testRouteDefinitions() {
        // Test that routes have proper definitions
        assertThat(camelContext.getRoutes()).isNotEmpty();
        assertThat(camelContext.getRoutes().size()).isGreaterThanOrEqualTo(5);
    }
}