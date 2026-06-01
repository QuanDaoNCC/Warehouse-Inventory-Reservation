package com.warehouse.api.exception;

import com.warehouse.api.response.ApiResponse;
import com.warehouse.domain.exception.DuplicateOrderReservationException;
import com.warehouse.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleInsufficientStock_returnsConflictEnvelopeWithErrorCode() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleInsufficientStock(
                new InsufficientStockException("A100", 5, 3)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error().code()).isEqualTo("INSUFFICIENT_STOCK");
        assertThat(response.getBody().error().status()).isEqualTo(409);
    }

    @Test
    void handleDuplicateOrder_returnsConflictEnvelopeWithErrorCode() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateOrder(
                new DuplicateOrderReservationException("ORD-1001")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("DUPLICATE_ORDER_RESERVATION");
        assertThat(response.getBody().error().message()).contains("ORD-1001");
    }
}
