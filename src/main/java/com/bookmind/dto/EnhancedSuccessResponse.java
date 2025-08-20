package com.bookmind.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedSuccessResponse {
    
    private boolean success;
    private String message;
    private Object data;
    private long timestamp;
    private String operationType;
    private int affectedItems;

}
