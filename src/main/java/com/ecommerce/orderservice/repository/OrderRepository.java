package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Order entity
 * Extends JpaRepository for CRUD operations
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find all orders with status in given list
     */
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}

