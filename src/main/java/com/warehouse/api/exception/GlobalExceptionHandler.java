package com.warehouse.api.exception;

import com.warehouse.api.response.ApiError;
import com.warehouse.api.response.ApiResponse;
import com.warehouse.domain.exception.DuplicateOrderReservationException;
import com.warehouse.domain.exception.InsufficientStockException;
import com.warehouse.domain.exception.InvalidStateTransitionException;
import com.warehouse.domain.exception.InventoryNotFoundException;
import com.warehouse.domain.exception.ReservationNotFoundException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        return buildResponse(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", ex.getMessage());
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTransition(InvalidStateTransitionException ex) {
        return buildResponse(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION", ex.getMessage());
    }

    @ExceptionHandler(DuplicateOrderReservationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateOrder(DuplicateOrderReservationException ex) {
        return buildResponse(HttpStatus.CONFLICT, "DUPLICATE_ORDER_RESERVATION", ex.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleReservationNotFound(ReservationNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInventoryNotFound(InventoryNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "INVENTORY_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = mostSpecificMessage(ex);
        if (message.toLowerCase(Locale.ROOT).contains("uq_reservations_order_id")) {
            return buildResponse(
                    HttpStatus.CONFLICT,
                    "DUPLICATE_ORDER_RESERVATION",
                    "Reservation already exists for this orderId"
            );
        }
        return buildResponse(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "Request violates a data constraint");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(HttpStatus status, String code, String message) {
        ApiError error = new ApiError(code, status.value(), message, Instant.now());
        ApiResponse<Void> body = ApiResponse.failure(error);
        return ResponseEntity.status(status).body(body);
    }

    private String mostSpecificMessage(DataIntegrityViolationException ex) {
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);
        String message = cause == null ? ex.getMessage() : cause.getMessage();
        return message == null ? "" : message;
    }
}
