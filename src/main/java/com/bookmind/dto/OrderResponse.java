package com.bookmind.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private String status;
    private double totalAmount;
    private String shippingAddress;
    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;
    private List<OrderItemDto> items;

}
