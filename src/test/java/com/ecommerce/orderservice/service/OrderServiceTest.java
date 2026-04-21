package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderItemRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.exception.InvalidOrderStateException;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.factory.OrderFactory;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.strategy.OrderStatusTransitionStrategy;
import com.ecommerce.orderservice.strategy.impl.CancelledStatusStrategy;
import com.ecommerce.orderservice.strategy.impl.ProcessingStatusStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService
 * Uses JUnit 5 and Mockito for testing
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderFactory orderFactory;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private List<OrderStatusTransitionStrategy> transitionStrategies;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderResponse testOrderResponse;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        testOrderResponse = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(itemRequest))
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        when(orderFactory.createOrder(any(CreateOrderRequest.class))).thenReturn(testOrder);
        when(paymentService.processPayment(any(BigDecimal.class))).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(testOrderResponse);

        // Act
        OrderResponse result = orderService.createOrder(createOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderFactory, times(1)).createOrder(createOrderRequest);
        verify(paymentService, times(1)).processPayment(testOrder.getTotalAmount());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void testCreateOrder_PaymentFailed() {
        // Arrange
        when(orderFactory.createOrder(any(CreateOrderRequest.class))).thenReturn(testOrder);
        when(paymentService.processPayment(any(BigDecimal.class))).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.createOrder(createOrderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toOrderResponse(testOrder)).thenReturn(testOrderResponse);

        // Act
        OrderResponse result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void testGetAllOrders_WithStatusFilter() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(orders);
        when(orderMapper.toOrderResponses(orders)).thenReturn(List.of(testOrderResponse));

        // Act
        List<OrderResponse> result = orderService.getAllOrders(OrderStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
    }

    @Test
    void testGetAllOrders_NoFilter() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.toOrderResponses(orders)).thenReturn(List.of(testOrderResponse));

        // Act
        List<OrderResponse> result = orderService.getAllOrders(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testCancelOrder_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        CancelledStatusStrategy strategy = new CancelledStatusStrategy();
        when(transitionStrategies.stream()).thenReturn(List.<OrderStatusTransitionStrategy>of(strategy).stream());
        
        Order cancelledOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("100.00"))
                .build();
        
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);
        
        OrderResponse cancelledResponse = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .build();
        
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(cancelledResponse);

        // Act
        OrderResponse result = orderService.cancelOrder(1L);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelOrder_InvalidState() {
        // Arrange
        Order processingOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PROCESSING)
                .build();
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(processingOrder));

        // Act & Assert
        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void testProcessPendingOrders() {
        // Arrange
        List<Order> pendingOrders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(pendingOrders);
        
        ProcessingStatusStrategy strategy = new ProcessingStatusStrategy();
        when(transitionStrategies.stream()).thenReturn(List.<OrderStatusTransitionStrategy>of(strategy).stream());
        
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        int result = orderService.processPendingOrders();

        // Assert
        assertEquals(1, result);
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
    }
}

