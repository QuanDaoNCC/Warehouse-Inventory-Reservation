package com.warehouse.api.controller;

import com.warehouse.application.dto.InventoryResponse;
import com.warehouse.application.dto.ReservationItemResponse;
import com.warehouse.application.dto.ReservationResponse;
import com.warehouse.application.service.ReservationService;
import com.warehouse.domain.exception.InventoryNotFoundException;
import com.warehouse.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ReservationController.class, InventoryController.class})
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Test
    void createReservation_returnsCreatedResponseEnvelope() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-01T01:00:00Z");
        ReservationResponse response = new ReservationResponse(
                id,
                "ORD-1001",
                ReservationStatus.PENDING,
                now,
                now,
                List.of(new ReservationItemResponse("A100", 5))
        );

        when(reservationService.createReservation(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "ORD-1001",
                                  "items": [
                                    {"sku": "A100", "quantity": 5}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.orderId").value("ORD-1001"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].sku").value("A100"))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    void createReservation_returnsValidationErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.status").value(400));
    }

    @Test
    void getInventory_returnsSuccessEnvelope() throws Exception {
        when(reservationService.getInventory("A100"))
                .thenReturn(new InventoryResponse("A100", 100, 95, 5));

        mockMvc.perform(get("/api/v1/inventory/A100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("A100"))
                .andExpect(jsonPath("$.data.availableStock").value(95))
                .andExpect(jsonPath("$.error").value(nullValue()));

        verify(reservationService).getInventory("A100");
    }

    @Test
    void getInventory_returnsNotFoundErrorEnvelope() throws Exception {
        when(reservationService.getInventory("UNKNOWN"))
                .thenThrow(new InventoryNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/api/v1/inventory/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("INVENTORY_NOT_FOUND"))
                .andExpect(jsonPath("$.error.status").value(404));
    }
}
