package com.warehouse.api.response;

public record ApiResponse<T>(
        T data,
        ApiError error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static ApiResponse<Void> failure(ApiError error) {
        return new ApiResponse<>(null, error);
    }
}
