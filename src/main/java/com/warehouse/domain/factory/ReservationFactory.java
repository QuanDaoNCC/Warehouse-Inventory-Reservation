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
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setOrderId(orderId);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(Instant.now());

        for (ItemRequest request : itemRequests) {
            ReservationItem item = new ReservationItem();
            item.setSku(request.sku());
            item.setQuantity(request.quantity());
            reservation.addItem(item);
        }

        return reservation;
    }

    public record ItemRequest(String sku, int quantity) {
    }
}
