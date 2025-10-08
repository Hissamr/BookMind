# 🛡️ Spring Security Components Explained

## SecurityConfiguration.java - The Bouncer

This class is like the main bouncer at a club who decides:
- Who can enter (authentication)
- Which areas they can access (authorization)
- How they should enter (OAuth2, JWT, etc.)

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    
    // These are the helpers our bouncer uses
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
            // 1. CORS - Allow requests from different websites (like React frontend)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. CSRF - Disable for APIs (we're using JWT instead)
            .csrf(csrf -> csrf.disable())
            
            // 3. Session - Create sessions when needed (for OAuth2)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            
            // 4. AUTHORIZATION RULES - Who can access what?
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - anyone can access
                .requestMatchers("/", "/login", "/oauth2/**", "/auth/**", 
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                                "/api/books/**").permitAll()
                // Protected endpoints - must be logged in
                .requestMatchers("/api/user/**", "/api/cart/**", "/api/wishlist/**", "/api/orders/**").authenticated()
                // Admin endpoints - must be admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            
            // 5. OAUTH2 LOGIN SETUP
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> 
                        authorization.baseUri("/oauth2/authorize"))
                .redirectionEndpoint(redirection -> 
                        redirection.baseUri("/login/oauth2/code/*"))
                .userInfoEndpoint(userInfo -> 
                        userInfo.userService(customOAuth2UserService))
                .successHandler(oauth2AuthenticationSuccessHandler(jwtUtil))
                .failureHandler(oauth2AuthenticationFailureHandler())
            )
            
            // 6. DISABLE FORM LOGIN - We're using OAuth2 instead
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // 7. LOGOUT CONFIGURATION
            .logout(logout -> logout
                    .logoutUrl("/auth/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
            )
            
            // 8. ADD JWT FILTER - Check JWT tokens on each request
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Why Each Part is Important:

#### 1. **CORS Configuration**
```java
// Without CORS: Browser blocks requests from React (localhost:3000) to Spring Boot (localhost:8080)
// With CORS: React can talk to Spring Boot freely
```

#### 2. **Authorization Rules**
```java
// Think of it like security checkpoints:
// Public area: Anyone can enter (/, /login, /api/books)
// Member area: Must show membership card (/api/user/*)
// VIP area: Must be admin (/api/admin/*)
```

#### 3. **OAuth2 Configuration**
```java
// Like setting up a partnership with Google:
// "Hey Google, when users want to login to BookMind, send them to you,
//  then send them back to us with their info"
```

---

## UserService.java - The User Manager

This class handles everything about users:

```java
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // This method is called by Spring Security to load user details
    @Override
    public UserDetails loadUserByUsername(String username) {
        // Find user in database
        User user = userRepository.findByEmailOrUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert our User object to Spring Security's UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "") 
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList()))
                .accountExpired(false)
                .accountLocked(!user.isEnabled())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }

    // Create OAuth2 user (from Google/GitHub login)
    public User createOAuth2User(String email, String username, String firstName, 
                                String lastName, String pictureUrl, String provider, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPictureUrl(pictureUrl);
        user.setProvider(provider);          // "google" or "github"
        user.setProviderId(providerId);      // Google's ID for this user
        user.setRoles(Set.of("USER"));       // Default role
        user.setEnabled(true);
        user.setEmailVerified(true);         // OAuth2 providers verify emails
        
        return userRepository.save(user);
    }
}
```

### Why UserService is Important:
1. **Bridge**: Connects Spring Security with your database
2. **User Creation**: Handles OAuth2 user registration automatically
3. **Role Management**: Assigns roles and permissions to users

---

## CustomOAuth2UserService.java - The OAuth2 Handler

This class processes users coming from Google/GitHub:

```java
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // 1. Get user info from OAuth2 provider (Google/GitHub)
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // 2. Extract provider name
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // 3. Convert provider-specific data to our format
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        
        // 4. Check if user already exists
        Optional<User> existingUser = userService.findByEmail(userInfo.getEmail());
        User user;
        
        if (existingUser.isPresent()) {
            // Update existing user
            user = existingUser.get();
            user = userService.updateOAuth2User(user, userInfo.getFirstName(), 
                                              userInfo.getLastName(), userInfo.getPictureUrl());
        } else {
            // Create new user
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
}
```

### Different OAuth2 Providers Give Different Data:

#### Google User Info:
```json
{
  "sub": "1234567890",
  "given_name": "John",
  "family_name": "Doe", 
  "email": "john@gmail.com",
  "picture": "https://photo.jpg"
}
```

#### GitHub User Info:
```json
{
  "id": 1234567,
  "login": "johndoe",
  "name": "John Doe",
  "email": "john@users.noreply.github.com",
  "avatar_url": "https://avatar.jpg"
}
```

Our `OAuth2UserInfoFactory` handles these differences!

---

## JwtUtil.java - The Token Manager

This class creates and validates JWT tokens:

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;  // Secret key for signing tokens

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;  // How long tokens last (24 hours)

    // Create a new JWT token
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)           // Who this token is for
                .setIssuedAt(now)              // When it was created
                .setExpiration(expiryDate)     // When it expires
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)  // Sign it
                .compact();
    }

    // Check if a token is valid
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            // Token is invalid (expired, tampered with, etc.)
            return false;
        }
    }

    // Get username from token
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}
```

### JWT Token Example:
```
User logs in → Server creates JWT:
{
  "sub": "john@example.com",
  "iat": 1670000000,
  "exp": 1670086400
}

User makes API request:
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

Server validates token and allows access
```

---

## JwtAuthenticationFilter.java - The Token Checker

This filter runs on every request to check JWT tokens:

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) {
        
        // 1. Extract JWT token from request
        String jwt = getJwtFromRequest(request);
        
        // 2. If token exists and is valid
        if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
            
            // 3. Get username from token
            String username = jwtUtil.getUsernameFromToken(jwt);
            
            // 4. Load user details from database
            UserDetails userDetails = userService.loadUserByUsername(username);
            
            // 5. Create authentication object
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // 6. Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        // 7. Continue with the request
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Check for "Bearer TOKEN_HERE" format
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### How JWT Filter Works:
```
1. Request comes in: GET /api/user/me
2. Filter extracts token: "Bearer eyJ0eXAiOiJKV1..."
3. Validates token: ✅ Valid, not expired
4. Gets username: "john@example.com"
5. Loads user from database: John Doe, ROLE_USER
6. Sets authentication: User is now "logged in" for this request
7. Request continues to controller
```

---

## Why We Need All These Components?

### 1. **Multiple Authentication Methods**
```java
// OAuth2 Flow: User → Google → Our App (creates session)
// JWT Flow: Frontend → API with token → Response
```

### 2. **Stateless vs Stateful**
```java
// Session (Stateful): Server remembers you're logged in
// JWT (Stateless): Token proves you're logged in
```

### 3. **Security Layers**
```java
// Filter 1: Check JWT tokens
// Filter 2: Check OAuth2 sessions  
// Filter 3: Check permissions
// Filter 4: Allow request through
```
