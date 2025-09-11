package com.globalbooks.orchestration;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.builder.Namespaces;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class PlaceOrderRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        Namespaces namespaces = new Namespaces();
        namespaces.add("ord", "http://bpel.globalbooks.com/");

        // Main orchestration route - SOAP endpoint
        from("cxf:bean:placeOrderEndpoint")
            .routeId("placeOrderSoapProcess")
            .log("=== STARTING SOAP ORDER PROCESS ===")
            .log("Received SOAP order request: ${body}")

            // Extract order data from SOAP envelope
            .setProperty("customerId", xpath("//ord:customerId/text()", String.class, namespaces))
            .setProperty("orderItems", xpath("//ord:orderItems", String.class, namespaces))
            .setProperty("shippingAddress", xpath("//ord:shippingAddress", String.class, namespaces))
            .setProperty("paymentMethod", xpath("//ord:paymentMethod/text()", String.class, namespaces))

            .to("direct:processOrder")
            .log("=== SOAP ORDER PROCESS COMPLETED ===");

        // Direct route for REST API calls
        from("direct:placeOrderProcess")
            .routeId("placeOrderRestProcess")
            .log("=== STARTING REST ORDER PROCESS ===")
            .log("Received REST order request: ${body}")

            // Extract data from PlaceOrderRequest object
            .setProperty("customerId", simple("${body.customerId}"))
            .setProperty("orderItems", simple("${body.orderItems}"))
            .setProperty("shippingAddress", simple("${body.shippingAddress}"))
            .setProperty("paymentMethod", simple("${body.paymentMethod}"))

            .to("direct:processOrder")
            .log("=== REST ORDER PROCESS COMPLETED ===");

        // Common processing route
        from("direct:processOrder")
            .routeId("processOrder")

            // Initialize total amount
            .setProperty("totalAmount", constant(0.0))
            .setProperty("currentItem", constant(0))

            // Process each order item
            .loop(simple("${body.orderItems.size()}"))
                .setProperty("currentItem", simple("${exchangeProperty.currentItem} + 1"))
                .setProperty("bookId", simple("${body.orderItems[${exchangeProperty.currentItem}].bookId}"))
                .setProperty("quantity", simple("${body.orderItems[${exchangeProperty.currentItem}].quantity}"))

                // Call Catalog Service to get book price
                .to("direct:getBookPrice")
                .log("Book price retrieved: ${body}")

                // Calculate subtotal and add to total
                .setProperty("itemPrice", xpath("//price/text()", Double.class))
                .setProperty("subtotal", simple("${exchangeProperty.itemPrice} * ${exchangeProperty.quantity}"))
                .setProperty("totalAmount", simple("${exchangeProperty.totalAmount} + ${exchangeProperty.subtotal}"))
                .log("Current total: ${exchangeProperty.totalAmount}")
            .end()

            // Create order via Orders Service
            .to("direct:createOrder")
            .log("Order created: ${body}")
            .setProperty("orderId", jsonpath("$.orderId"))

            // Process payment
            .to("direct:processPayment")
            .log("Payment processed: ${body}")
            .setProperty("paymentStatus", jsonpath("$.status"))

            // Check payment success and create shipment
            .choice()
                .when(simple("${exchangeProperty.paymentStatus} == 'SUCCESS' || ${exchangeProperty.paymentStatus} == 'COMPLETED'"))
                    .to("direct:createShipment")
                    .log("Shipment created: ${body}")
                    .setProperty("trackingNumber", jsonpath("$.trackingNumber"))
                .otherwise()
                    .setProperty("trackingNumber", constant(""))
            .end()

            // Prepare response
            .setBody(simple("{\"orderId\":\"${exchangeProperty.orderId}\"," +
                           "\"totalAmount\":${exchangeProperty.totalAmount}," +
                           "\"status\":\"${exchangeProperty.paymentStatus}\"," +
                           "\"trackingNumber\":\"${exchangeProperty.trackingNumber}\"}"));


        // Catalog Service Route
        from("direct:getBookPrice")
            .routeId("catalogServiceRoute")
            .log("Calling Catalog Service for book: ${exchangeProperty.bookId}")
            .setHeader("Content-Type", constant("text/xml"))
            .setBody(simple("<getBookPriceRequest xmlns=\"http://catalog.globalbooks.com/\"><bookId>${exchangeProperty.bookId}</bookId></getBookPriceRequest>"))
            .to("http://catalog-service:8080/ws")
            .convertBodyTo(String.class);

        // Orders Service Route
        from("direct:createOrder")
            .routeId("ordersServiceRoute")
            .log("Creating order for customer: ${exchangeProperty.customerId}")
            .setHeader("Content-Type", constant("application/json"))
            .setBody(simple("{\"customerId\":\"${exchangeProperty.customerId}\"," +
                           "\"items\":${exchangeProperty.orderItems}," +
                           "\"totalAmount\":${exchangeProperty.totalAmount}}"))
            .to("http://orders-service:8081/api/v1/orders?bridgeEndpoint=true")
            .convertBodyTo(String.class);

        // Payments Service Route
        from("direct:processPayment")
            .routeId("paymentsServiceRoute")
            .log("Processing payment for order: ${exchangeProperty.orderId}")
            .setHeader("Content-Type", constant("application/json"))
            .setBody(simple("{\"orderId\":${exchangeProperty.orderId}," +
                           "\"customerId\":\"${exchangeProperty.customerId}\"," +
                           "\"amount\":${exchangeProperty.totalAmount}," +
                           "\"paymentMethod\":\"${exchangeProperty.paymentMethod}\"}"))
            .to("http://payments-service:8083/api/v1/payments/initiate?bridgeEndpoint=true")
            .convertBodyTo(String.class);

        // Shipping Service Route
        from("direct:createShipment")
            .routeId("shippingServiceRoute")
            .log("Creating shipment for order: ${exchangeProperty.orderId}")
            .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
            .setBody(simple("orderId=${exchangeProperty.orderId}&customerId=${exchangeProperty.customerId}&shippingAddress=${exchangeProperty.shippingAddress}&carrier=FedEx"))
            .to("http://shipping-service:8084/api/v1/shippings?bridgeEndpoint=true")
            .convertBodyTo(String.class);
    }
}