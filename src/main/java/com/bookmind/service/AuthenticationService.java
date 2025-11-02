package com.bookmind.service;

import com.bookmind.dto.*;
import com.bookmind.exception.UserNotFoundException;
import com.bookmind.model.User;
import com.bookmind.repository.UserRepository;
import com.bookmind.utility.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Register a new user
     * 
     * @param registerRequest the registration request containing user details
     * @return RegisterResponse with registration status and user info
     */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest registerRequest) {
        log.info("Registering new user with username: {}", registerRequest.getUsername());

        // Check if username or email already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Registration failed: Username {} is already taken", registerRequest.getUsername());
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed: Email {} is already in use", registerRequest.getEmail());
            throw new IllegalArgumentException("Email is already in use");
        }

        // Create new user entity
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setRoles(new HashSet<>() {
            {
                add("USER");
            }
        });

        User savedUser = userRepository.save(user);
        log.info("User {} registered successfully with ID: {}", savedUser.getUsername(), savedUser.getId());

        return RegisterResponse.builder()
                .success(true)
                .message("User registered successfully")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .emailVerified(false)
                .build();
    }

    /**
     * Login user and generate JWT tokens
     * 
     * @param loginRequest the login request containing username and password
     * @return LoginResponse with tokens and user info
     */
    @Transactional
    public LoginResponse loginUser(LoginRequest loginRequest) {
        log.info("Authenticating user with username: {}", loginRequest.getUsername());

        try {
            // Authenticate user credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // Fetch user from database
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Generate JWT tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            log.info("User {} authenticated successfully", user.getUsername());

            return LoginResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .createdAt(user.getCreatedAt())
                    .emailVerified(user.isEmailVerified())
                    .build();

        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for username: {}", loginRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * Refresh access token using refresh token
     * 
     * @param refreshRequest the refresh token request
     * @return TokenRefreshResponse with new access token
     */
    @Transactional(readOnly = true)
    public TokenRefreshResponse refreshAccessToken(TokenRefreshRequest refreshRequest) {
        log.info("Refreshing access token");

        String requestRefreshToken = refreshRequest.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenProvider.validateRefreshToken(requestRefreshToken)) {
            log.warn("Invalid refresh token");
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Extract username from refresh token
        String username = jwtTokenProvider.getUsernameFromRefreshToken(requestRefreshToken);
        if (username == null) {
            log.warn("Could not extract username from refresh token");
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);
        log.info("Access token refreshed successfully for user: {}", username);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .build();
    }

    /**
     * Get current authenticated user details
     * 
     * @param username the username of the current user
     * @return User entity of the current user
     */
    @Transactional(readOnly = true)
    public User getCurrentUser(String username) {
        log.debug("Fetching current user: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UserNotFoundException("User not found: " + username);
                });
    }

    /**
     * Logout user
     * 
     * @param username the username to logout
     */
    @Transactional
    public void logoutUser(String username) {
        log.info("User {} logged out", username);
    }

    /**
     * Validate access token
     * 
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateAccessToken(token);
    }

    /**
     * Get username from token
     * 
     * @param token the JWT token
     * @return username extracted from token
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }
}