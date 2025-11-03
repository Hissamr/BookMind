package com.bookmind.security;

import com.bookmind.utility.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts HTTP requests to validate JWT
 * tokens
 * and set the authenticated user in the SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = "Bearer ".length();

    /**
     * Filter method that validates JWT token in the Authorization header
     * and sets the authenticated user in SecurityContext if token is valid
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from Authorization header
            String token = extractJwtFromRequest(request);

            // Validate token and set authentication if valid
            if (StringUtils.hasText(token)) {
                if (tokenProvider.validateAccessToken(token)) {
                    setAuthenticationFromToken(token, request);
                } else {
                    log.warn("Invalid or expired JWT token");
                }
            } else {
                log.debug("No JWT token found in request");
            }

        } catch (UsernameNotFoundException ex) {
            log.warn("User not found while processing JWT token: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context: {}", ex.getMessage(), ex);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * Expected format: "Bearer <token>"
     * 
     * @param request the HTTP request
     * @return JWT token string, or null if not found or invalid format
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null) {
            log.debug("Authorization header not found");
            return null;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Authorization header does not start with 'Bearer '");
            return null;
        }

        if (authHeader.length() <= BEARER_PREFIX_LENGTH) {
            log.warn("Authorization header has Bearer prefix but no token");
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        log.debug("JWT token extracted successfully");
        return token;
    }

    /**
     * Load user details and set authentication in SecurityContext
     * 
     * @param token   the JWT token
     * @param request the HTTP request
     * @throws UsernameNotFoundException if user not found in database
     */
    private void setAuthenticationFromToken(String token, HttpServletRequest request) throws UsernameNotFoundException {
        // Extract username from token
        String username = tokenProvider.getUsernameFromToken(token);

        if (username == null) {
            log.warn("Could not extract username from JWT token");
            throw new UsernameNotFoundException("Invalid token: username not found");
        }

        log.debug("Extracted username '{}' from JWT token", username);

        // Load user details from database
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Create authentication token with user details and authorities
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        // Set additional details from request
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Set authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Successfully authenticated user '{}' with JWT token", username);
    }

}