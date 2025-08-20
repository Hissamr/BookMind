package com.bookmind.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishListStatsRequest {
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Wishlist ID is required")
    @Positive(message = "Wishlist ID must be positive")
    private Long wishListId;

    @Builder.Default
    @Min(value = 1, message = "Days must be at least 1")
    @Max(value = 365, message = "Days cannot exceed 365")
    private Integer days = 7;

    @Builder.Default
    @Min(value = 1, message = "Recent limit must be at least 1")
    @Max(value = 100, message = "Recent limit cannot exceed 100")
    private Integer recentLimit = 5;
    
}
