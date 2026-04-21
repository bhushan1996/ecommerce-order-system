package com.ecommerce.orderservice.strategy.impl;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.strategy.OrderStatusTransitionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Strategy for transitioning to DELIVERED status
 * Allowed from: SHIPPED
 */
@Component
@Slf4j
public class DeliveredStatusStrategy implements OrderStatusTransitionStrategy {

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return newStatus == OrderStatus.DELIVERED && currentStatus == OrderStatus.SHIPPED;
    }

    @Override
    public void executeTransition(Order order, OrderStatus newStatus) {
        log.info("Transitioning order {} from {} to DELIVERED", order.getId(), order.getStatus());
        order.setStatus(OrderStatus.DELIVERED);
        // Additional business logic can be added here
        // e.g., send delivery confirmation, request feedback, etc.
    }

    @Override
    public OrderStatus getTargetStatus() {
        return OrderStatus.DELIVERED;
    }
}

