package com.globalbooks.payments.controller;

import com.globalbooks.payments.model.Payment;
import com.globalbooks.payments.model.PaymentStatus;
import com.globalbooks.payments.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<Payment> initiatePayment(
            @RequestParam Long orderId,
            @RequestParam String customerId,
            @RequestParam double amount,
            @RequestParam String paymentMethod) {
        try {
            Payment payment = paymentService.initiatePayment(orderId, customerId, amount, paymentMethod);
            return new ResponseEntity<>(payment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<Payment> processPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.processPayment(paymentId);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        if (payment != null) {
            return new ResponseEntity<>(payment, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Payment>> getPaymentsByCustomerId(@PathVariable String customerId) {
        List<Payment> payments = paymentService.getPaymentsByCustomerId(customerId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getPaymentsByStatus(
            @RequestParam(required = false) PaymentStatus status) {
        List<Payment> payments;
        if (status != null) {
            payments = paymentService.getPaymentsByStatus(status);
        } else {
            // In a real implementation, you might want to add a method to get all payments
            payments = paymentService.getPaymentsByStatus(PaymentStatus.PENDING);
        }
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Payment> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam double refundAmount) {
        try {
            Payment payment = paymentService.refundPayment(paymentId, refundAmount);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Payment> cancelPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.cancelPayment(paymentId);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("Payments Service is running", HttpStatus.OK);
    }
}