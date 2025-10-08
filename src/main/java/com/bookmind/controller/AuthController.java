package com.bookmind.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Get OAuth2 login URLs
     */
    @GetMapping("/login-urls")
    public ResponseEntity<?> getLoginUrls() {
        Map<String, Object> loginUrls = new HashMap<>();
        
        // These URLs will be handled by Spring Security OAuth2
        loginUrls.put("google", "/oauth2/authorize/google");
        loginUrls.put("github", "/oauth2/authorize/github");
        
        Map<String, Object> response = new HashMap<>();
        response.put("loginUrls", loginUrls);
        response.put("message", "Available OAuth2 login providers");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Landing page for authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "BookMind Authentication");
        response.put("message", "OAuth2 authentication service is running");
        response.put("availableProviders", new String[]{"google", "github"});
        
        return ResponseEntity.ok(response);
    }
}
