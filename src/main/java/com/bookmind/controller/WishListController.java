package com.bookmind.controller;

import com.bookmind.model.WishList;
import com.bookmind.service.WishListService;
import com.bookmind.dto.WishListResponse;
import com.bookmind.dto.SuccessResponse;
import com.bookmind.dto.GetUserWishListsRequest;
import com.bookmind.dto.GetWishListRequest;
import com.bookmind.dto.CreateWishListRequest;
import com.bookmind.dto.UpdateWishListRequest;
import com.bookmind.dto.DeleteWishListRequest;
import com.bookmind.dto.AddBookToWishListRequest;
import com.bookmind.dto.RemoveBookFromWishListRequest;


import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/")
public class WishListController {

    private final WishListService wishListService;
    
    /**
     * Get all wishlists for a specific user
     * 
     * @param userId the ID of the user whose wishlists to retrieve
     * @return a list of wishlist DTOs
     */
    @GetMapping("/wishlists")
    public ResponseEntity<List<WishListResponse>> getAllWishList(@PathVariable Long userId) {
        GetUserWishListsRequest request = new GetUserWishListsRequest(userId);
        List<WishListResponse> response = wishListService.getAllWishListByUserId(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get a single wishlist by ID
     * 
     * @param userId the ID of the user who owns the wishlist
     * @param wishListId the ID of the wishlist to retrieve
     * @return the wishlist DTO
     */
    @GetMapping("/wishlists/{wishListId}")
    public ResponseEntity<WishListResponse> getWishListById(@PathVariable Long userId, @PathVariable Long wishListId) {
        try {
            GetWishListRequest request = new GetWishListRequest(userId, wishListId);
            WishListResponse response = wishListService.getWishListByUserId(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Create a new wishlist for a user
     * 
     * @param userId the ID of the user who will own the wishlist
     * @param request the wishlist creation request
     * @return the created wishlist DTO
     */
    @PostMapping("/wishlists")
    public ResponseEntity<?> addWishList(@PathVariable Long userId, @RequestBody CreateWishListRequest request) {
        try {
            // Convert entity to response DTO
            WishListResponse response = wishListService.addWishListToUser(userId, request);
            // Return the created wishlist response
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding WishList: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Error adding WishList: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/wishlists/{wishListId}")
    public ResponseEntity<?> updateWishList(@PathVariable Long userId, @PathVariable Long wishListId, @RequestBody WishList wishList) {
        try {
            UpdateWishListRequest request = new UpdateWishListRequest(userId, wishListId, wishList.getName());
            WishListResponse response = wishListService.updateWishListToUser(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error updating wishlist: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/wishlists/{wishListId}")
    public ResponseEntity<?> deleteWishList(@PathVariable Long userId, @PathVariable Long wishListId) {
        try {
            DeleteWishListRequest request = new DeleteWishListRequest(userId, wishListId);
            SuccessResponse response = wishListService.deleteWishList(request);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error deleting wishlist: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/wishlists/{wishlistId}/books/{bookId}")
    public ResponseEntity<?> addBookToWishList(@PathVariable Long userId, @PathVariable Long wishlistId, @PathVariable Long bookId) {
        try {
            AddBookToWishListRequest request = new AddBookToWishListRequest(userId, wishlistId, bookId);
            SuccessResponse response = wishListService.addBookToWishList(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding book to wishlist: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/wishlists/{wishlistId}/books/{bookId}")
    public ResponseEntity<?> removeBookFromWishList(@PathVariable Long userId, @PathVariable Long wishlistId, @PathVariable Long bookId) {
        try {
            RemoveBookFromWishListRequest request = new RemoveBookFromWishListRequest(userId, wishlistId, bookId);
            SuccessResponse response = wishListService.removeBookFromWishList(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error removing book from wishlist: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
}
