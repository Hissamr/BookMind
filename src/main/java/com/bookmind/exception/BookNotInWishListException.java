package com.bookmind.exception;

/**
 * Exception thrown when attempting to remove a book that doesn't exist in a wishlist.
 */
public class BookNotInWishListException extends RuntimeException {
    
    public BookNotInWishListException(String message) {
        super(message);
    }
    
    public BookNotInWishListException(Long bookId, Long wishListId) {
        super("Book with ID " + bookId + " is not in wishlist with ID " + wishListId);
    }
    
    public BookNotInWishListException(String message, Throwable cause) {
        super(message, cause);
    }
}
