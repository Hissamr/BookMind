package com.bookmind.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookmind.dto.AddToCartRequest;
import com.bookmind.dto.CartResponse;
import com.bookmind.dto.CheckoutResponse;
import com.bookmind.dto.RemoveFromCartRequest;
import com.bookmind.dto.UpdateCartItemRequest;
import com.bookmind.security.AuthenticatedUserProvider;
import com.bookmind.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Cart operations.
 * 
 * SECURITY: User ID is extracted from JWT token, NOT from URL or request body.
 * This prevents users from accessing or modifying other users' carts.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;
    private final AuthenticatedUserProvider authProvider;

    /**
     * Get the cart for the authenticated user
     * 
     * @return the CartResponse DTO
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        Long userId = authProvider.getCurrentUserId();
        log.info("Fetching cart for authenticated user {}", userId);

        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Add a book to the authenticated user's cart
     * 
     * @param request contains bookId and quantity
     * @return the updated CartResponse DTO
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@RequestBody @Valid AddToCartRequest request) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Adding book {} to cart for authenticated user {}", request.getBookId(), userId);

        CartResponse response = cartService.addToCart(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update the quantity of a book in the authenticated user's cart
     * 
     * @param request contains bookId and new quantity
     * @return the updated CartResponse DTO
     */
    @PutMapping("/items")
    public ResponseEntity<CartResponse> updateCartItem(@RequestBody @Valid UpdateCartItemRequest request) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Updating book {} quantity in cart for authenticated user {}", request.getBookId(), userId);

        CartResponse response = cartService.updateCartItemQuantity(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a book from the authenticated user's cart
     * 
     * @param request contains bookId
     * @return the updated CartResponse DTO
     */
    @DeleteMapping("/items")
    public ResponseEntity<CartResponse> removeFromCart(@RequestBody @Valid RemoveFromCartRequest request) {
        Long userId = authProvider.getCurrentUserId();
        log.info("Removing book {} from cart for authenticated user {}", request.getBookId(), userId);

        CartResponse response = cartService.removeFromCart(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Clear all items from the authenticated user's cart
     * 
     * @return the empty CartResponse DTO
     */
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart() {
        Long userId = authProvider.getCurrentUserId();
        log.info("Clearing cart for authenticated user {}", userId);

        CartResponse response = cartService.clearCart(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Checkout the authenticated user's cart (convert to order)
     * 
     * @return the CheckoutResponse DTO with order details
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkoutCart() {
        Long userId = authProvider.getCurrentUserId();
        log.info("Checking out cart for authenticated user {}", userId);

        CheckoutResponse response = cartService.checkoutCart(userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

