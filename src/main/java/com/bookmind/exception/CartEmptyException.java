package com.bookmind.exception;

public class CartEmptyException extends RuntimeException {

    public CartEmptyException(Long userId) {
        super("Cart is empty for user ID: " + userId);
    }

    public CartEmptyException(String message) {
        super(message);
    }
}
