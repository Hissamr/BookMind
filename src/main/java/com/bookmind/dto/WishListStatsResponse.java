package com.bookmind.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishListStatsResponse {
    
    private Long wishListId;
    private String wishListName;
    private int totalBooks;
    private int booksAddedToday;
    private int booksRemovedToday;
    private String lastModified;
    private List<BookSummaryDto> recentlyAdded;

}
