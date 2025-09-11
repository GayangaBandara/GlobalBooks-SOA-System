package main.java.com.globalbooks.simpleorchestration;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Service
public class PlaceOrderOrchestrationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public OrchestrationResponse placeOrder(OrchestrationRequest request) {
        OrchestrationResponse response = new OrchestrationResponse();

        try {
            // Step 1: Calculate total price
            BigDecimal totalAmount = calculateTotalPrice(request.getOrderItems());
            response.setTotalAmount(totalAmount);

            // Step 2: Create order
            String orderId = createOrder(request, totalAmount);
            response.setOrderId(orderId);

            // Step 3: Process payment
            String paymentStatus = processPayment(orderId, totalAmount, request.getPaymentMethod());
            response.setStatus(paymentStatus);

            // Step 4: Create shipment if payment successful
            if ("SUCCESS".equals(paymentStatus)) {
                String trackingNumber = createShipment(orderId, request);
                response.setTrackingNumber(trackingNumber);
            }

        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    private BigDecimal calculateTotalPrice(List<Map<String, Object>> orderItems) {
        BigDecimal total = BigDecimal.ZERO;

        for (Map<String, Object> item : orderItems) {
            String bookId = (String) item.get("bookId");
            Integer quantity = (Integer) item.get("quantity");

            // Mock price lookup - in real scenario, call catalog service
            BigDecimal price = getBookPrice(bookId);
            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        return total;
    }

    private BigDecimal getBookPrice(String bookId) {
        // Mock implementation - replace with actual catalog service call
        return BigDecimal.valueOf(50.00); // Mock price
    }

    private String createOrder(OrchestrationRequest request, BigDecimal totalAmount) {
        // Mock implementation - replace with actual orders service call
        return "ORD-" + System.currentTimeMillis();
    }

    private String processPayment(String orderId, BigDecimal amount, String paymentMethod) {
        // Mock implementation - replace with actual payments service call
        return "SUCCESS"; // Mock success
    }

    private String createShipment(String orderId, OrchestrationRequest request) {
        // Mock implementation - replace with actual shipping service call
        return "TRK-" + System.currentTimeMillis();
    }
}