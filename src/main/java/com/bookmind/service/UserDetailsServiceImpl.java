package com.bookmind.service;

import com.bookmind.model.User;
import com.bookmind.repository.UserRepository;
import com.bookmind.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username for Spring Security authentication
     * 
     * @param username the username to search for
     * @return UserDetails with user information and authorities
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        // Convert user roles to Spring Security authorities
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // If user has roles, map them to authorities
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> {
                // Add "ROLE_" prefix if not already present
                String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities.add(new SimpleGrantedAuthority(roleName));
                log.debug("Added authority: {} for user: {}", roleName, username);
            });
        } else {
            // Default role if user has no roles assigned
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            log.debug("User {} has no roles, assigned default ROLE_USER", username);
        }

        log.info("Successfully loaded user: {} with {} authorities", username, authorities.size());

        // Return CustomUserDetails which includes userId for secure access
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),       // enabled
                true,                    // accountNonExpired
                true,                    // credentialsNonExpired
                true,                    // accountNonLocked
                authorities
        );
    }

    /**
     * Load user by email (additional method for flexibility)
     * 
     * @param email the email to search for
     * @return UserDetails with user information and authorities
     * @throws UsernameNotFoundException if user not found
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        log.debug("Attempting to load user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        return loadUserDetailsFromUser(user);
    }

    /**
     * Helper method to convert User entity to UserDetails
     * 
     * @param user the User entity
     * @return UserDetails object
     */
    private UserDetails loadUserDetailsFromUser(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> {
                String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities.add(new SimpleGrantedAuthority(roleName));
            });
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                authorities
        );
    }
}
