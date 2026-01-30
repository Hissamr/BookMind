package com.bookmind.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for removing a book from cart.
 * Note: userId is NOT included - it comes from the authenticated JWT token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveFromCartRequest {

    @Positive(message = "Book ID must be positive")
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;

}
