package com.bookmind.exception;

/**
 * Exception thrown when a requested user is not found in the database.
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long id) {
        super("User not found with ID: " + id);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
