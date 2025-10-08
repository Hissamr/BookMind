package com.bookmind.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationException exception) throws IOException {
        
        log.error("OAuth2 authentication failed", exception);
        
        String targetUrl = determineTargetUrl(request, response, exception);
        response.sendRedirect(targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) {
        
        // You can customize this based on your frontend URL structure
        String targetUrl = "http://localhost:3000/auth/oauth2/redirect";
        
        // Add error information as query parameters
        String encodedError = URLEncoder.encode("OAuth2 Authentication Failed", StandardCharsets.UTF_8);
        String encodedMessage = URLEncoder.encode(exception.getLocalizedMessage(), StandardCharsets.UTF_8);
        
        targetUrl = targetUrl + "?error=" + encodedError + "&message=" + encodedMessage;
        
        log.debug("Redirecting to: {}", targetUrl);
        return targetUrl;
    }
}
