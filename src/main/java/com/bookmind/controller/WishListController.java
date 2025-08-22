package com.bookmind.controller;

import com.bookmind.service.WishListService;
import com.bookmind.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/")
@Validated
public class WishListController {

    private final WishListService wishListService;
    
    /**
     * Get all wishlists for a specific user
     * @param userId the ID of the user whose wishlists to retrieve
     * @return a List of WishListResponse DTOs
     */
    @GetMapping("/wishlists")
    public ResponseEntity<List<WishListResponse>> getAllWishList(@PathVariable @Positive(message = "User ID must be positive") Long userId) {
        log.info("Fetching all wishlists for user {}", userId);
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
    public ResponseEntity<WishListResponse> getWishListById(@PathVariable @Positive(message = "User ID must be positive") Long userId, @PathVariable @Positive(message = "wishlist ID must be positive") Long wishListId) {
        log.info("Fetching wishlist {} for user {}", wishListId, userId);
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
    public ResponseEntity<WishListResponse> addWishList(@PathVariable @Positive(message = "User ID must be positive") Long userId, @RequestBody @Valid CreateWishListRequest request) {
        log.info("Creating new wishlist '{}' for user {}", request.getName(), userId);
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
    public ResponseEntity<WishListResponse> updateWishList(@PathVariable @Positive(message = "User ID must be positive") Long userId, @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId, @RequestBody @Valid CreateWishListRequest updateRequest) {
        log.info("Updating wishlist '{}' for user {}", wishListId, userId);
        UpdateWishListRequest request = new UpdateWishListRequest(userId, wishListId, updateRequest.getName());
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
    public ResponseEntity<SuccessResponse> deleteWishList(@PathVariable @Positive(message = "User ID must be positive") Long userId, @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId) {
        log.info("Deleting wishlist '{}' for user {}", wishListId, userId);
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
    public ResponseEntity<SuccessResponse> addBookToWishList(@PathVariable @Positive(message = "User ID must be positive") Long userId, @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId, @PathVariable @Positive(message = "Book ID must be positive") Long bookId) {
        log.info("Adding book '{}' to wishlist '{}' for user {}", bookId, wishListId, userId);
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
    public ResponseEntity<SuccessResponse> removeBookFromWishList(@PathVariable @Positive(message = "User ID must be positive") Long userId, @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishlistId, @PathVariable @Positive(message = "Book ID must be positive") Long bookId) {
        log.info("Removing book '{}' from wishlist '{}' for user {}", bookId, wishlistId, userId);
        RemoveBookFromWishListRequest request = new RemoveBookFromWishListRequest(userId, wishlistId, bookId);
        SuccessResponse response = wishListService.removeBookFromWishList(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Add Multiple Books to Wishlist
     * @param userId ID of the User
     * @param wishlistId ID of the Wishlist from which the book will be removed
     * @param request the bulk add request containing the book IDs
     * @return detailed response about the bulk operation results
     */
    @PostMapping("/wishlists/{wishlistId}/books/bulk")
    public ResponseEntity<BulkOperationResponse> addMultipleBooksToWishList(@PathVariable @Positive(message = "User ID must be positive") Long userId, @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishlistId, @RequestBody @Valid BulkAddBooksRequest request) {
        log.info("Bulk adding {} books to wishlist {} for user {}", request.getBookIds().size(), wishlistId, userId);

        BulkAddBooksToWishListRequest serviceRequest = BulkAddBooksToWishListRequest.builder()
                    .userId(userId)
                    .wishListId(wishlistId)
                    .bookIds(request.getBookIds())
                    .build();
        
        BulkOperationResponse response = wishListService.addMultipleBooksToWishList(serviceRequest);
        HttpStatus status = response.getFailed() > 0 ? HttpStatus.MULTI_STATUS : HttpStatus.OK;
        return new ResponseEntity<>(response, status);            
    }

    public ResponseEntity<BulkOperationResponse> removeMultipleBooksFromWishList(@PathVariable @Positive(message = "User ID must be positive") Long userId, @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishlistId, BulkRemoveBooksRequest request) {
        log.info("Bulk removing {} books from wishlist {} for user {}", request.getBookIds().size(), wishlistId, userId);

        BulkRemoveBookFromWishListRequest serviceRequest = BulkRemoveBookFromWishListRequest.builder()
                    .userId(userId)
                    .wishListId(wishlistId)
                    .bookIds(request.getBookIds())
                    .build();

        BulkOperationResponse response = wishListService.removeMultipleBooksFromWishList(serviceRequest);
        HttpStatus status = response.getFailed() > 0 ? HttpStatus.MULTI_STATUS : HttpStatus.OK;
        return new ResponseEntity<>(response, status);
                    
    }

}
