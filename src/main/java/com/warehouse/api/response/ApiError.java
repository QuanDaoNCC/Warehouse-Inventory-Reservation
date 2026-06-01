package com.warehouse.api.response;

import java.time.Instant;

public record ApiError(
        String code,
        int status,
        String message,
        Instant timestamp
) {
}
