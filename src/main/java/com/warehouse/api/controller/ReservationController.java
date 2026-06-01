package com.warehouse.api.controller;

import com.warehouse.application.dto.CreateReservationRequest;
import com.warehouse.application.dto.ReservationResponse;
import com.warehouse.application.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Create and manage inventory reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a reservation")
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return reservationService.createReservation(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a reservation")
    public ReservationResponse getReservation(@PathVariable UUID id) {
        return reservationService.getReservation(id);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending reservation")
    public ReservationResponse confirmReservation(@PathVariable UUID id) {
        return reservationService.confirmReservation(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending reservation")
    public ReservationResponse cancelReservation(@PathVariable UUID id) {
        return reservationService.cancelReservation(id);
    }
}
