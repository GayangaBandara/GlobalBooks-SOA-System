package com.globalbooks.orchestration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestration")
@CrossOrigin(origins = "*")
public class OrchestrationController {

    @Autowired
    private PlaceOrderService placeOrderService;

    @PostMapping("/place-order")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        try {
            PlaceOrderResponse response = placeOrderService.placeOrder(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("Orchestration Service is running", HttpStatus.OK);
    }
}