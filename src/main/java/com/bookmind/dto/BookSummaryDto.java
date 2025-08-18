package com.bookmind.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing basic book information in WishList responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryDto {
    private Long id;
    private String title;
    private String author;
    private String genre;
    private double price;
    private Boolean available;
    private double averageRating;
    private String coverImageUrl;
    private String isbn;
}
