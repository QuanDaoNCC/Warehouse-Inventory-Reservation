package com.warehouse.domain.exception;

public class InventoryNotFoundException extends DomainException {

    public InventoryNotFoundException(String sku) {
        super("Inventory not found for SKU: " + sku);
    }
}
