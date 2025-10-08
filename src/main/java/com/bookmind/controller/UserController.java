package com.bookmind.controller;

import com.bookmind.model.User;
import com.bookmind.service.CustomOAuth2User;
import com.bookmind.service.UserService;
import com.bookmind.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "User not authenticated"));
            }

            Object principal = authentication.getPrincipal();
            User user = null;

            if (principal instanceof CustomOAuth2User) {
                CustomOAuth2User oAuth2User = (CustomOAuth2User) principal;
                user = oAuth2User.getUser();
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                // For form-based authentication
                org.springframework.security.core.userdetails.User userDetails = 
                    (org.springframework.security.core.userdetails.User) principal;
                Optional<User> foundUser = userService.findByEmail(userDetails.getUsername());
                if (foundUser.isPresent()) {
                    user = foundUser.get();
                }
            }

            if (user == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found"));
            }

            // Return user profile data
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("id", user.getId());
            userProfile.put("email", user.getEmail());
            userProfile.put("username", user.getUsername());
            userProfile.put("firstName", user.getFirstName());
            userProfile.put("lastName", user.getLastName());
            userProfile.put("pictureUrl", user.getPictureUrl());
            userProfile.put("provider", user.getProvider());
            userProfile.put("roles", user.getRoles());
            userProfile.put("enabled", user.isEnabled());
            userProfile.put("emailVerified", user.isEmailVerified());
            userProfile.put("createdAt", user.getCreatedAt());

            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Generate JWT token for current session
     */
    @PostMapping("/token")
    public ResponseEntity<?> generateToken(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "User not authenticated"));
            }

            String token = jwtUtil.generateTokenFromAuthentication(authentication);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("expiresIn", 86400); // 24 hours

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating token", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error generating token"));
        }
    }

    /**
     * Check authentication status
     */
    @GetMapping("/auth-status")
    public ResponseEntity<?> getAuthStatus(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("authenticated", true);
            response.put("principal", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
        } else {
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error during logout"));
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates, 
                                         Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "User not authenticated"));
            }

            Object principal = authentication.getPrincipal();
            User user = null;

            if (principal instanceof CustomOAuth2User) {
                CustomOAuth2User oAuth2User = (CustomOAuth2User) principal;
                user = oAuth2User.getUser();
            } else {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Profile update not supported for this authentication type"));
            }

            // Update allowed fields
            boolean updated = false;
            
            if (updates.containsKey("firstName") && user.getFirstName() != null) {
                user.setFirstName(updates.get("firstName"));
                updated = true;
            }
            
            if (updates.containsKey("lastName") && user.getLastName() != null) {
                user.setLastName(updates.get("lastName"));
                updated = true;
            }

            if (updated) {
                user = userService.save(user);
                return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No valid updates provided"));
            }

        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error updating profile"));
        }
    }
}
