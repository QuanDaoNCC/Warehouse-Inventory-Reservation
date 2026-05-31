package com.warehouse.application.dto;

import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        String orderId,
        ReservationStatus status,
        Instant createdAt,
        List<ReservationItemResponse> items
) {

    public static ReservationResponse from(Reservation reservation) {
        List<ReservationItemResponse> items = reservation.getItems().stream()
                .map(item -> new ReservationItemResponse(item.getSku(), item.getQuantity()))
                .toList();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                items
        );
    }
}
