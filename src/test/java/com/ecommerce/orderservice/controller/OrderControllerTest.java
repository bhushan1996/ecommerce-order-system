package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderItemRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for OrderController
 * Uses MockMvc for testing REST endpoints
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderResponse testOrderResponse;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
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
    void testCreateOrder_Success() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(testOrderResponse);

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void testCreateOrder_ValidationError() throws Exception {
        // Arrange - empty items list
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .items(List.of())
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetOrderById_Success() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(testOrderResponse);

        // Act & Assert
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void testGetAllOrders_Success() throws Exception {
        // Arrange
        when(orderService.getAllOrders(null)).thenReturn(List.of(testOrderResponse));

        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void testGetAllOrders_WithStatusFilter() throws Exception {
        // Arrange
        when(orderService.getAllOrders(OrderStatus.PENDING)).thenReturn(List.of(testOrderResponse));

        // Act & Assert
        mockMvc.perform(get("/orders")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testUpdateOrderStatus_Success() throws Exception {
        // Arrange
        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .status(OrderStatus.PROCESSING)
                .build();

        OrderResponse updatedResponse = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.PROCESSING)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.PROCESSING)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    void testCancelOrder_Success() throws Exception {
        // Arrange
        OrderResponse cancelledResponse = OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(orderService.cancelOrder(1L)).thenReturn(cancelledResponse);

        // Act & Assert
        mockMvc.perform(post("/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}

