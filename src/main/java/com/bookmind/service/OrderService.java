package com.bookmind.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bookmind.model.OrderItem;
import com.bookmind.model.CartItem;
import com.bookmind.model.Cart;
import com.bookmind.model.Order;
import com.bookmind.repository.OrderRepository;
import com.bookmind.dto.OrderResponse;
import com.bookmind.dto.OrderItemDto;

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
