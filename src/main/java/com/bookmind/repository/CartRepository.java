package com.bookmind.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookmind.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    //Find cart by user ID
    Optional<Cart> findByUserId(Long userId);

    //Check if cart exists for user ID
    boolean existsByUserId(Long userId);

    //Find active (not checked out) cart by user ID
    Optional<Cart> findByUserIdAndCheckedOutFalse(Long userId);
}
