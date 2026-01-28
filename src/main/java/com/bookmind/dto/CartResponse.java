package com.bookmind.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    
    private Long id;
    private Long userId;
    private List<CartItemDto> items;  // Use DTO, not entity
    private double totalPrice;
    private int totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
