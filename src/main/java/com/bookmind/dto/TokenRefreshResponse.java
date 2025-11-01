package com.bookmind.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshResponse {
    
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";

}
