package com.bookmind.repository;

import com.bookmind.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    // Additional methods for WishList can be defined here if needed

    @Query("SELECT w FROM WishList w WHERE w.user.id = :userId AND w.id = :wishlistId")
    Optional<WishList> findByUserIdAndWishListId(@Param("userId") Long userId, @Param("wishlistId") Long whislistId);

    @Query(value = """ 
    SELECT COUNT(w) > 0 FROM whisList w
    WHERE w.user.id = :userId
    AND LOWER(w.name) = LOWER(:name)
    """)
    boolean existsUserByIdAndName(@Param("userId") Long userId, @Param("name") String name);

    @Query(value = """ 
    SELECT COUNT(w) > 0 FROM whisList w
    WHERE w.user.id = :userId
    AND LOWER(w.name) = LOWER(:name)
    AND w.id <> :wishlistId
    """)
    boolean existsByUserIdAndNameExceptId(@Param("userId") Long userId, @Param("name") String name, @Param("whislistId") Long wishlistId);
}