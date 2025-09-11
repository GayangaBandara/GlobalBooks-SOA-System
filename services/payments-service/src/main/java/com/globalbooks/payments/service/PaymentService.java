package com.globalbooks.payments.service;

import com.globalbooks.payments.model.Payment;
import com.globalbooks.payments.model.PaymentStatus;
import com.globalbooks.payments.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment initiatePayment(Long orderId, String customerId, double amount, String paymentMethod) {
        if (orderId == null || customerId == null || customerId.trim().isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid payment parameters");
        }

        Payment payment = new Payment(orderId, customerId, amount, paymentMethod);
        return paymentRepository.save(payment);
    }

    public Payment processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in pending state");
        }

        // Simulate payment processing
        payment.setStatus(PaymentStatus.PROCESSING);

        // Generate transaction ID
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Simulate payment gateway processing (in real implementation, this would call external payment gateway)
        try {
            Thread.sleep(2000); // Simulate processing time

            // Simulate success/failure (90% success rate for demo)
            if (Math.random() > 0.1) {
                payment.setStatus(PaymentStatus.COMPLETED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment gateway declined transaction");
            }
        } catch (InterruptedException e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment processing interrupted");
        }

        return paymentRepository.save(payment);
    }

    public Payment processPayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        // Set processing status if not already set
        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PROCESSING);
        }

        // Generate transaction ID if not present
        if (payment.getTransactionId() == null) {
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        payment.setUpdatedAt(LocalDateTime.now());

        // Simulate payment gateway processing
        try {
            Thread.sleep(2000); // Simulate processing time

            // Simulate success/failure (90% success rate for demo)
            if (Math.random() > 0.1) {
                payment.setStatus(PaymentStatus.COMPLETED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment gateway declined transaction");
            }
        } catch (InterruptedException e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment processing interrupted");
        }

        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }

    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByCustomerId(String customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public Payment refundPayment(Long paymentId, double refundAmount) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        if (refundAmount > payment.getAmount()) {
            throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setFailureReason("Refunded amount: " + refundAmount);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Payment cannot be cancelled in current state");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }
}