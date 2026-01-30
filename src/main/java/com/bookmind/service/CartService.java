package com.bookmind.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bookmind.dto.AddToCartRequest;
import com.bookmind.dto.CartItemDto;
import com.bookmind.dto.CartResponse;
import com.bookmind.dto.GetCartRequest;
import com.bookmind.dto.RemoveFromCartRequest;
import com.bookmind.dto.UpdateCartItemRequest;
import com.bookmind.dto.ClearCartRequest;
import com.bookmind.dto.CheckoutCartRequest;
import com.bookmind.dto.CheckoutResponse;
import com.bookmind.exception.BookNotFoundException;
import com.bookmind.exception.BookNotInCartException;
import com.bookmind.exception.CartNotFoundException;
import com.bookmind.exception.CartEmptyException;
import com.bookmind.exception.CartAlreadyCheckedOutException;
import com.bookmind.exception.UserNotFoundException;
import com.bookmind.model.Book;
import com.bookmind.model.Cart;
import com.bookmind.model.CartItem;
import com.bookmind.model.Order;
import com.bookmind.model.User;
import com.bookmind.repository.BookRepository;
import com.bookmind.repository.CartItemRepository;
import com.bookmind.repository.CartRepository;
import com.bookmind.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    /**
     * Get cart for a user
     * @param request GetCartRequest containing userId
     * @return CartResponse with cart details
     * @throws CartNotFoundException if cart not found
     */
    public CartResponse getCart(GetCartRequest request) {
        log.info("Fetching cart details for user ID: {}", request.getUserId());
        
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new CartNotFoundException(request.getUserId()));

        return toCartResponse(cart);
    }

    /**
     * Add a book to user's cart. Creates cart if it doesn't exist.
     * If book already in cart, increases quantity.
     * 
     * @param request AddToCartRequest containing userId, bookId, quantity
     * @return CartResponse with updated cart details
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public CartResponse addToCart(AddToCartRequest request) {
        log.info("Adding book ID: {} (qty: {}) to cart for user ID: {}", 
                request.getBookId(), request.getQuantity(), request.getUserId());
        
        // 1. Get or create cart for user
        Cart cart = getOrCreateCart(request.getUserId());

        // 2. Get the book
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException(request.getBookId()));

        // 3. Check if book already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), request.getBookId());

        if (existingItem.isPresent()) {
            // Book already in cart - increase quantity
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(cartItem);
            log.info("Updated quantity for book ID: {} in cart. New qty: {}", 
                    request.getBookId(), cartItem.getQuantity());
        } else {
            // New book - create new CartItem
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setBook(book);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(book.getPrice());  // Snapshot price at time of adding
            cart.addCartItem(cartItem);
            log.info("Added new book ID: {} to cart", request.getBookId());
        }

        // 4. Recalculate total and save
        cart.recalculateTotalPrice();
        Cart savedCart = cartRepository.save(cart);

        return toCartResponse(savedCart);
    }

    /**
     * Get existing cart or create new one for user
     * 
     * @param userId ID of the user
     * @return Cart object
     */
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new cart for user ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException(userId));
                    
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(0.0);
                    newCart.setCheckedOut(false);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Remove a book from user's cart completely.
     * 
     * @param request RemoveFromCartRequest containing userId, bookId
     * @return CartResponse with updated cart details
     * @throws CartNotFoundException if cart not found
     * @throws BookNotInCartException if book not in cart
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public CartResponse removeFromCart(RemoveFromCartRequest request) {
        log.info("Removing book ID: {} from cart for user ID: {}", 
                request.getBookId(), request.getUserId());
        
        // 1. Get cart (must exist for remove operation)
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new CartNotFoundException(request.getUserId()));

        // 2. Find the cart item
        CartItem cartItem = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), request.getBookId())
                .orElseThrow(() -> new BookNotInCartException(request.getBookId(), cart.getId()));

        // 3. Remove item from cart (orphanRemoval=true will delete from DB)
        cart.removeCartItem(cartItem);
        log.info("Removed book ID: {} from cart for user ID: {}", 
                request.getBookId(), request.getUserId());

        // 4. Recalculate total and save
        cart.recalculateTotalPrice();
        Cart savedCart = cartRepository.save(cart);

        return toCartResponse(savedCart);
    }
    
    /**
     * Update quantity of a book in user's cart.
     * 
     * @param request UpdateCartItemRequest containing userId, bookId, quantity
     * @return CartResponse with updated cart details
     * @throws CartNotFoundException if cart not found
     * @throws BookNotInCartException if book not in cart
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public CartResponse updateCartItemQuantity(UpdateCartItemRequest request) {
        log.info("Updating quantity for book ID: {} in cart for user ID: {} to qty: {}", request.getBookId(), request.getUserId(), request.getQuantity());
        
        // 1. Get cart (must exist for update operation)
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new CartNotFoundException(request.getUserId()));

        // 2. Find the cart item
        CartItem cartItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), request.getBookId())
                .orElseThrow(() -> new BookNotInCartException(request.getBookId(), cart.getId()));
        
        // 3. Update quantity
        cartItem.setQuantity(request.getQuantity());
        log.info("Updated quantity for book ID: {} in cart for user ID: {} to qty: {}", request.getBookId(), request.getUserId(), request.getQuantity());

        cart.recalculateTotalPrice();
        Cart savedCart = cartRepository.save(cart);

        return toCartResponse(savedCart);
    }

    /**
     * Clear all items from user's cart.
     * 
     * @param request ClearCartRequest containing userId
     * @return CartResponse with updated (empty) cart details
     * @throws CartNotFoundException if cart not found
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public CartResponse clearCart(ClearCartRequest request) {
        log.info("Clearing cart for user ID: {}", request.getUserId());

        // 1. Get cart (must exist for clear operation)
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new CartNotFoundException(request.getUserId()));

        // 2. Clear all items from the cart
        cart.clearCart();
        log.info("Cleared all items from cart for user ID: {}", request.getUserId());
        // 3. Save the cleared cart
        Cart savedCart = cartRepository.save(cart);

        return toCartResponse(savedCart);
    }

    /**
     * Checkout user's cart and create an order.
     * 
     * @param request CheckoutCartRequest containing userId and shippingAddress
     * @return CheckoutResponse with order details
     * @throws CartNotFoundException if cart not found
     * @throws CartEmptyException if cart is empty
     * @throws CartAlreadyCheckedOutException if cart already checked out
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public CheckoutResponse checkoutCart(CheckoutCartRequest request) {
        log.info("Checking out cart for user ID: {}", request.getUserId());

        // 1. Get cart (must exist for checkout operation)
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new CartNotFoundException(request.getUserId()));
        
        // 2. Validate cart is not already checked out
        if (cart.isCheckedOut()) {
            log.warn("Cart already checked out for user ID: {}", request.getUserId());
            throw new CartAlreadyCheckedOutException(cart.getId());
        }

        // 3. Validate cart is not empty
        if (cart.getItems().isEmpty()) {
            log.warn("Cannot checkout an empty cart for user ID: {}", request.getUserId());
            throw new CartEmptyException(request.getUserId());
        }

        // 4. Create order from cart
        Order order = orderService.createOrderFromCart(cart, request.getShippingAddress());
        log.info("Order created with ID: {} from cart ID: {}", order.getId(), cart.getId());

        // 5. Clear the cart (keep cart entity, just remove items)
        cart.clearCart();
        cartRepository.save(cart);
        log.info("Cart ID: {} cleared after checkout for user ID: {}", cart.getId(), request.getUserId());

        return CheckoutResponse.builder()
                .success(true)
                .message("Checkout successful! Your order has been placed.")
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .estimatedDeliveryDate(java.time.LocalDate.now().plusDays(7))
                .build();
    }   

    // ==================== MAPPER METHODS ====================

    /**
     * Convert Cart entity to CartResponse DTO
     * 
     * @param cart the Cart entity
     * @return CartResponse DTO
     */
    private CartResponse toCartResponse(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemDtos)
                .totalPrice(cart.getTotalPrice())
                .totalItems(itemDtos.size())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    /**
     * Convert CartItem entity to CartItemDto
     * 
     * @param cartItem the CartItem entity
     * @return CartItemDto DTO
     */
    private CartItemDto toCartItemDto(CartItem cartItem) {
        return new CartItemDto(
                cartItem.getBook().getId(),
                cartItem.getBook().getTitle(),
                cartItem.getPrice(),
                cartItem.getQuantity(),
                cartItem.getPrice() * cartItem.getQuantity()  // totalPrice for this item
        );
    }
}
