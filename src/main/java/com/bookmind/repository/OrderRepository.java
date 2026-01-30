package com.bookmind.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import com.bookmind.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find all orders by user ID
    List<Order> findByUserId(Long userId);

    // Find  user's orders sorted by date (newest first)
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    // Find orders by status
    List<Order> findByStatus(String status);

    // Find user's orders filtered by status
    List<Order> findByUserIdAndStatus(Long userId, String status);

    // Find specific order by user ID and order ID
    Optional<Order> findByUserIdAndId(Long userId, Long id);

    // Count orders by user ID
    long countByUserId(Long userId);

    // Count orders by status
    long countByStatus(String status);

    // Find orders within a date range
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
}
