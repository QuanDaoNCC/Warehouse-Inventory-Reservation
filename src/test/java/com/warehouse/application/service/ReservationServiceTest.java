package com.warehouse.application.service;

import com.warehouse.application.dto.CreateReservationRequest;
import com.warehouse.application.dto.ReservationItemRequest;
import com.warehouse.application.port.InventoryRepositoryPort;
import com.warehouse.application.port.ReservationRepositoryPort;
import com.warehouse.domain.exception.InsufficientStockException;
import com.warehouse.domain.exception.InvalidStateTransitionException;
import com.warehouse.domain.exception.InventoryNotFoundException;
import com.warehouse.domain.exception.ReservationNotFoundException;
import com.warehouse.domain.factory.ReservationFactory;
import com.warehouse.domain.model.Inventory;
import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationItem;
import com.warehouse.domain.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepositoryPort reservationRepository;

    @Mock
    private InventoryRepositoryPort inventoryRepository;

    @Mock
    private ReservationFactory reservationFactory;

    @InjectMocks
    private ReservationService reservationService;

    private Inventory inventoryA100;

    @BeforeEach
    void setUp() {
        inventoryA100 = new Inventory();
        inventoryA100.setSku("A100");
        inventoryA100.setTotalStock(100);
        inventoryA100.setAvailableStock(100);
        inventoryA100.setReservedStock(0);
    }

    @Test
    void createReservation_succeedsWhenStockAvailable() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1001",
                List.of(new ReservationItemRequest("A100", 5))
        );

        Reservation reservation = buildPendingReservation("ORD-1001", "A100", 5);

        when(inventoryRepository.findBySkusForUpdate(List.of("A100"))).thenReturn(List.of(inventoryA100));
        when(reservationFactory.create(eq("ORD-1001"), any())).thenReturn(reservation);
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        var response = reservationService.createReservation(request);

        assertThat(response.orderId()).isEqualTo("ORD-1001");
        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
        assertThat(inventoryA100.getAvailableStock()).isEqualTo(95);
        assertThat(inventoryA100.getReservedStock()).isEqualTo(5);
        verify(inventoryRepository).save(inventoryA100);
    }

    @Test
    void createReservation_rejectsWhenInsufficientStock() {
        inventoryA100.setAvailableStock(3);

        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1002",
                List.of(new ReservationItemRequest("A100", 5))
        );

        when(inventoryRepository.findBySkusForUpdate(List.of("A100"))).thenReturn(List.of(inventoryA100));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("A100")
                .hasMessageContaining("requested 5")
                .hasMessageContaining("available 3");

        verify(reservationRepository, never()).save(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createReservation_rejectsWhenSkuNotFound() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1003",
                List.of(new ReservationItemRequest("UNKNOWN", 1))
        );

        when(inventoryRepository.findBySkusForUpdate(List.of("UNKNOWN"))).thenReturn(List.of());

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void confirmReservation_transitionsPendingToConfirmed() {
        UUID id = UUID.randomUUID();
        Reservation reservation = buildPendingReservation("ORD-2001", "A100", 10);
        reservation.setId(id);

        when(reservationRepository.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        var response = reservationService.confirmReservation(id);

        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void confirmReservation_rejectsWhenAlreadyConfirmed() {
        UUID id = UUID.randomUUID();
        Reservation reservation = buildPendingReservation("ORD-2002", "A100", 10);
        reservation.setId(id);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirmReservation(id))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("confirm")
                .hasMessageContaining("CONFIRMED");
    }

    @Test
    void confirmReservation_rejectsWhenCancelled() {
        UUID id = UUID.randomUUID();
        Reservation reservation = buildPendingReservation("ORD-2003", "A100", 10);
        reservation.setId(id);
        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirmReservation(id))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancelReservation_transitionsPendingToCancelledAndRestoresStock() {
        UUID id = UUID.randomUUID();
        Reservation reservation = buildPendingReservation("ORD-3001", "A100", 10);
        reservation.setId(id);

        inventoryA100.setAvailableStock(90);
        inventoryA100.setReservedStock(10);

        when(reservationRepository.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findBySkusForUpdate(List.of("A100"))).thenReturn(List.of(inventoryA100));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        var response = reservationService.cancelReservation(id);

        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(inventoryA100.getAvailableStock()).isEqualTo(100);
        assertThat(inventoryA100.getReservedStock()).isEqualTo(0);
        verify(inventoryRepository).save(inventoryA100);
    }

    @Test
    void cancelReservation_rejectsWhenConfirmed() {
        UUID id = UUID.randomUUID();
        Reservation reservation = buildPendingReservation("ORD-3002", "A100", 10);
        reservation.setId(id);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(id))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("cancel")
                .hasMessageContaining("CONFIRMED");

        verify(inventoryRepository, never()).findBySkusForUpdate(any());
    }

    @Test
    void getReservation_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservation(id))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void createReservation_aggregatesDuplicateSkusInRequest() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1004",
                List.of(
                        new ReservationItemRequest("A100", 3),
                        new ReservationItemRequest("A100", 2)
                )
        );

        Reservation reservation = buildPendingReservation("ORD-1004", "A100", 5);

        when(inventoryRepository.findBySkusForUpdate(List.of("A100"))).thenReturn(List.of(inventoryA100));

        ArgumentCaptor<List<ReservationFactory.ItemRequest>> captor = ArgumentCaptor.forClass(List.class);
        when(reservationFactory.create(eq("ORD-1004"), captor.capture())).thenReturn(reservation);
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        reservationService.createReservation(request);

        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).quantity()).isEqualTo(5);
    }

    private Reservation buildPendingReservation(String orderId, String sku, int quantity) {
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setOrderId(orderId);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(Instant.now());

        ReservationItem item = new ReservationItem();
        item.setSku(sku);
        item.setQuantity(quantity);
        reservation.addItem(item);

        return reservation;
    }
}
