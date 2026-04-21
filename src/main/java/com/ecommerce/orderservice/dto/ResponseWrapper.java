package com.ecommerce.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper
 * Provides consistent response structure across all endpoints
 * 
 * Structure:
 * {
 *   "timestamp": "2024-01-01T10:00:00",
 *   "status": 200,
 *   "message": "Success",
 *   "data": {...},
 *   "error": null
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private Integer status;
    private String message;
    private T data;
    private String error;

    /**
     * Create success response with data
     */
    public static <T> ResponseWrapper<T> success(T data, String message) {
        return ResponseWrapper.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Create success response with data and custom status
     */
    public static <T> ResponseWrapper<T> success(T data, String message, Integer status) {
        return ResponseWrapper.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Create success response without data
     */
    public static <T> ResponseWrapper<T> success(String message) {
        return ResponseWrapper.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message(message)
                .build();
    }

    /**
     * Create error response
     */
    public static <T> ResponseWrapper<T> error(Integer status, String message, String error) {
        return ResponseWrapper.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(message)
                .error(error)
                .build();
    }
}

