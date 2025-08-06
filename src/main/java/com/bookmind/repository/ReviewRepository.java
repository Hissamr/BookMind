package com.bookmind.repository;

import com.bookmind.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Custom query methods can be defined here if needed
    // For example:
    // List<Review> findByBookId(Long bookId);
    // List<Review> findByUserId(Long userId);

}
