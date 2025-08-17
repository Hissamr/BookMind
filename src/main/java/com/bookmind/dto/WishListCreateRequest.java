package com.bookmind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WishListCreateRequest {
    
    @NotBlank(message = "Wishlist name is required")
    @Size(min = 1, max = 100, message = "Wishlist name must be between 1 and 100 characters")
    private String name;
}
