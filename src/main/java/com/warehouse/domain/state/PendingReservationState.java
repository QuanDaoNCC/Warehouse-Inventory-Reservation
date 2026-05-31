package com.warehouse.domain.state;

import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationStatus;

public class PendingReservationState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        reservation.transitionTo(ReservationStatus.CONFIRMED);
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.transitionTo(ReservationStatus.CANCELLED);
    }

    @Override
    public ReservationStatus status() {
        return ReservationStatus.PENDING;
    }
}
