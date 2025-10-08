package com.bookmind.service;

import com.bookmind.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user: " + ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        log.debug("Processing OAuth2 user from provider: {}", registrationId);
        log.debug("User attributes: {}", attributes);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        
        if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> existingUser = userService.findByEmail(userInfo.getEmail());
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (!registrationId.equals(user.getProvider())) {
                throw new OAuth2AuthenticationException(
                    String.format("Looks like you're signed up with %s account. Please use your %s account to login.",
                        user.getProvider(), user.getProvider())
                );
            }
            user = userService.updateOAuth2User(user, userInfo.getFirstName(), 
                                              userInfo.getLastName(), userInfo.getPictureUrl());
        } else {
            String username = userService.generateUniqueUsername(userInfo.getEmail());
            user = userService.createOAuth2User(
                userInfo.getEmail(),
                username,
                userInfo.getFirstName(),
                userInfo.getLastName(),
                userInfo.getPictureUrl(),
                registrationId,
                userInfo.getId()
            );
        }

        return new CustomOAuth2User(oauth2User, user);
    }

    // Inner class for OAuth2 user information extraction
    public static abstract class OAuth2UserInfo {
        protected Map<String, Object> attributes;

        public OAuth2UserInfo(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public abstract String getId();
        public abstract String getFirstName();
        public abstract String getLastName();
        public abstract String getEmail();
        public abstract String getPictureUrl();
    }

    // Google OAuth2 user info
    public static class GoogleOAuth2UserInfo extends OAuth2UserInfo {
        public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
            super(attributes);
        }

        @Override
        public String getId() {
            return (String) attributes.get("sub");
        }

        @Override
        public String getFirstName() {
            return (String) attributes.get("given_name");
        }

        @Override
        public String getLastName() {
            return (String) attributes.get("family_name");
        }

        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }

        @Override
        public String getPictureUrl() {
            return (String) attributes.get("picture");
        }
    }

    // GitHub OAuth2 user info
    public static class GitHubOAuth2UserInfo extends OAuth2UserInfo {
        public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
            super(attributes);
        }

        @Override
        public String getId() {
            return String.valueOf(attributes.get("id"));
        }

        @Override
        public String getFirstName() {
            String name = (String) attributes.get("name");
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.split(" ");
                return parts[0];
            }
            return null;
        }

        @Override
        public String getLastName() {
            String name = (String) attributes.get("name");
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.split(" ");
                if (parts.length > 1) {
                    return parts[parts.length - 1];
                }
            }
            return null;
        }

        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }

        @Override
        public String getPictureUrl() {
            return (String) attributes.get("avatar_url");
        }
    }

    // Factory for creating OAuth2UserInfo instances
    public static class OAuth2UserInfoFactory {
        public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
            switch (registrationId.toLowerCase()) {
                case "google":
                    return new GoogleOAuth2UserInfo(attributes);
                case "github":
                    return new GitHubOAuth2UserInfo(attributes);
                default:
                    throw new OAuth2AuthenticationException("Sorry! Login with " + registrationId + " is not supported yet.");
            }
        }
    }
}
