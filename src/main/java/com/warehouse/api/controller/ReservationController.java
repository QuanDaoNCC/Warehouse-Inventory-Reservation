package com.warehouse.api.controller;

import com.warehouse.application.dto.CreateReservationRequest;
import com.warehouse.application.dto.ReservationResponse;
import com.warehouse.application.service.ReservationService;
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
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return reservationService.createReservation(request);
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable UUID id) {
        return reservationService.getReservation(id);
    }

    @PostMapping("/{id}/confirm")
    public ReservationResponse confirmReservation(@PathVariable UUID id) {
        return reservationService.confirmReservation(id);
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancelReservation(@PathVariable UUID id) {
        return reservationService.cancelReservation(id);
    }
}
