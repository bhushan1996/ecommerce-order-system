package com.ecommerce.orderservice.strategy.impl;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.strategy.OrderStatusTransitionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Strategy for transitioning to CANCELLED status
 * Allowed from: PENDING only
 */
@Component
@Slf4j
public class CancelledStatusStrategy implements OrderStatusTransitionStrategy {

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return newStatus == OrderStatus.CANCELLED && currentStatus == OrderStatus.PENDING;
    }

    @Override
    public void executeTransition(Order order, OrderStatus newStatus) {
        log.info("Transitioning order {} from {} to CANCELLED", order.getId(), order.getStatus());
        order.setStatus(OrderStatus.CANCELLED);
        // Additional business logic can be added here
        // e.g., release reserved inventory, refund payment, notify customer, etc.
    }

    @Override
    public OrderStatus getTargetStatus() {
        return OrderStatus.CANCELLED;
    }
}

