package com.bookmind.exception;

public class CartAlreadyCheckedOutException extends RuntimeException {

    public CartAlreadyCheckedOutException(Long cartId) {
        super("Cart with ID " + cartId + " has already been checked out");
    }

    public CartAlreadyCheckedOutException(String message) {
        super(message);
    }
}
