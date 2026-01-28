package com.bookmind.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.bookmind.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    //Find item by cart and book (to update quantity if exists)
    Optional<CartItem> findByCartIdAndBookId(Long cartId, Long bookId);

    //check if item exists in cart by cart ID and book ID
    boolean existsByCartIdAndBookId(Long cartId, Long bookId);

    //Delete all items by cart ID (when clearing cart)
    void deleteAllByCartId(Long cartId);
}
