package com.ecommerce.orderservice.entity;

/**
 * Enum representing the lifecycle states of an order
 * 
 * Allowed transitions:
 * PENDING → PROCESSING → SHIPPED → DELIVERED
 * PENDING → CANCELLED (only from PENDING state)
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

