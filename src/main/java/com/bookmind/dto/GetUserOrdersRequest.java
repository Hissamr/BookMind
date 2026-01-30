package com.bookmind.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserOrdersRequest {
    
    @Positive(message = "User ID must be positive")
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    // Optional - if null, returns all orders; otherwise filters by status
    private String status;

}
