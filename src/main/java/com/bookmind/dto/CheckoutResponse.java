package com.bookmind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    
    private boolean success;
    private String message;
    private Long orderId;
    private double totalAmount;
    private LocalDate estimatedDeliveryDate;

}
