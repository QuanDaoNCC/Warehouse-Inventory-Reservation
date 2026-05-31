package com.warehouse.domain.state;

import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationStatus;

public interface ReservationState {

    void confirm(Reservation reservation);

    void cancel(Reservation reservation);

    ReservationStatus status();
}
