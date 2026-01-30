package com.bookmind.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a book to cart.
 * Note: userId is NOT included - it comes from the authenticated JWT token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @Positive(message = "Book ID must be positive")
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;  // Default to 1

}
