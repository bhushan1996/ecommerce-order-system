package com.ecommerce.orderservice.exception;

import com.ecommerce.orderservice.entity.OrderStatus;

/**
 * Custom exception thrown when an invalid order state transition is attempted
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(OrderStatus currentStatus, OrderStatus newStatus) {
        super(String.format("Invalid state transition from %s to %s", currentStatus, newStatus));
    }

    public InvalidOrderStateException(String message) {
        super(message);
    }
}

