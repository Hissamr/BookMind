package com.bookmind.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookmind.dto.OrderResponse;
import com.bookmind.dto.UpdateOrderStatusRequest;
import com.bookmind.security.AuthenticatedUserProvider;
import com.bookmind.service.OrderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Order operations.
 * 
 * SECURITY: User ID is extracted from JWT token for user-specific operations.
 * Admin endpoints require ROLE_ADMIN authority.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final AuthenticatedUserProvider authProvider;

    /**
     * Get all orders for the authenticated user.
     * Optionally filter by status.
     * 
     * @param status optional status filter (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
     * @return list of OrderResponse DTOs
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestParam(required = false) String status) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Fetching orders for authenticated user {} with status filter: {}", userId, status);

        List<OrderResponse> orders = orderService.getOrdersByUserId(userId, status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get a specific order by ID for the authenticated user.
     * 
     * @param orderId the order ID
     * @return OrderResponse DTO
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable @Positive(message = "Order ID must be positive") Long orderId) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Fetching order {} for authenticated user {}", orderId, userId);

        OrderResponse order = orderService.getOrderById(userId, orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Cancel an order (user can only cancel their own orders).
     * Only allowed if order status is PENDING.
     * 
     * @param orderId the order ID to cancel
     * @return updated OrderResponse DTO
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable @Positive(message = "Order ID must be positive") Long orderId) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Cancelling order {} for authenticated user {}", orderId, userId);

        OrderResponse order = orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(order);
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all orders (Admin only).
     * 
     * @param status optional status filter
     * @return list of all OrderResponse DTOs
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) String status) {
        log.info("Admin fetching all orders with status filter: {}", status);

        List<OrderResponse> orders = orderService.getAllOrders(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status (Admin only).
     * 
     * @param request contains orderId and new status
     * @return updated OrderResponse DTO
     */
    @PutMapping("/admin/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @RequestBody @Valid UpdateOrderStatusRequest request) {
        log.info("Admin updating order {} status to {}", request.getOrderId(), request.getStatus());

        OrderResponse order = orderService.updateOrderStatus(request.getOrderId(), request.getStatus());
        return ResponseEntity.ok(order);
    }
}

