package com.bookmind.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    
    private Long bookId;
    private String title;
    private String author;
    private double price;
    private int quantity;
    private double totalPrice;

}
