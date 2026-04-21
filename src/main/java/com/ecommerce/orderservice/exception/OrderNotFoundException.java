package com.ecommerce.orderservice.exception;

/**
 * Custom exception thrown when an order is not found
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
