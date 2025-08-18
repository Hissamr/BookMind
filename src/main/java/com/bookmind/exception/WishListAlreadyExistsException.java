package com.bookmind.exception;

public class WishListAlreadyExistsException extends RuntimeException{
    
    public WishListAlreadyExistsException(String wishListName) {
        super("Wishlist with name '" + wishListName + "' already exists.");
    }
}
