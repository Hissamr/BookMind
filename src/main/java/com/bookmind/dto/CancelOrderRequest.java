package com.bookmind.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    
    @Positive(message = "User ID must be positive")
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Positive(message = "Order ID must be positive")
    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

}
