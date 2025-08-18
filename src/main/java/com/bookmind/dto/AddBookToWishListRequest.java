package com.bookmind.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a book to a WishList
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBookToWishListRequest {
    
    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotNull(message = "WishList ID cannot be null")
    @Positive(message = "WishList ID must be positive")
    private Long wishListId;
    
    @NotNull(message = "Book ID cannot be null")
    @Positive(message = "Book ID must be positive")
    private Long bookId;
}
