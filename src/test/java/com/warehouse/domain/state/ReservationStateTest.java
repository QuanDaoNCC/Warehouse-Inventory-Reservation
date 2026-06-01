package com.warehouse.domain.state;

import com.warehouse.domain.exception.InvalidStateTransitionException;
import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationStateTest {

    @Test
    void pendingState_allowsConfirmAndCancel() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);

        reservation.confirm();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getUpdatedAt()).isNotNull();

        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setUpdatedAt(null);
        reservation.cancel();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservation.getUpdatedAt()).isNotNull();
    }

    @Test
    void confirmedState_rejectsConfirmAndCancel() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);

        assertThatThrownBy(reservation::confirm)
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(reservation::cancel)
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancelledState_rejectsConfirmAndCancel() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CANCELLED);

        assertThatThrownBy(reservation::confirm)
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(reservation::cancel)
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
