package com.ecommerce.orderservice.strategy.impl;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.strategy.OrderStatusTransitionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Strategy for transitioning to SHIPPED status
 * Allowed from: PROCESSING
 */
@Component
@Slf4j
public class ShippedStatusStrategy implements OrderStatusTransitionStrategy {

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return newStatus == OrderStatus.SHIPPED && currentStatus == OrderStatus.PROCESSING;
    }

    @Override
    public void executeTransition(Order order, OrderStatus newStatus) {
        log.info("Transitioning order {} from {} to SHIPPED", order.getId(), order.getStatus());
        order.setStatus(OrderStatus.SHIPPED);
        // Additional business logic can be added here
        // e.g., generate tracking number, notify customer, etc.
    }

    @Override
    public OrderStatus getTargetStatus() {
        return OrderStatus.SHIPPED;
    }
}

