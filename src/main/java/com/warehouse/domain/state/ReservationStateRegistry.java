package com.warehouse.domain.state;

import com.warehouse.domain.model.ReservationStatus;

import java.util.Map;

public final class ReservationStateRegistry {

    private static final Map<ReservationStatus, ReservationState> STATES = Map.of(
            ReservationStatus.PENDING, new PendingReservationState(),
            ReservationStatus.CONFIRMED, new ConfirmedReservationState(),
            ReservationStatus.CANCELLED, new CancelledReservationState()
    );

    private ReservationStateRegistry() {
    }

    public static ReservationState forStatus(ReservationStatus status) {
        ReservationState state = STATES.get(status);
        if (state == null) {
            throw new IllegalArgumentException("Unknown reservation status: " + status);
        }
        return state;
    }
}
