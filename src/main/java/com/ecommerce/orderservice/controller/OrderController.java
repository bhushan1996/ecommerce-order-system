package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.ResponseWrapper;
import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Order operations
 * Implements RESTful API endpoints with proper HTTP status codes
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing e-commerce orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order
     * POST /api/orders
     */
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order with the provided items. Validates payment and returns order details."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Payment service unavailable"
        )
    })
    @PostMapping
    public ResponseEntity<ResponseWrapper<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Received request to create order");
        
        OrderResponse orderResponse = orderService.createOrder(request);
        ResponseWrapper<OrderResponse> response = ResponseWrapper.success(
                orderResponse,
                "Order created successfully",
                201
        );
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves order details by order ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order found",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<OrderResponse>> getOrderById(
            @Parameter(description = "Order ID", required = true) @PathVariable Long id) {
        log.info("Received request to get order with ID: {}", id);
        
        OrderResponse orderResponse = orderService.getOrderById(id);
        ResponseWrapper<OrderResponse> response = ResponseWrapper.success(
                orderResponse,
                "Order retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders with optional status filter
     * GET /api/orders?status=PENDING
     */
    @Operation(
        summary = "Get all orders",
        description = "Retrieves all orders, optionally filtered by status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
        )
    })
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<OrderResponse>>> getAllOrders(
            @Parameter(description = "Filter by order status (optional)")
            @RequestParam(required = false) OrderStatus status) {
        log.info("Received request to get all orders with status filter: {}", status);
        
        List<OrderResponse> orders = orderService.getAllOrders(status);
        ResponseWrapper<List<OrderResponse>> response = ResponseWrapper.success(
                orders,
                "Orders retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update order status
     * PUT /api/orders/{id}/status
     */
    @Operation(
        summary = "Update order status",
        description = "Updates the status of an existing order using strategy pattern for valid transitions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order status updated successfully",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid status transition"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found"
        )
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseWrapper<OrderResponse>> updateOrderStatus(
            @Parameter(description = "Order ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("Received request to update order {} status to {}", id, request.getStatus());
        
        OrderResponse orderResponse = orderService.updateOrderStatus(id, request.getStatus());
        ResponseWrapper<OrderResponse> response = ResponseWrapper.success(
                orderResponse,
                "Order status updated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel order
     * POST /api/orders/{id}/cancel
     */
    @Operation(
        summary = "Cancel order",
        description = "Cancels an order. Only orders in PENDING status can be cancelled."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order cancelled successfully",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Order cannot be cancelled (not in PENDING status)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found"
        )
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ResponseWrapper<OrderResponse>> cancelOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable Long id) {
        log.info("Received request to cancel order {}", id);
        
        OrderResponse orderResponse = orderService.cancelOrder(id);
        ResponseWrapper<OrderResponse> response = ResponseWrapper.success(
                orderResponse,
                "Order cancelled successfully"
        );
        
        return ResponseEntity.ok(response);
    }
}

