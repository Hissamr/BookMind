package com.bookmind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new WishList
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWishListRequest {
    
    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotBlank(message = "WishList name cannot be blank")
    @Size(min = 1, max = 100, message = "WishList name must be between 1 and 100 characters")
    private String name;
}
