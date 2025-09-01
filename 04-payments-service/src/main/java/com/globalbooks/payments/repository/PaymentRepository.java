package com.globalbooks.payments.repository;

import com.globalbooks.payments.model.Payment;
import com.globalbooks.payments.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByCustomerId(String customerId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByCustomerIdAndStatus(String customerId, PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
}