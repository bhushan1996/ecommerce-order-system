package com.ecommerce.orderservice.strategy.impl;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.strategy.OrderStatusTransitionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Strategy for transitioning to PROCESSING status
 * Allowed from: PENDING
 */
@Component
@Slf4j
public class ProcessingStatusStrategy implements OrderStatusTransitionStrategy {

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return newStatus == OrderStatus.PROCESSING && currentStatus == OrderStatus.PENDING;
    }

    @Override
    public void executeTransition(Order order, OrderStatus newStatus) {
        log.info("Transitioning order {} from {} to PROCESSING", order.getId(), order.getStatus());
        order.setStatus(OrderStatus.PROCESSING);
        // Additional business logic can be added here
        // e.g., notify warehouse, reserve inventory, etc.
    }

    @Override
    public OrderStatus getTargetStatus() {
        return OrderStatus.PROCESSING;
    }
}

