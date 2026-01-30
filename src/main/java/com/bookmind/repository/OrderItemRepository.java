package com.bookmind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookmind.model.OrderItem;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Find all order items by order ID
    List<OrderItem> findByOrderId(Long orderId);

    // Find all order items by book ID
    List<OrderItem> findByBookId(Long bookId);

    // Count order items by book ID
    long countByBookId(Long bookId);

    // Check if a user has purchased a specific book
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.order.user.id = :userId AND oi.book.id = :bookId")
    boolean hasUserPurchasedBook(@Param("userId") Long userId, @Param("bookId") Long bookId);

    // Delete all order items by order ID
    void deleteAllByOrderId(Long orderId);
    
}
