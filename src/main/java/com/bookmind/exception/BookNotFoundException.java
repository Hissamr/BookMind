package com.bookmind.exception;

/**
 * Exception thrown when a requested book is not found in the database.
 */
public class BookNotFoundException extends RuntimeException {
    
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(Long id) {
        super("Book not found with ID: " + id);
    }
    
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
