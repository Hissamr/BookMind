package com.bookmind.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for WishList operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishListResponse {
    private Long id;
    private String name;
    private UserSummaryDto user;
    private List<BookSummaryDto> books;
    private int bookCount;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Convenience constructor without user details (for internal operations)
    public WishListResponse(Long id, String name, List<BookSummaryDto> books, 
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.books = books;
        this.bookCount = books != null ? books.size() : 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Method to set books and automatically update count
    public void setBooks(List<BookSummaryDto> books) {
        this.books = books;
        this.bookCount = books != null ? books.size() : 0;
    }
}
