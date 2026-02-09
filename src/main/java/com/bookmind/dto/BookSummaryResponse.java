package com.bookmind.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for book summary response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryResponse {
    
    private Long bookId;
    private String title;
    private String author;
    private String summary;
    private LocalDateTime generatedAt;
    private boolean cached;
    private String message;
}
