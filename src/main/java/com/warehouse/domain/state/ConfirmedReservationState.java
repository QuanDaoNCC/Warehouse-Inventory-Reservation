package com.warehouse.domain.state;

import com.warehouse.domain.exception.InvalidStateTransitionException;
import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationStatus;

public class ConfirmedReservationState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        throw new InvalidStateTransitionException(ReservationStatus.CONFIRMED, "confirm");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new InvalidStateTransitionException(ReservationStatus.CONFIRMED, "cancel");
    }

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CONFIRMED;
    }
}
