package com.warehouse.api.controller;

import com.warehouse.application.dto.InventoryResponse;
import com.warehouse.application.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Read warehouse stock levels")
public class InventoryController {

    private final ReservationService reservationService;

    public InventoryController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{sku}")
    @Operation(summary = "Get stock for a SKU")
    public InventoryResponse getInventory(@PathVariable String sku) {
        return reservationService.getInventory(sku);
    }
}
