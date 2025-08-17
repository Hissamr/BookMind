package com.bookmind.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WishListResponse {
    private Long id;
    private String name;
    private Long userId;
    private String username; // For display purposes
    private int bookCount;
    private List<BookSummaryDto> books;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class BookSummaryDto {
        private Long id;
        private String title;
        private String author;
        private Double price;
        private String imageUrl;
    }
}
