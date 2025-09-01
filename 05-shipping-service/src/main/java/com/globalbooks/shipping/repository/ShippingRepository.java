package com.globalbooks.shipping.repository;

import com.globalbooks.shipping.model.Shipping;
import com.globalbooks.shipping.model.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long> {
    List<Shipping> findByOrderId(Long orderId);
    List<Shipping> findByCustomerId(String customerId);
    List<Shipping> findByStatus(ShippingStatus status);
    List<Shipping> findByCustomerIdAndStatus(String customerId, ShippingStatus status);
    List<Shipping> findByCarrier(String carrier);
    Optional<Shipping> findByTrackingNumber(String trackingNumber);
}