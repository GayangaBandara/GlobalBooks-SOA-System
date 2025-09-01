package com.globalbooks.shipping.controller;

import com.globalbooks.shipping.model.Shipping;
import com.globalbooks.shipping.model.ShippingStatus;
import com.globalbooks.shipping.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shippings")
@CrossOrigin(origins = "*")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    @PostMapping
    public ResponseEntity<Shipping> createShipping(
            @RequestParam Long orderId,
            @RequestParam String customerId,
            @RequestParam String shippingAddress,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) String country,
            @RequestParam String carrier) {
        try {
            Shipping shipping = shippingService.createShipping(orderId, customerId, shippingAddress,
                    city, state, postalCode, country, carrier);
            return new ResponseEntity<>(shipping, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{shippingId}/process")
    public ResponseEntity<Shipping> processShipping(@PathVariable Long shippingId) {
        try {
            Shipping shipping = shippingService.processShipping(shippingId);
            return new ResponseEntity<>(shipping, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{shippingId}/status")
    public ResponseEntity<Shipping> updateShippingStatus(
            @PathVariable Long shippingId,
            @RequestParam ShippingStatus status,
            @RequestParam(required = false) String notes) {
        try {
            Shipping shipping = shippingService.updateShippingStatus(shippingId, status, notes);
            return new ResponseEntity<>(shipping, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{shippingId}")
    public ResponseEntity<Shipping> getShippingById(@PathVariable Long shippingId) {
        Shipping shipping = shippingService.getShippingById(shippingId);
        if (shipping != null) {
            return new ResponseEntity<>(shipping, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Shipping>> getShippingsByOrderId(@PathVariable Long orderId) {
        List<Shipping> shippings = shippingService.getShippingsByOrderId(orderId);
        return new ResponseEntity<>(shippings, HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Shipping>> getShippingsByCustomerId(@PathVariable String customerId) {
        List<Shipping> shippings = shippingService.getShippingsByCustomerId(customerId);
        return new ResponseEntity<>(shippings, HttpStatus.OK);
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Shipping> getShippingByTrackingNumber(@PathVariable String trackingNumber) {
        Shipping shipping = shippingService.getShippingByTrackingNumber(trackingNumber);
        if (shipping != null) {
            return new ResponseEntity<>(shipping, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<Shipping>> getShippingsByStatus(
            @RequestParam(required = false) ShippingStatus status,
            @RequestParam(required = false) String carrier) {
        List<Shipping> shippings;
        if (status != null) {
            shippings = shippingService.getShippingsByStatus(status);
        } else if (carrier != null) {
            shippings = shippingService.getShippingsByCarrier(carrier);
        } else {
            // Return all shippings with PENDING status as default
            shippings = shippingService.getShippingsByStatus(ShippingStatus.PENDING);
        }
        return new ResponseEntity<>(shippings, HttpStatus.OK);
    }

    @PostMapping("/{shippingId}/cancel")
    public ResponseEntity<Shipping> cancelShipping(@PathVariable Long shippingId) {
        try {
            Shipping shipping = shippingService.cancelShipping(shippingId);
            return new ResponseEntity<>(shipping, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{shippingId}/deliver")
    public ResponseEntity<Shipping> markAsDelivered(@PathVariable Long shippingId) {
        try {
            Shipping shipping = shippingService.markAsDelivered(shippingId);
            return new ResponseEntity<>(shipping, HttpStatus.OK);
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
        return new ResponseEntity<>("Shipping Service is running", HttpStatus.OK);
    }
}