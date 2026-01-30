package com.bookmind.exception;

public class BookNotInCartException extends RuntimeException {

    public BookNotInCartException(Long bookId, Long cartId) {
        super("Book with ID " + bookId + " is not in cart with ID " + cartId);
    }

    public BookNotInCartException(String message) {
        super(message);
    }
}
