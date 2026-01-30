package com.bookmind.exception;

/**
 * Exception thrown when a user's cart is not found in the database.
 */
public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(Long userId) {
        super("Cart not found for user ID: " + userId);
    }

    public CartNotFoundException(String message) {
        super(message);
    }
}
