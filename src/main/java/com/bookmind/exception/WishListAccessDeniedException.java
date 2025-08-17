package com.bookmind.exception;

/**
 * Exception thrown when a user tries to access a wishlist they don't own.
 */
public class WishListAccessDeniedException extends RuntimeException {
    
    public WishListAccessDeniedException(String message) {
        super(message);
    }
    
    public WishListAccessDeniedException(Long userId, Long wishListId) {
        super("User with ID " + userId + " does not have access to wishlist with ID " + wishListId);
    }
    
    public WishListAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
