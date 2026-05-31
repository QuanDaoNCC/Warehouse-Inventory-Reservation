package com.warehouse.api.controller;

import com.warehouse.application.dto.InventoryResponse;
import com.warehouse.application.service.ReservationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final ReservationService reservationService;

    public InventoryController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{sku}")
    public InventoryResponse getInventory(@PathVariable String sku) {
        return reservationService.getInventory(sku);
    }
}
