package com.ecommerce.orderservice.mapper;

import com.ecommerce.orderservice.dto.OrderItemResponse;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Entity and DTO objects
 * Could use MapStruct for more complex mappings
 */
@Component
public class OrderMapper {

    /**
     * Convert Order entity to OrderResponse DTO
     */
    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .items(toOrderItemResponses(order.getItems()))
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of OrderItem entities to OrderItemResponse DTOs
     */
    private List<OrderItemResponse> toOrderItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert OrderItem entity to OrderItemResponse DTO
     */
    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }

    /**
     * Convert list of Order entities to OrderResponse DTOs
     */
    public List<OrderResponse> toOrderResponses(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }
}

