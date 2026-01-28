package com.bookmind.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCartRequest {

    @Positive(message = "User ID must be positive")
    @NotNull(message = "User ID cannot be null")
    private Long userId;

}
