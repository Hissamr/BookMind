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
     * @param userId the ID of the user whose wishlists to retrieve
     * @return a List of WishListResponse DTOs
     */
    @GetMapping("/wishlists")
    public ResponseEntity<List<WishListResponse>> getAllWishList(@PathVariable Long userId) {
        GetUserWishListsRequest request = new GetUserWishListsRequest(userId);
        List<WishListResponse> response = wishListService.getAllWishListByUserId(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get a single wishlist by ID
     * @param userId the ID of the user who owns the wishlist
     * @param wishListId the ID of the wishlist to retrieve
     * @return the WishListResponse DTO
     */
    @GetMapping("/wishlists/{wishListId}")
    public ResponseEntity<WishListResponse> getWishListById(@PathVariable Long userId, @PathVariable Long wishListId) {
        GetWishListRequest request = new GetWishListRequest(userId, wishListId);
        WishListResponse response = wishListService.getWishListByUserId(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Create a new wishlist for a user
     * @param userId the ID of the user who will own the wishlist
     * @param request the wishlist creation request
     * @return the WishListResponse DTO
     */
    @PostMapping("/wishlists")
    public ResponseEntity<WishListResponse> addWishList(@PathVariable Long userId, @RequestBody CreateWishListRequest request) {
        WishListResponse response = wishListService.addWishListToUser(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update the Wishlist of the user
     * @param userId ID of the User
     * @param wishListId ID of the WishList to be updated
     * @param wishList WishList object to be updated
     * @return WishListResponse
     */
    @PutMapping("/wishlists/{wishListId}")
    public ResponseEntity<WishListResponse> updateWishList(@PathVariable Long userId, @PathVariable Long wishListId, @RequestBody WishList wishList) {
        UpdateWishListRequest request = new UpdateWishListRequest(userId, wishListId, wishList.getName());
        WishListResponse response = wishListService.updateWishListToUser(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Delete a Wishlist of the User
     * @param userId ID of the User
     * @param wishListId ID of the WishList to be deleted
     * @return SuccessResponse indicating the deletion was successful
     */
    @DeleteMapping("/wishlists/{wishListId}")
    public ResponseEntity<SuccessResponse> deleteWishList(@PathVariable Long userId, @PathVariable Long wishListId) {
        DeleteWishListRequest request = new DeleteWishListRequest(userId, wishListId);
        SuccessResponse response = wishListService.deleteWishList(request);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    /**
     * Add a book to a Wishlist
     * @param userId ID of the User
     * @param wishListId ID of the Wishlist to which the book will be added
     * @param bookId ID of the Book to be added
     * @return SuccessResponse indicating the book was added successfully
     */
    @PostMapping("/wishlists/{wishlistId}/books/{bookId}")
    public ResponseEntity<SuccessResponse> addBookToWishList(@PathVariable Long userId, @PathVariable Long wishListId, @PathVariable Long bookId) {
        AddBookToWishListRequest request = new AddBookToWishListRequest(userId, wishListId, bookId);
        SuccessResponse response = wishListService.addBookToWishList(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Remove a book from a Wishlist
     * @param userId ID of the User
     * @param wishListId ID of the Wishlist from which the book will be removed
     * @param bookId ID of the Book to be removed
     * @return SuccessResponse indicating the book was removed successfully
     */
    @DeleteMapping("/wishlists/{wishlistId}/books/{bookId}")
    public ResponseEntity<SuccessResponse> removeBookFromWishList(@PathVariable Long userId, @PathVariable Long wishlistId, @PathVariable Long bookId) {
        RemoveBookFromWishListRequest request = new RemoveBookFromWishListRequest(userId, wishlistId, bookId);
        SuccessResponse response = wishListService.removeBookFromWishList(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
}
