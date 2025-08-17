package com.bookmind.exception;

public class WishListNotFoundException extends RuntimeException {
    public WishListNotFoundException(String message) {
        super(message);
    }
    
    public WishListNotFoundException(Long id) {
        super("WishList not found with ID: " + id);
    }
}
