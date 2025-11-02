package com.bookmind.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for representing basic user information in WishList responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private Set<String> roles;
}
