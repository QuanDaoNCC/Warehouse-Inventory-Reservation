package com.warehouse.infrastructure.persistence.mapper;

import com.warehouse.domain.model.Inventory;
import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationItem;
import com.warehouse.domain.model.ReservationStatus;
import com.warehouse.infrastructure.persistence.entity.InventoryEntity;
import com.warehouse.infrastructure.persistence.entity.ReservationEntity;
import com.warehouse.infrastructure.persistence.entity.ReservationItemEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainMapperTest {

    @Test
    void toDomain_mapsInventoryFields() {
        InventoryEntity entity = new InventoryEntity();
        entity.setSku("A100");
        entity.setTotalStock(100);
        entity.setAvailableStock(90);
        entity.setReservedStock(10);
        entity.setVersion(3);

        Inventory domain = DomainMapper.toDomain(entity);

        assertThat(domain.getSku()).isEqualTo("A100");
        assertThat(domain.getTotalStock()).isEqualTo(100);
        assertThat(domain.getAvailableStock()).isEqualTo(90);
        assertThat(domain.getReservedStock()).isEqualTo(10);
        assertThat(domain.getVersion()).isEqualTo(3);
    }

    @Test
    void toDomain_mapsReservationWithItemsAndAuditFields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-06-01T01:00:00Z");
        Instant updatedAt = Instant.parse("2026-06-01T02:00:00Z");

        ReservationEntity entity = new ReservationEntity();
        entity.setId(id);
        entity.setOrderId("ORD-1001");
        entity.setStatus(ReservationStatus.PENDING);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        ReservationItemEntity itemEntity = new ReservationItemEntity();
        itemEntity.setId(10L);
        itemEntity.setSku("A100");
        itemEntity.setQuantity(5);
        itemEntity.setReservation(entity);
        entity.setItems(List.of(itemEntity));

        Reservation domain = DomainMapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getOrderId()).isEqualTo("ORD-1001");
        assertThat(domain.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(domain.getItems()).hasSize(1);
        assertThat(domain.getItems().get(0).getSku()).isEqualTo("A100");
        assertThat(domain.getItems().get(0).getReservation()).isSameAs(domain);
    }

    @Test
    void toEntity_mapsReservationAndSetsItemBackReference() {
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setOrderId("ORD-1001");
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(Instant.parse("2026-06-01T01:00:00Z"));
        reservation.setUpdatedAt(Instant.parse("2026-06-01T02:00:00Z"));

        ReservationItem item = new ReservationItem();
        item.setId(10L);
        item.setSku("A100");
        item.setQuantity(5);
        reservation.addItem(item);

        ReservationEntity entity = DomainMapper.toEntity(reservation);

        assertThat(entity.getId()).isEqualTo(reservation.getId());
        assertThat(entity.getUpdatedAt()).isEqualTo(reservation.getUpdatedAt());
        assertThat(entity.getItems()).hasSize(1);
        assertThat(entity.getItems().get(0).getReservation()).isSameAs(entity);
    }
}
