package com.bookmind.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkAddBooksRequest {
    
    @NotNull(message = "Book IDs list is required")
    @NotEmpty(message = "Book IDs list cannot be empty")
    @Size(min = 1, max = 50, message = "Can add between 1 and 50 books at once")
    private List<@NotNull(message = "Book ID cannot be null") @Positive(message = "Book ID must be positive") Long> bookIds;

}
