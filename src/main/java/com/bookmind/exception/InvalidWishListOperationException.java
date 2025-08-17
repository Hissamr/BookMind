package com.bookmind.exception;

/**
 * Exception thrown for invalid wishlist operations like empty names, unauthorized access, etc.
 */
public class InvalidWishListOperationException extends RuntimeException {
    
    public InvalidWishListOperationException(String message) {
        super(message);
    }
    
    public InvalidWishListOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
