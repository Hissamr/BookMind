package com.bookmind.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookmind.dto.BulkAddBooksRequest;
import com.bookmind.dto.BulkOperationResponse;
import com.bookmind.dto.BulkRemoveBooksRequest;
import com.bookmind.dto.CreateWishListRequest;
import com.bookmind.dto.SuccessResponse;
import com.bookmind.dto.WishListResponse;
import com.bookmind.security.AuthenticatedUserProvider;
import com.bookmind.service.WishListService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for WishList operations.
 * 
 * SECURITY: User ID is extracted from JWT token, NOT from URL or request body.
 * This prevents users from accessing or modifying other users' wishlists.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlists")
public class WishListController {

    private final WishListService wishListService;
    private final AuthenticatedUserProvider authProvider;

    /**
     * Get all wishlists for the authenticated user
     * 
     * @return a List of WishListResponse DTOs
     */
    @GetMapping
    public ResponseEntity<List<WishListResponse>> getAllWishLists() {
        Long userId = authProvider.getCurrentUserId();
        log.info("Fetching all wishlists for authenticated user {}", userId);

        List<WishListResponse> response = wishListService.getAllWishListsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single wishlist by ID for the authenticated user
     * 
     * @param wishListId the ID of the wishlist to retrieve
     * @return the WishListResponse DTO
     */
    @GetMapping("/{wishListId}")
    public ResponseEntity<WishListResponse> getWishListById(
            @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Fetching wishlist {} for authenticated user {}", wishListId, userId);

        WishListResponse response = wishListService.getWishListById(userId, wishListId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new wishlist for the authenticated user
     * 
     * @param request the wishlist creation request containing the name
     * @return the WishListResponse DTO
     */
    @PostMapping
    public ResponseEntity<WishListResponse> createWishList(
            @RequestBody @Valid CreateWishListRequest request) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Creating new wishlist '{}' for authenticated user {}", request.getName(), userId);

        WishListResponse response = wishListService.createWishList(userId, request.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update a wishlist name for the authenticated user
     * 
     * @param wishListId ID of the WishList to be updated
     * @param request contains the new wishlist name
     * @return WishListResponse
     */
    @PutMapping("/{wishListId}")
    public ResponseEntity<WishListResponse> updateWishList(
            @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId,
            @RequestBody @Valid CreateWishListRequest request) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Updating wishlist {} to '{}' for authenticated user {}", wishListId, request.getName(), userId);

        WishListResponse response = wishListService.updateWishList(userId, wishListId, request.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a wishlist for the authenticated user
     * 
     * @param wishListId ID of the WishList to be deleted
     * @return SuccessResponse indicating the deletion was successful
     */
    @DeleteMapping("/{wishListId}")
    public ResponseEntity<SuccessResponse> deleteWishList(
            @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Deleting wishlist {} for authenticated user {}", wishListId, userId);

        SuccessResponse response = wishListService.deleteWishList(userId, wishListId);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    /**
     * Add a book to a wishlist
     * 
     * @param wishListId ID of the Wishlist
     * @param bookId ID of the Book to be added
     * @return SuccessResponse indicating the book was added successfully
     */
    @PostMapping("/{wishListId}/books/{bookId}")
    public ResponseEntity<SuccessResponse> addBookToWishList(
            @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId,
            @PathVariable @Positive(message = "Book ID must be positive") Long bookId) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Adding book {} to wishlist {} for authenticated user {}", bookId, wishListId, userId);

        SuccessResponse response = wishListService.addBookToWishList(userId, wishListId, bookId);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a book from a wishlist
     * 
     * @param wishListId ID of the Wishlist
     * @param bookId ID of the Book to be removed
     * @return SuccessResponse indicating the book was removed successfully
     */
    @DeleteMapping("/{wishListId}/books/{bookId}")
    public ResponseEntity<SuccessResponse> removeBookFromWishList(
            @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId,
            @PathVariable @Positive(message = "Book ID must be positive") Long bookId) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Removing book {} from wishlist {} for authenticated user {}", bookId, wishListId, userId);

        SuccessResponse response = wishListService.removeBookFromWishList(userId, wishListId, bookId);
        return ResponseEntity.ok(response);
    }

    /**
     * Bulk add multiple books to a wishlist
     * 
     * @param wishListId ID of the Wishlist
     * @param request contains list of book IDs to add
     * @return detailed response about the bulk operation results
     */
    @PostMapping("/{wishListId}/books/bulk")
    public ResponseEntity<BulkOperationResponse> addMultipleBooksToWishList(
            @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId,
            @RequestBody @Valid BulkAddBooksRequest request) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Bulk adding {} books to wishlist {} for authenticated user {}",
                request.getBookIds().size(), wishListId, userId);

        BulkOperationResponse response = wishListService.addMultipleBooksToWishList(userId, wishListId, request.getBookIds());
        HttpStatus status = response.getFailed() > 0 ? HttpStatus.MULTI_STATUS : HttpStatus.OK;
        return new ResponseEntity<>(response, status);
    }

    /**
     * Bulk remove multiple books from a wishlist
     * 
     * @param wishListId ID of the Wishlist
     * @param request contains list of book IDs to remove
     * @return detailed response about the bulk operation results
     */
    @DeleteMapping("/{wishListId}/books/bulk")
    public ResponseEntity<BulkOperationResponse> removeMultipleBooksFromWishList(
            @PathVariable @Positive(message = "Wishlist ID must be positive") Long wishListId,
            @RequestBody @Valid BulkRemoveBooksRequest request) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Bulk removing {} books from wishlist {} for authenticated user {}",
                request.getBookIds().size(), wishListId, userId);

        BulkOperationResponse response = wishListService.removeMultipleBooksFromWishList(userId, wishListId, request.getBookIds());
        HttpStatus status = response.getFailed() > 0 ? HttpStatus.MULTI_STATUS : HttpStatus.OK;
        return new ResponseEntity<>(response, status);
    }
}
