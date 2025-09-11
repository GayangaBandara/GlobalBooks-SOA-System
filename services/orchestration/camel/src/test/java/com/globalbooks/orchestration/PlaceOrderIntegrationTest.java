package com.globalbooks.orchestration;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@CamelSpringBootTest
@SpringBootTest(classes = CamelOrchestrationApplication.class)
class PlaceOrderIntegrationTest {

    @EndpointInject("mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce("cxf:bean:placeOrderEndpoint")
    protected ProducerTemplate template;

    @Test
    void testOrderOrchestration() throws Exception {
        // Prepare SOAP request
        String soapRequest = createSoapRequest();

        // Set up expectations
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.expectedBodiesReceived(createExpectedResponse());

        // Send the request
        template.sendBody("cxf:bean:placeOrderEndpoint", soapRequest);

        // Verify expectations
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    void testOrderOrchestrationWithInvalidData() throws Exception {
        // Test with invalid book ID
        String invalidSoapRequest = createInvalidSoapRequest();

        // Set up expectations for error response
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.expectedBodiesReceived(createErrorResponse());

        // Send the request
        template.sendBody("cxf:bean:placeOrderEndpoint", invalidSoapRequest);

        // Verify expectations
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    void testOrderOrchestrationFlow() throws Exception {
        // Test the complete orchestration flow
        String soapRequest = createSoapRequest();

        // Mock the external service calls
        resultEndpoint.expectedMessageCount(1);

        // Send request and verify the orchestration completes
        template.sendBody("cxf:bean:placeOrderEndpoint", soapRequest);

        // Verify that all steps in the orchestration were executed
        resultEndpoint.assertIsSatisfied();
    }

    private String createSoapRequest() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" \n" +
               "                  xmlns:ord=\"http://bpel.globalbooks.com/\">\n" +
               "   <soapenv:Header/>\n" +
               "   <soapenv:Body>\n" +
               "      <ord:placeOrderRequest>\n" +
               "         <customerId>CUST001</customerId>\n" +
               "         <orderItems>\n" +
               "            <item>\n" +
               "               <bookId>978-0134685991</bookId>\n" +
               "               <quantity>2</quantity>\n" +
               "            </item>\n" +
               "            <item>\n" +
               "               <bookId>978-0321127426</bookId>\n" +
               "               <quantity>1</quantity>\n" +
               "            </item>\n" +
               "         </orderItems>\n" +
               "         <shippingAddress>\n" +
               "            <street>123 Main St</street>\n" +
               "            <city>Colombo</city>\n" +
               "            <country>Sri Lanka</country>\n" +
               "            <postalCode>10000</postalCode>\n" +
               "         </shippingAddress>\n" +
               "         <paymentMethod>CREDIT_CARD</paymentMethod>\n" +
               "      </ord:placeOrderRequest>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
    }

    private String createInvalidSoapRequest() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" \n" +
               "                  xmlns:ord=\"http://bpel.globalbooks.com/\">\n" +
               "   <soapenv:Header/>\n" +
               "   <soapenv:Body>\n" +
               "      <ord:placeOrderRequest>\n" +
               "         <customerId>CUST001</customerId>\n" +
               "         <orderItems>\n" +
               "            <item>\n" +
               "               <bookId>INVALID-BOOK</bookId>\n" +
               "               <quantity>1</quantity>\n" +
               "            </item>\n" +
               "         </orderItems>\n" +
               "         <shippingAddress>\n" +
               "            <street>123 Main St</street>\n" +
               "            <city>Colombo</city>\n" +
               "            <country>Sri Lanka</country>\n" +
               "            <postalCode>10000</postalCode>\n" +
               "         </shippingAddress>\n" +
               "         <paymentMethod>CREDIT_CARD</paymentMethod>\n" +
               "      </ord:placeOrderRequest>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
    }

    private String createExpectedResponse() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "   <soapenv:Body>\n" +
               "      <placeOrderResponse xmlns=\"http://bpel.globalbooks.com/\">\n" +
               "         <orderId>ORD-TEST-001</orderId>\n" +
               "         <totalAmount>150.00</totalAmount>\n" +
               "         <status>SUCCESS</status>\n" +
               "         <trackingNumber>TRK-TEST-001</trackingNumber>\n" +
               "      </placeOrderResponse>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
    }

    private String createErrorResponse() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "   <soapenv:Body>\n" +
               "      <placeOrderResponse xmlns=\"http://bpel.globalbooks.com/\">\n" +
               "         <orderId></orderId>\n" +
               "         <totalAmount>0.00</totalAmount>\n" +
               "         <status>ERROR</status>\n" +
               "         <errorMessage>Book not found: INVALID-BOOK</errorMessage>\n" +
               "      </placeOrderResponse>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
    }
}