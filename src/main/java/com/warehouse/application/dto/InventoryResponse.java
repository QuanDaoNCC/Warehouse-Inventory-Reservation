package com.warehouse.application.dto;

import com.warehouse.domain.model.Inventory;

public record InventoryResponse(
        String sku,
        int totalStock,
        int availableStock,
        int reservedStock
) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getSku(),
                inventory.getTotalStock(),
                inventory.getAvailableStock(),
                inventory.getReservedStock()
        );
    }
}
