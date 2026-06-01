package com.warehouse.application.port;

import com.warehouse.domain.model.Reservation;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {

    Optional<Reservation> findById(UUID id);

    Optional<Reservation> findByIdForUpdate(UUID id);

    boolean existsByOrderId(String orderId);

    Reservation save(Reservation reservation);
}
