package com.warehouse.domain.exception;

import com.warehouse.domain.model.ReservationStatus;

public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(ReservationStatus current, String action) {
        super("Cannot %s reservation in %s state".formatted(action, current));
    }
}
