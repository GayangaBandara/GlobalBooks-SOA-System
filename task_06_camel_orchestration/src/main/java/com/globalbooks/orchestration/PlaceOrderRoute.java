package main.java.com.globalbooks.orchestration;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.builder.xml.XPathBuilder;
import org.springframework.stereotype.Component;

@Component
public class PlaceOrderRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        namespaces().add("ord", "http://bpel.globalbooks.com/");

        // Main orchestration route - SOAP endpoint
        from("cxf:bean:placeOrderEndpoint")
            .routeId("placeOrderProcess")
            .log("=== STARTING ORDER PROCESS ===")
            .log("Received order request: ${body}")

            // Extract order data from SOAP envelope
            .setProperty("customerId", xpath("//ord:customerId/text()", String.class))
            .setProperty("orderItems", xpath("//ord:orderItems", String.class))
            .setProperty("shippingAddress", xpath("//ord:shippingAddress", String.class))
            .setProperty("paymentMethod", xpath("//ord:paymentMethod/text()", String.class))

            // Initialize total amount
            .setProperty("totalAmount", constant(0.0))
            .setProperty("currentItem", constant(0))

            // Process each order item
            .loop(xpath("count(//ord:item)", Integer.class))
                .setProperty("currentItem", simple("${exchangeProperty.currentItem} + 1"))
                .setProperty("bookId", xpath("//ord:item[${exchangeProperty.currentItem}]/ord:bookId/text()", String.class))
                .setProperty("quantity", xpath("//ord:item[${exchangeProperty.currentItem}]/ord:quantity/text()", Integer.class))

                // Call Catalog Service to get book price
                .to("direct:getBookPrice")
                .log("Book price retrieved: ${body}")

                // Calculate subtotal and add to total
                .setProperty("itemPrice", jsonpath("$.price"))
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
                .when(simple("${exchangeProperty.paymentStatus} == 'SUCCESS'"))
                    .to("direct:createShipment")
                    .log("Shipment created: ${body}")
                    .setProperty("trackingNumber", jsonpath("$.trackingNumber"))
                .otherwise()
                    .setProperty("trackingNumber", constant(""))
            .end()

            // Prepare response
            .setBody(simple("<placeOrderResponse xmlns=\"http://bpel.globalbooks.com/\">" +
                          "<orderId>${exchangeProperty.orderId}</orderId>" +
                          "<totalAmount>${exchangeProperty.totalAmount}</totalAmount>" +
                          "<status>${exchangeProperty.paymentStatus}</status>" +
                          "<trackingNumber>${exchangeProperty.trackingNumber}</trackingNumber>" +
                          "</placeOrderResponse>"))

            .log("=== ORDER PROCESS COMPLETED ===")
            .log("Final response: ${body}");

        // Catalog Service Route
        from("direct:getBookPrice")
            .routeId("catalogServiceRoute")
            .log("Calling Catalog Service for book: ${exchangeProperty.bookId}")
            .setHeader("Content-Type", constant("application/json"))
            .setBody(simple("{\"bookId\":\"${exchangeProperty.bookId}\"}"))
            .to("http://localhost:8080/catalog/api/books/price?bridgeEndpoint=true")
            .convertBodyTo(String.class);

        // Orders Service Route
        from("direct:createOrder")
            .routeId("ordersServiceRoute")
            .log("Creating order for customer: ${exchangeProperty.customerId}")
            .setHeader("Content-Type", constant("application/json"))
            .setBody(simple("{\"customerId\":\"${exchangeProperty.customerId}\"," +
                          "\"items\":${exchangeProperty.orderItems}," +
                          "\"totalAmount\":${exchangeProperty.totalAmount}}"))
            .to("http://localhost:8081/api/v1/orders?bridgeEndpoint=true")
            .convertBodyTo(String.class);

        // Payments Service Route
        from("direct:processPayment")
            .routeId("paymentsServiceRoute")
            .log("Processing payment for order: ${exchangeProperty.orderId}")
            .setHeader("Content-Type", constant("application/json"))
            .setBody(simple("{\"orderId\":\"${exchangeProperty.orderId}\"," +
                          "\"amount\":${exchangeProperty.totalAmount}," +
                          "\"paymentMethod\":\"${exchangeProperty.paymentMethod}\"}"))
            .to("http://localhost:8083/api/v1/payments?bridgeEndpoint=true")
            .convertBodyTo(String.class);

        // Shipping Service Route
        from("direct:createShipment")
            .routeId("shippingServiceRoute")
            .log("Creating shipment for order: ${exchangeProperty.orderId}")
            .setHeader("Content-Type", constant("application/json"))
            .setBody(simple("{\"orderId\":\"${exchangeProperty.orderId}\"," +
                          "\"shippingAddress\":${exchangeProperty.shippingAddress}," +
                          "\"items\":${exchangeProperty.orderItems}}"))
            .to("http://localhost:8084/api/v1/shipping?bridgeEndpoint=true")
            .convertBodyTo(String.class);
    }
}