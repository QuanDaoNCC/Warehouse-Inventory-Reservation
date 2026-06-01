package com.warehouse.domain.model;

import com.warehouse.domain.state.ReservationState;
import com.warehouse.domain.state.ReservationStateRegistry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Reservation {

    private UUID id;
    private String orderId;
    private ReservationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ReservationItem> items = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ReservationItem> getItems() {
        return items;
    }

    public void setItems(List<ReservationItem> items) {
        this.items = items;
    }

    public void addItem(ReservationItem item) {
        items.add(item);
        item.setReservation(this);
    }

    public void confirm() {
        currentState().confirm(this);
    }

    public void cancel() {
        currentState().cancel(this);
    }

    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }

    public void transitionTo(ReservationStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    private ReservationState currentState() {
        return ReservationStateRegistry.forStatus(status);
    }
}
