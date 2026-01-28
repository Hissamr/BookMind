package com.bookmind.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    
    private Long bookId;
    private String title;
    private double price;
    private int quantity;
    private double totalPrice;

}
