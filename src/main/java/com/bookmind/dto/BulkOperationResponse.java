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
public class BulkOperationResponse {
    
    private boolean success;
    private String message;
    private int totalRequested;
    private int totalProcessed;
    private int skipped;
    private int failed;
    private List<BulkOperationDetail> details;

}
