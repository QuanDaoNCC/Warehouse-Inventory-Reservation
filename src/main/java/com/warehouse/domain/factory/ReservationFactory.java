package com.warehouse.domain.factory;

import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationItem;
import com.warehouse.domain.model.ReservationStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ReservationFactory {

    public Reservation create(String orderId, List<ItemRequest> itemRequests) {
        validate(orderId, itemRequests);

        Instant now = Instant.now();

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setOrderId(orderId);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(now);
        reservation.setUpdatedAt(now);

        for (ItemRequest request : itemRequests) {
            ReservationItem item = new ReservationItem();
            item.setSku(request.sku());
            item.setQuantity(request.quantity());
            reservation.addItem(item);
        }

        return reservation;
    }

    private void validate(String orderId, List<ItemRequest> itemRequests) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("itemRequests must not be empty");
        }
        for (ItemRequest request : itemRequests) {
            if (request == null) {
                throw new IllegalArgumentException("itemRequests must not contain null items");
            }
            if (request.sku() == null || request.sku().isBlank()) {
                throw new IllegalArgumentException("item sku must not be blank");
            }
            if (request.quantity() <= 0) {
                throw new IllegalArgumentException("item quantity must be greater than zero");
            }
        }
    }

    public record ItemRequest(String sku, int quantity) {
    }
}
