package com.warehouse.domain.state;

import com.warehouse.domain.exception.InvalidStateTransitionException;
import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationStatus;

public class CancelledReservationState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        throw new InvalidStateTransitionException(ReservationStatus.CANCELLED, "confirm");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new InvalidStateTransitionException(ReservationStatus.CANCELLED, "cancel");
    }

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CANCELLED;
    }
}
