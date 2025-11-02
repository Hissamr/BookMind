package com.bookmind.controller;

import com.bookmind.dto.*;
import com.bookmind.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Register a new user account
     * 
     * POST /api/auth/register
     * 
     * @param registerRequest containing username, email, and password
     * @return RegisterResponse with user details and success status
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for username: {}", registerRequest.getUsername());
        try {
            RegisterResponse response = authenticationService.registerUser(registerRequest);
            log.info("User registered successfully with username: {}", registerRequest.getUsername());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for username: {}: {}", registerRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RegisterResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error during registration for username: {}: {}", registerRequest.getUsername(),
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RegisterResponse.builder()
                            .success(false)
                            .message("Unexpected error occurred")
                            .build());
        }

    }

    /**
     * Authenticate user and generate JWT tokens
     * 
     * POST /api/auth/login
     * 
     * @param loginRequest containing username and password
     * @return LoginResponse with JWT tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.getEmail());
        try {
            LoginResponse response = authenticationService.loginUser(loginRequest);
            log.info("User logged in successfully for email: {}", loginRequest.getEmail());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Login failed for email: {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.builder()
                            .success(false)
                            .message("Unexpected error occurred")
                            .build());
        }
    }

    /**
     * Refresh access token using refresh token
     * 
     * POST /api/auth/refresh
     * 
     * @param refreshRequest containing refresh token
     * @return TokenRefreshResponse with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        log.info("Token refresh request received");
        try {
            TokenRefreshResponse response = authenticationService.refreshAccessToken(refreshRequest);
            log.info("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenRefreshResponse.builder()
                            .accessToken(null)
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TokenRefreshResponse.builder()
                            .accessToken(null)
                            .build());
        }
    }

    /**
     * Get current authenticated user profile
     * 
     * GET /api/auth/me
     * 
     * @return UserSummaryDto with current user information
     */
    @GetMapping("/me")
    public ResponseEntity<UserSummaryDto> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Unauthorized access attempt to /me endpoint");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String username = authentication.getName();
            log.debug("Fetching current user profile for: {}", username);

            com.bookmind.model.User user = authenticationService.getCurrentUser(username);

            UserSummaryDto userDto = UserSummaryDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .emailVerified(user.isEmailVerified())
                    .createdAt(user.getCreatedAt())
                    .roles(user.getRoles())
                    .build();

            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            log.error("Error fetching current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Logout user (invalidate session/tokens)
     * 
     * POST /api/auth/logout
     * 
     * @return SuccessResponse indicating logout status
     */
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logoutUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                log.info("Logout request from user: {}", username);
                authenticationService.logoutUser(username);
                SecurityContextHolder.clearContext();
            }

            return ResponseEntity.ok(SuccessResponse.builder()
                    .success(true)
                    .message("Logged out successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SuccessResponse.builder()
                            .success(false)
                            .message("Logout failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Validate JWT token
     * 
     * POST /api/auth/validate
     * 
     * @param token the JWT token to validate
     * @return SuccessResponse with validation status
     */
    @PostMapping("/validate")
    public ResponseEntity<SuccessResponse> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authenticationService.validateToken(token);

            if (isValid) {
                String username = authenticationService.getUsernameFromToken(token);
                log.debug("Token validated for user: {}", username);
                return ResponseEntity.ok(SuccessResponse.builder()
                        .success(true)
                        .message("Token is valid")
                        .build());
            } else {
                log.warn("Invalid token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(SuccessResponse.builder()
                                .success(false)
                                .message("Token is invalid or expired")
                                .build());
            }
        } catch (Exception e) {
            log.error("Error validating token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SuccessResponse.builder()
                            .success(false)
                            .message("Token validation error: " + e.getMessage())
                            .build());
        }
    }
}
