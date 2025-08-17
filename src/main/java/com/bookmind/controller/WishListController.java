package com.bookmind.controller;

import com.bookmind.model.WishList;
import com.bookmind.service.WishListService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WishListController {

    private final WishListService wishListService;

    @GetMapping("/wishlists")
    public ResponseEntity<List<WishList>> getAllWishList() {
        return new ResponseEntity<>(wishListService.getAllWishList(), HttpStatus.OK);
    }

    @GetMapping("/wishlists/{id}")
    public ResponseEntity<WishList> getWishListById(@PathVariable Long id) {
        try {
            WishList wishList = wishListService.getWishListById(id);
            return new ResponseEntity<>(wishList, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/wishlists")
    public ResponseEntity<?> addWishList(@RequestBody WishList wishList) {
        try {
            WishList savedWishList = wishListService.addWishList(wishList);
            return new ResponseEntity<>(savedWishList, HttpStatus.CREATED);
        }
        catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding WishList: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Error adding WishList: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/wishlists/{id}")
    public ResponseEntity<?> updateWishList(@PathVariable Long id, @RequestBody WishList wishList) {
        try {
            WishList updatedWishList = wishListService.updateWishList(id, wishList);
            return new ResponseEntity<>(updatedWishList, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error updating wishlist: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/wishlists/{id}")
    public ResponseEntity<?> deleteWishList(@PathVariable Long id) {
        try {
            wishListService.deleteWishList(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error deleting wishlist: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/wishlists/{wishlistId}/books/{bookId}")
    public ResponseEntity<?> addBookToWishList(@PathVariable Long wishlistId, @PathVariable Long bookId) {
        try {
            wishListService.addBookToWishList(bookId, wishlistId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding book to wishlist: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/wishlists/{wishlistId}/books/{bookId}")
    public ResponseEntity<?> removeBookFromWishList(@PathVariable Long wishlistId, @PathVariable Long bookId) {
        try {
            wishListService.removeBookFromWishList(bookId, wishlistId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding book to wishlist: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
}
