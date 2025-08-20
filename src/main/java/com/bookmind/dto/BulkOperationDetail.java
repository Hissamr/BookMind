package com.bookmind.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationDetail {

    private Long bookId;
    private String status;
    private String reason;
    private String bookDescription;

}
