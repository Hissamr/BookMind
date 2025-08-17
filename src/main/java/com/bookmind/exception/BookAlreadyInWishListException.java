package com.bookmind.exception;

public class BookAlreadyInWishListException extends RuntimeException {
    public BookAlreadyInWishListException(String message) {
        super(message);
    }
    
    public BookAlreadyInWishListException(Long bookId, Long wishListId) {
        super("Book with ID " + bookId + " is already in wishlist with ID " + wishListId);
    }
}
