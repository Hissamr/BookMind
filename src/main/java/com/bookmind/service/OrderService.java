package com.bookmind.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bookmind.dto.OrderItemDto;
import com.bookmind.dto.OrderResponse;
import com.bookmind.dto.GetOrderRequest;
import com.bookmind.dto.GetUserOrdersRequest;
import com.bookmind.model.Cart;
import com.bookmind.model.CartItem;
import com.bookmind.model.Order;
import com.bookmind.model.OrderItem;
import com.bookmind.repository.OrderRepository;
import com.bookmind.model.OrderStatus;
import com.bookmind.exception.OrderNotFoundException;

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
     * @return OrderResponse with the created order details
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
     * 
     * @param cartItem The cart item to convert
     * @return OrderItem with the same book, quantity, and price
     */
    private OrderItem convertToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setBook(cartItem.getBook());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getPrice());  // Use cart's snapshot price
        return orderItem;
    }

    /**
     * Fetch an order by its ID for a specific user
     * 
     * @param request The request containing userId and orderId
     * @return OrderResponse with the order details
     * @throws OrderNotFoundException if the order is not found
     */
    public OrderResponse getOrderById(GetOrderRequest request) {
        log.info("Fetching order with ID: {} for user ID: {}", request.getOrderId(), request.getUserId());

            Order order = orderRepository.findByUserIdAndId(request.getUserId(), request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with ID: " + request.getOrderId() + " for user ID: " + request.getUserId()));
        
        log.info("Order found with ID: {} for user ID: {}", order.getId(), order.getUser().getId());

        return toOrderResponse(order);
    }

    /**
     * Fetch all orders for a specific user
     * 
     * @param request The request containing the userId
     * @return List of OrderResponse with the user's orders
     */
    public List<OrderResponse> getOrdersByUserId(GetUserOrdersRequest request) {
        log.info("Fetching orders for user ID: {}", request.getUserId());

        if(request.getStatus() != null) {
            return getOrdersByUserIdAndStatus(request.getUserId(), request.getStatus());
        }

        List<Order> orders = orderRepository.findByUserId(request.getUserId());

        log.info("Found {} orders for user ID: {}", orders.size(), request.getUserId());

        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetch orders for a user filtered by status
     * 
     * @param userId The ID of the user
     * @param status The status to filter orders by
     * @return List of OrderResponse with the filtered orders
     */
    public List<OrderResponse> getOrdersByUserIdAndStatus(Long userId, String status) {
        log.info("Fetching orders for user ID: {} with status: {}", userId, status);

        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, status);

        log.info("Found {} orders for user ID: {} with status: {}", orders.size(), userId, status);

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
     * 
     * @param order The order entity to convert
     * @return OrderResponse DTO
     */
    public OrderResponse toOrderResponse(Order order) {
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
     * 
     * @param orderItem The order item entity to convert
     * @return OrderItemDto DTO
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
