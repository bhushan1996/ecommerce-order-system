package com.ecommerce.orderservice.strategy;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;

/**
 * Strategy interface for order status transitions
 * Implements Strategy Pattern for handling different status transitions
 */
public interface OrderStatusTransitionStrategy {

    /**
     * Check if transition is allowed
     */
    boolean canTransition(OrderStatus currentStatus, OrderStatus newStatus);

    /**
     * Execute the transition
     */
    void executeTransition(Order order, OrderStatus newStatus);

    /**
     * Get the target status this strategy handles
     */
    OrderStatus getTargetStatus();
}

