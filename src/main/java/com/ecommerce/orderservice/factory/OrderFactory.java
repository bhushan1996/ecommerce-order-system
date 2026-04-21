package com.ecommerce.orderservice.factory;

import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.entity.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory Pattern implementation for creating Order entities
 * Encapsulates the complex logic of order creation
 */
@Component
@Slf4j
public class OrderFactory {

    /**
     * Create a new Order from CreateOrderRequest
     * Uses Builder pattern internally
     */
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating new order with {} items", request.getItems().size());

        // Create order using Builder pattern
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        // Create and add order items
        List<OrderItem> orderItems = createOrderItems(request, order);
        orderItems.forEach(order::addItem);

        // Calculate total amount
        order.calculateTotalAmount();

        log.info("Order created successfully with total amount: {}", order.getTotalAmount());
        return order;
    }

    /**
     * Create order items from request
     */
    private List<OrderItem> createOrderItems(CreateOrderRequest request, Order order) {
        return request.getItems().stream()
                .map(itemRequest -> OrderItem.builder()
                        .productId(itemRequest.getProductId())
                        .quantity(itemRequest.getQuantity())
                        .price(itemRequest.getPrice())
                        .order(order)
                        .build())
                .toList();
    }
}

