package com.bookmind.config;

import com.bookmind.service.CustomOAuth2User;
import com.bookmind.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        
        log.info("OAuth2 authentication successful for user: {}", authentication.getName());
        
        try {
            // Generate JWT token
            String token = jwtUtil.generateTokenFromAuthentication(authentication);
            
            // Get user details
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            
            // For web applications, you might want to redirect to a frontend URL with the token
            // For API-only applications, you might return JSON response
            
            // Option 1: Redirect to frontend with token as query parameter (for web apps)
            String targetUrl = determineTargetUrl(request, response, authentication, token);
            response.sendRedirect(targetUrl);
            
            // Option 2: Return JSON response (for API apps) - uncomment if needed
            /*
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", Map.of(
                "id", oAuth2User.getId(),
                "email", oAuth2User.getEmail(),
                "username", oAuth2User.getUsername(),
                "firstName", oAuth2User.getFirstName(),
                "lastName", oAuth2User.getLastName(),
                "pictureUrl", oAuth2User.getPictureUrl(),
                "provider", oAuth2User.getProvider()
            ));
            response.getWriter().write(mapper.writeValueAsString(result));
            */
            
        } catch (Exception ex) {
            log.error("Error handling OAuth2 authentication success", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication processing error");
        }
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication, String token) {
        
        // You can customize this based on your frontend URL structure
        String targetUrl = "http://localhost:3000/auth/oauth2/redirect"; // Example React frontend URL
        
        // Add token as query parameter
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        targetUrl = targetUrl + "?token=" + encodedToken;
        
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
        targetUrl += "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
        
        log.debug("Redirecting to: {}", targetUrl);
        return targetUrl;
    }
}
