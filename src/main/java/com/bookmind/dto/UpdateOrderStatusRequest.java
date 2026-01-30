package com.bookmind.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    
    @Positive(message = "Order ID must be positive")
    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Status cannot be null")
    private String status;

}
