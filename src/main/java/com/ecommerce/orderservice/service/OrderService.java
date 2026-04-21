package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.exception.InvalidOrderStateException;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.factory.OrderFactory;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.strategy.OrderStatusTransitionStrategy;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service layer for Order operations
 * Implements business logic with Resilience4j patterns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderFactory orderFactory;
    private final OrderMapper orderMapper;
    private final List<OrderStatusTransitionStrategy> transitionStrategies;
    private final PaymentService paymentService;

    /**
     * Create a new order
     * Uses Factory pattern for order creation
     * Applies Circuit Breaker for payment service call
     */
    @Transactional
    @CircuitBreaker(name = "paymentService", fallbackMethod = "createOrderFallback")
    @Bulkhead(name = "orderProcessing")
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order with {} items", request.getItems().size());

        // Use factory to create order
        Order order = orderFactory.createOrder(request);

        // Simulate payment processing (with circuit breaker)
        boolean paymentSuccess = paymentService.processPayment(order.getTotalAmount());
        
        if (!paymentSuccess) {
            throw new RuntimeException("Payment processing failed");
        }

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Fallback method for createOrder when circuit breaker is open
     */
    private OrderResponse createOrderFallback(CreateOrderRequest request, Exception ex) {
        log.error("Circuit breaker activated for createOrder. Reason: {}", ex.getMessage());
        throw new RuntimeException("Payment service is currently unavailable. Please try again later.");
    }

    /**
     * Get order by ID
     */
    @RateLimiter(name = "orderApi")
    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        return orderMapper.toOrderResponse(order);
    }

    /**
     * Get all orders or filter by status
     */
    @RateLimiter(name = "orderApi")
    public List<OrderResponse> getAllOrders(OrderStatus status) {
        log.info("Fetching orders with status filter: {}", status);
        
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }
        
        return orderMapper.toOrderResponses(orders);
    }

    /**
     * Update order status
     * Uses Strategy pattern for status transitions
     */
    @Transactional
    @Bulkhead(name = "orderProcessing")
    @Retry(name = "orderService")
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} to status {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Find appropriate strategy
        OrderStatusTransitionStrategy strategy = findStrategy(newStatus);
        
        // Validate transition
        if (!strategy.canTransition(order.getStatus(), newStatus)) {
            throw new InvalidOrderStateException(order.getStatus(), newStatus);
        }

        // Execute transition
        strategy.executeTransition(order, newStatus);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, newStatus);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    /**
     * Cancel order
     * Only allowed if status is PENDING
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Cancelling order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order. Only PENDING orders can be cancelled. Current status: " + order.getStatus()
            );
        }

        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    /**
     * Process pending orders (called by scheduler)
     * Updates all PENDING orders to PROCESSING
     */
    @Transactional
    public int processPendingOrders() {
        log.info("Processing pending orders");

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        log.info("Found {} pending orders", pendingOrders.size());

        int processedCount = 0;
        for (Order order : pendingOrders) {
            try {
                OrderStatusTransitionStrategy strategy = findStrategy(OrderStatus.PROCESSING);
                if (strategy.canTransition(order.getStatus(), OrderStatus.PROCESSING)) {
                    strategy.executeTransition(order, OrderStatus.PROCESSING);
                    orderRepository.save(order);
                    processedCount++;
                    log.info("Order {} transitioned to PROCESSING", order.getId());
                }
            } catch (Exception e) {
                log.error("Failed to process order {}: {}", order.getId(), e.getMessage());
            }
        }

        log.info("Processed {} orders from PENDING to PROCESSING", processedCount);
        return processedCount;
    }

    /**
     * Find strategy for target status
     */
    private OrderStatusTransitionStrategy findStrategy(OrderStatus targetStatus) {
        Map<OrderStatus, OrderStatusTransitionStrategy> strategyMap = transitionStrategies.stream()
                .collect(Collectors.toMap(
                        OrderStatusTransitionStrategy::getTargetStatus,
                        Function.identity()
                ));

        OrderStatusTransitionStrategy strategy = strategyMap.get(targetStatus);
        if (strategy == null) {
            throw new IllegalStateException("No strategy found for status: " + targetStatus);
        }
        return strategy;
    }
}

