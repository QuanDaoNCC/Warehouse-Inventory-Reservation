package com.warehouse.domain.exception;

public class InsufficientStockException extends DomainException {

    public InsufficientStockException(String sku, int requested, int available) {
        super("Insufficient stock for SKU '%s': requested %d, available %d".formatted(sku, requested, available));
    }
}
