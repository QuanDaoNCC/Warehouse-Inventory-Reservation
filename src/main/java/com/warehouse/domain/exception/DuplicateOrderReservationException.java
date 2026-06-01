package com.warehouse.domain.exception;

public class DuplicateOrderReservationException extends DomainException {

    public DuplicateOrderReservationException(String orderId) {
        super("Reservation already exists for orderId %s".formatted(orderId));
    }
}
