package com.warehouse.domain.factory;

import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(reservation.getUpdatedAt()).isEqualTo(reservation.getCreatedAt());
        assertThat(reservation.getItems()).hasSize(2);
        assertThat(reservation.getItems().get(0).getSku()).isEqualTo("A100");
        assertThat(reservation.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void create_rejectsBlankOrderId() {
        List<ReservationFactory.ItemRequest> items = List.of(new ReservationFactory.ItemRequest("A100", 5));

        assertThatThrownBy(() -> factory.create(" ", items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void create_rejectsEmptyItems() {
        assertThatThrownBy(() -> factory.create("ORD-1001", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itemRequests");
    }

    @Test
    void create_rejectsInvalidItemQuantity() {
        List<ReservationFactory.ItemRequest> items = List.of(new ReservationFactory.ItemRequest("A100", 0));

        assertThatThrownBy(() -> factory.create("ORD-1001", items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");
    }
}
