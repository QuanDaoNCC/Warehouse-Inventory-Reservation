package com.warehouse.infrastructure.persistence.mapper;

import com.warehouse.domain.model.Inventory;
import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationItem;
import com.warehouse.infrastructure.persistence.entity.InventoryEntity;
import com.warehouse.infrastructure.persistence.entity.ReservationEntity;
import com.warehouse.infrastructure.persistence.entity.ReservationItemEntity;

import java.util.ArrayList;
import java.util.List;

public final class DomainMapper {

    private DomainMapper() {
    }

    public static Inventory toDomain(InventoryEntity entity) {
        Inventory inventory = new Inventory();
        inventory.setSku(entity.getSku());
        inventory.setTotalStock(entity.getTotalStock());
        inventory.setAvailableStock(entity.getAvailableStock());
        inventory.setReservedStock(entity.getReservedStock());
        inventory.setVersion(entity.getVersion());
        return inventory;
    }

    public static InventoryEntity toEntity(Inventory domain) {
        InventoryEntity entity = new InventoryEntity();
        entity.setSku(domain.getSku());
        entity.setTotalStock(domain.getTotalStock());
        entity.setAvailableStock(domain.getAvailableStock());
        entity.setReservedStock(domain.getReservedStock());
        entity.setVersion(domain.getVersion());
        return entity;
    }

    public static Reservation toDomain(ReservationEntity entity) {
        Reservation reservation = new Reservation();
        reservation.setId(entity.getId());
        reservation.setOrderId(entity.getOrderId());
        reservation.setStatus(entity.getStatus());
        reservation.setCreatedAt(entity.getCreatedAt());
        reservation.setUpdatedAt(entity.getUpdatedAt());

        List<ReservationItem> items = new ArrayList<>();
        for (ReservationItemEntity itemEntity : entity.getItems()) {
            ReservationItem item = new ReservationItem();
            item.setId(itemEntity.getId());
            item.setSku(itemEntity.getSku());
            item.setQuantity(itemEntity.getQuantity());
            item.setReservation(reservation);
            items.add(item);
        }
        reservation.setItems(items);
        return reservation;
    }

    public static ReservationEntity toEntity(Reservation domain) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(domain.getId());
        entity.setOrderId(domain.getOrderId());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt() == null ? domain.getCreatedAt() : domain.getUpdatedAt());

        List<ReservationItemEntity> items = new ArrayList<>();
        for (ReservationItem item : domain.getItems()) {
            ReservationItemEntity itemEntity = new ReservationItemEntity();
            itemEntity.setId(item.getId());
            itemEntity.setSku(item.getSku());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setReservation(entity);
            items.add(itemEntity);
        }
        entity.setItems(items);
        return entity;
    }
}
