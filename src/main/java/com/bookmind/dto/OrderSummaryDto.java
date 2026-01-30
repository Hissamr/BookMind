package com.bookmind.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDto {
    
    private Long id;
    private String status;
    private double totalAmount;
    private LocalDateTime orderDate;
    private int itemCount;

}
