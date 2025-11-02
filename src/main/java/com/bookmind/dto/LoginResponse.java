package com.bookmind.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private boolean emailVerified;
}
