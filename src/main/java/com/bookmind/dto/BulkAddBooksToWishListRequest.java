package com.bookmind.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAddBooksToWishListRequest {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Wishlist ID is required")
    @Positive(message = "Wishlist ID must be positive")
    private Long wishListId;

    @NotNull(message =  "Book IDs list is required")
    @NotEmpty(message = "Book IDs list cannot be empty")
    @Size(min = 1, max = 50, message = "Can add between 1 and 50 books at once")
    private List<@NotNull(message = "Book IDs cannot be null") @Positive(message = "Book ID must be positive") Long> bookIds;

}
