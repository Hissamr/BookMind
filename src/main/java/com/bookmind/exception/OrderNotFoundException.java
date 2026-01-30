package com.bookmind.exception;

/**
 * Exception thrown when an order is not found for a given user.
 */

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId, Long userId) {
        super("Order with ID " + orderId + " not found for user ID: " + userId);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
    
}
