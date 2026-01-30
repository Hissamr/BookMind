package com.bookmind.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class to extract authenticated user information from Security Context.
 * Use this instead of exposing userId in URLs or request bodies.
 * 
 * How it works:
 * 1. User logs in → JWT token is created with username
 * 2. User makes request with token → JwtAuthenticationFilter validates it
 * 3. Filter loads UserDetails and sets Authentication in SecurityContext
 * 4. This utility extracts userId from the authenticated CustomUserDetails
 */
@Component
public class AuthenticatedUserProvider {

    /**
     * Get the current authenticated user's ID from the Security Context.
     * 
     * @return the userId of the authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }

        throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
    }

    /**
     * Get the current authenticated user's username.
     * 
     * @return the username of the authenticated user
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUsername();
        }

        return authentication.getName();
    }

    /**
     * Get the full CustomUserDetails of the authenticated user.
     * 
     * @return CustomUserDetails of the authenticated user
     */
    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }

        throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
    }
}
