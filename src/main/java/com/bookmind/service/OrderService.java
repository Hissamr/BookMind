package com.bookmind.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bookmind.dto.OrderItemDto;
import com.bookmind.dto.OrderResponse;
import com.bookmind.exception.OrderNotFoundException;
import com.bookmind.model.Cart;
import com.bookmind.model.CartItem;
import com.bookmind.model.Order;
import com.bookmind.model.OrderItem;
import com.bookmind.model.OrderStatus;
import com.bookmind.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Create an order from a cart
     * 
     * @param cart The cart to convert to an order
     * @param shippingAddress The shipping address for the order
     * @return Order entity
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public Order createOrderFromCart(Cart cart, String shippingAddress) {
        log.info("Creating order from cart ID: {} for user ID: {}", cart.getId(), cart.getUser().getId());

        // 1. Create new Order
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setShippingAddress(shippingAddress);

        // 2. Convert CartItems to OrderItems and add to order
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = convertToOrderItem(cartItem);
            order.addOrderItem(orderItem);
        }

        // 3. Save and return
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {} for user ID: {}",
                savedOrder.getId(), savedOrder.getUser().getId());

        return savedOrder;
    }

    /**
     * Convert a CartItem to an OrderItem
     */
    private OrderItem convertToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setBook(cartItem.getBook());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getPrice());
        return orderItem;
    }

    /**
     * Fetch an order by its ID for a specific user
     * 
     * @param userId The authenticated user's ID
     * @param orderId The order ID to fetch
     * @return OrderResponse with the order details
     * @throws OrderNotFoundException if the order is not found
     */
    public OrderResponse getOrderById(Long userId, Long orderId) {
        log.info("Fetching order with ID: {} for user ID: {}", orderId, userId);

        Order order = orderRepository.findByUserIdAndId(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with ID: " + orderId + " for user ID: " + userId));

        log.info("Order found with ID: {} for user ID: {}", order.getId(), order.getUser().getId());

        return toOrderResponse(order);
    }

    /**
     * Fetch all orders for a specific user, optionally filtered by status
     * 
     * @param userId The authenticated user's ID
     * @param status Optional status filter (can be null)
     * @return List of OrderResponse with the user's orders
     */
    public List<OrderResponse> getOrdersByUserId(Long userId, String status) {
        log.info("Fetching orders for user ID: {} with status filter: {}", userId, status);

        List<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByUserIdAndStatus(userId, status);
        } else {
            orders = orderRepository.findByUserId(userId);
        }

        log.info("Found {} orders for user ID: {}", orders.size(), userId);

        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel an order (user can only cancel PENDING orders)
     * 
     * @param userId The authenticated user's ID
     * @param orderId The order ID to cancel
     * @return OrderResponse with the cancelled order
     * @throws OrderNotFoundException if order not found or doesn't belong to user
     * @throws IllegalStateException if order is not in PENDING status
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        log.info("Cancelling order ID: {} for user ID: {}", orderId, userId);

        // 1. Find the order (must belong to user)
        Order order = orderRepository.findByUserIdAndId(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with ID: " + orderId + " for user ID: " + userId));

        // 2. Only PENDING orders can be cancelled
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot cancel order ID: {} - status is {}", orderId, order.getStatus());
            throw new IllegalStateException(
                    "Cannot cancel order. Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }

        // 3. Update status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        log.info("Order ID: {} cancelled successfully for user ID: {}", orderId, userId);

        return toOrderResponse(savedOrder);
    }

    /**
     * Get all orders (Admin only), optionally filtered by status
     * 
     * @param status Optional status filter (can be null)
     * @return List of all OrderResponse
     */
    public List<OrderResponse> getAllOrders(String status) {
        log.info("Admin fetching all orders with status filter: {}", status);

        List<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }

        log.info("Found {} total orders", orders.size());

        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update the status of an order (Admin operation)
     * 
     * @param orderId The ID of the order to update
     * @param newStatus The new status to set
     * @return OrderResponse with the updated order details
     * @throws OrderNotFoundException if order not found
     * @throws IllegalArgumentException if status is invalid
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public OrderResponse updateOrderStatus(Long orderId, String newStatus) {
        log.info("Updating status of order ID: {} to {}", orderId, newStatus);

        // 1. Find the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        // 2. Validate and convert status string to enum
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid order status: {}", newStatus);
            throw new IllegalArgumentException("Invalid order status: " + newStatus +
                    ". Valid values are: " + java.util.Arrays.toString(OrderStatus.values()));
        }

        // 3. Update status and save
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order ID: {} status updated to {}", orderId, newStatus);

        return toOrderResponse(updatedOrder);
    }

    // ==================== MAPPER METHODS ====================

    /**
     * Convert Order entity to OrderResponse DTO
     */
    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::toOrderItemDto)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .orderDate(order.getOrderDate())
                .updatedAt(order.getUpdatedAt())
                .items(itemDtos)
                .build();
    }

    /**
     * Convert OrderItem entity to OrderItemDto
     */
    private OrderItemDto toOrderItemDto(OrderItem orderItem) {
        return OrderItemDto.builder()
                .bookId(orderItem.getBook().getId())
                .title(orderItem.getBook().getTitle())
                .author(orderItem.getBook().getAuthor())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .totalPrice(orderItem.getPrice() * orderItem.getQuantity())
                .build();
    }
}
