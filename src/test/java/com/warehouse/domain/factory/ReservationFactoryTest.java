package com.warehouse.domain.factory;

import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationFactoryTest {

    private final ReservationFactory factory = new ReservationFactory();

    @Test
    void create_buildsPendingReservationWithItems() {
        List<ReservationFactory.ItemRequest> items = List.of(
                new ReservationFactory.ItemRequest("A100", 5),
                new ReservationFactory.ItemRequest("B200", 3)
        );

        Reservation reservation = factory.create("ORD-1001", items);

        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getOrderId()).isEqualTo("ORD-1001");
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservation.getCreatedAt()).isNotNull();
        assertThat(reservation.getItems()).hasSize(2);
        assertThat(reservation.getItems().get(0).getSku()).isEqualTo("A100");
        assertThat(reservation.getItems().get(0).getQuantity()).isEqualTo(5);
    }
}
