package com.bookmind.repository;

import com.bookmind.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    // Additional methods for WishList can be defined here if needed
}