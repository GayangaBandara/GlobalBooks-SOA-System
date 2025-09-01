package com.globalbooks.shipping.service;

import com.globalbooks.shipping.model.Shipping;
import com.globalbooks.shipping.model.ShippingStatus;
import com.globalbooks.shipping.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShippingService {

    @Autowired
    private ShippingRepository shippingRepository;

    public Shipping createShipping(Long orderId, String customerId, String shippingAddress,
                                   String city, String state, String postalCode, String country,
                                   String carrier) {
        if (orderId == null || customerId == null || customerId.trim().isEmpty() ||
            shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId, customerId, and shippingAddress are required");
        }

        Shipping shipping = new Shipping(orderId, customerId, shippingAddress, carrier);
        shipping.setCity(city);
        shipping.setState(state);
        shipping.setPostalCode(postalCode);
        shipping.setCountry(country != null ? country : "USA");

        // Set estimated delivery (3-7 days from now)
        int daysToAdd = 3 + (int)(Math.random() * 5); // 3-7 days
        shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(daysToAdd));

        return shippingRepository.save(shipping);
    }

    public Shipping createShipment(Shipping shipping) {
        if (shipping == null) {
            throw new IllegalArgumentException("Shipping cannot be null");
        }

        if (shipping.getOrderId() == null || shipping.getShippingAddress() == null ||
            shipping.getShippingAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId and shippingAddress are required");
        }

        // Set default values if not provided
        if (shipping.getCarrier() == null) {
            shipping.setCarrier("FEDEX"); // Default carrier
        }

        if (shipping.getCountry() == null) {
            shipping.setCountry("USA");
        }

        // Set estimated delivery if not set
        if (shipping.getEstimatedDelivery() == null) {
            int daysToAdd = 3 + (int)(Math.random() * 5); // 3-7 days
            shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(daysToAdd));
        }

        // Generate tracking number
        shipping.setTrackingNumber(generateTrackingNumber(shipping.getCarrier()));

        // Set timestamps
        shipping.setCreatedAt(LocalDateTime.now());
        shipping.setUpdatedAt(LocalDateTime.now());

        // Process the shipping
        shipping.setStatus(ShippingStatus.PROCESSING);

        // Simulate shipping completion
        try {
            Thread.sleep(1000); // Simulate processing time

            if (Math.random() > 0.1) {
                shipping.setStatus(ShippingStatus.SHIPPED);
            } else {
                shipping.setStatus(ShippingStatus.FAILED);
                shipping.setNotes("Shipping failed due to carrier issues");
            }
        } catch (InterruptedException e) {
            shipping.setStatus(ShippingStatus.FAILED);
            shipping.setNotes("Shipping processing interrupted");
        }

        return shippingRepository.save(shipping);
    }

    public Shipping processShipping(Long shippingId) {
        Shipping shipping = shippingRepository.findById(shippingId)
            .orElseThrow(() -> new IllegalArgumentException("Shipping not found"));

        if (shipping.getStatus() != ShippingStatus.PENDING) {
            throw new IllegalStateException("Shipping is not in pending state");
        }

        // Generate tracking number
        shipping.setTrackingNumber(generateTrackingNumber(shipping.getCarrier()));

        // Simulate shipping processing
        shipping.setStatus(ShippingStatus.PROCESSING);

        // Simulate shipping completion (90% success rate)
        try {
            Thread.sleep(1000); // Simulate processing time

            if (Math.random() > 0.1) {
                shipping.setStatus(ShippingStatus.SHIPPED);
            } else {
                shipping.setStatus(ShippingStatus.FAILED);
                shipping.setNotes("Shipping failed due to carrier issues");
            }
        } catch (InterruptedException e) {
            shipping.setStatus(ShippingStatus.FAILED);
            shipping.setNotes("Shipping processing interrupted");
        }

        return shippingRepository.save(shipping);
    }

    public Shipping updateShippingStatus(Long shippingId, ShippingStatus newStatus, String notes) {
        Shipping shipping = shippingRepository.findById(shippingId)
            .orElseThrow(() -> new IllegalArgumentException("Shipping not found"));

        shipping.setStatus(newStatus);
        if (notes != null && !notes.trim().isEmpty()) {
            shipping.setNotes(notes);
        }

        return shippingRepository.save(shipping);
    }

    public Shipping getShippingById(Long shippingId) {
        return shippingRepository.findById(shippingId).orElse(null);
    }

    public List<Shipping> getShippingsByOrderId(Long orderId) {
        return shippingRepository.findByOrderId(orderId);
    }

    public List<Shipping> getShippingsByCustomerId(String customerId) {
        return shippingRepository.findByCustomerId(customerId);
    }

    public List<Shipping> getShippingsByStatus(ShippingStatus status) {
        return shippingRepository.findByStatus(status);
    }

    public List<Shipping> getShippingsByCarrier(String carrier) {
        return shippingRepository.findByCarrier(carrier);
    }

    public Shipping getShippingByTrackingNumber(String trackingNumber) {
        return shippingRepository.findByTrackingNumber(trackingNumber).orElse(null);
    }

    public Shipping cancelShipping(Long shippingId) {
        Shipping shipping = shippingRepository.findById(shippingId)
            .orElseThrow(() -> new IllegalArgumentException("Shipping not found"));

        if (shipping.getStatus() == ShippingStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered shipping");
        }

        shipping.setStatus(ShippingStatus.CANCELLED);
        shipping.setNotes("Shipping cancelled by customer");
        shipping.setUpdatedAt(LocalDateTime.now());

        return shippingRepository.save(shipping);
    }

    public Shipping markAsDelivered(Long shippingId) {
        Shipping shipping = shippingRepository.findById(shippingId)
            .orElseThrow(() -> new IllegalArgumentException("Shipping not found"));

        if (shipping.getStatus() != ShippingStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("Shipping must be out for delivery to mark as delivered");
        }

        shipping.setStatus(ShippingStatus.DELIVERED);
        shipping.setNotes("Package delivered successfully");

        return shippingRepository.save(shipping);
    }

    private String generateTrackingNumber(String carrier) {
        String prefix = "";
        switch (carrier != null ? carrier.toUpperCase() : "DEFAULT") {
            case "FEDEX": prefix = "FDX"; break;
            case "UPS": prefix = "UPS"; break;
            case "USPS": prefix = "USPS"; break;
            case "DHL": prefix = "DHL"; break;
            default: prefix = "TRK"; break;
        }
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}