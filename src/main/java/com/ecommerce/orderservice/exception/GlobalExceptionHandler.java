package com.ecommerce.orderservice.exception;

import com.ecommerce.orderservice.dto.ResponseWrapper;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler using @ControllerAdvice
 * Handles all exceptions and returns consistent API responses
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle OrderNotFoundException
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleOrderNotFoundException(OrderNotFoundException ex) {
        log.error("Order not found: {}", ex.getMessage());
        ResponseWrapper<Void> response = ResponseWrapper.error(
                HttpStatus.NOT_FOUND.value(),
                "Order Not Found",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle InvalidOrderStateException
     */
    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleInvalidOrderStateException(InvalidOrderStateException ex) {
        log.error("Invalid order state: {}", ex.getMessage());
        ResponseWrapper<Void> response = ResponseWrapper.error(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Order State",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ResponseWrapper<Map<String, String>> response = ResponseWrapper.<Map<String, String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation Failed")
                .data(errors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Circuit Breaker exceptions
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleCallNotPermittedException(CallNotPermittedException ex) {
        log.error("Circuit breaker is open: {}", ex.getMessage());
        ResponseWrapper<Void> response = ResponseWrapper.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Temporarily Unavailable",
                "The service is currently unavailable. Please try again later."
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle Rate Limiter exceptions
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ResponseWrapper<Void>> handleRequestNotPermitted(RequestNotPermitted ex) {
        log.error("Rate limit exceeded: {}", ex.getMessage());
        ResponseWrapper<Void> response = ResponseWrapper.error(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                "Rate limit exceeded. Please try again later."
        );
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Handle Bulkhead exceptions
     */
    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleBulkheadFullException(BulkheadFullException ex) {
        log.error("Bulkhead is full: {}", ex.getMessage());
        ResponseWrapper<Void> response = ResponseWrapper.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Busy",
                "The service is currently busy. Please try again later."
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleGlobalException(Exception ex) {
        log.error("Unexpected error: ", ex);
        ResponseWrapper<Void> response = ResponseWrapper.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later."
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

