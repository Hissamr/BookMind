# OAuth2 Authentication Setup for BookMind

## Overview

BookMind now supports OAuth2 authentication with Google and GitHub providers, alongside JWT token-based API authentication.

## Features Added

- ✅ OAuth2 login with Google and GitHub
- ✅ JWT token generation and validation
- ✅ User management with OAuth2 profile integration
- ✅ Session-based and stateless authentication
- ✅ RESTful authentication APIs
- ✅ Web-based testing interface

## Quick Setup

### 1. OAuth2 Provider Configuration

#### Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
5. Set application type to "Web application"
6. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
7. Copy Client ID and Client Secret

#### GitHub OAuth2 Setup

1. Go to [GitHub Settings](https://github.com/settings/developers)
2. Click "New OAuth App"
3. Fill in:
   - Application name: `BookMind`
   - Homepage URL: `http://localhost:8080`
   - Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Copy Client ID and Client Secret

### 2. Environment Variables

Create a `.env` file or set environment variables:

```bash
# Google OAuth2
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"

# GitHub OAuth2  
export GITHUB_CLIENT_ID="your-github-client-id"
export GITHUB_CLIENT_SECRET="your-github-client-secret"

# JWT Secret (generate a secure random string)
export JWT_SECRET="your-super-secret-jwt-key-that-should-be-at-least-256-bits-long"
```

### 3. Database Setup

The application will automatically create the necessary OAuth2 fields in the User table:
- `provider` - OAuth2 provider (google, github)
- `provider_id` - Provider's user ID
- `first_name`, `last_name` - User's name from OAuth2 provider
- `picture_url` - Profile picture URL
- `email_verified` - Email verification status

### 4. Start the Application

```bash
# With Docker Compose (includes PostgreSQL and Redis)
docker-compose up -d db redis

# Run the Spring Boot application
./mvnw spring-boot:run

# Or with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Testing OAuth2 Integration

### 1. Web Interface Testing

1. Open browser to `http://localhost:8080`
2. Click on "Login with Google" or "Login with GitHub"
3. Complete OAuth2 flow
4. Test API endpoints using the built-in interface

### 2. API Endpoints

#### Authentication Endpoints
```
GET  /auth/status              - Service status
GET  /auth/login-urls         - OAuth2 login URLs
GET  /oauth2/authorize/google  - Initiate Google login
GET  /oauth2/authorize/github  - Initiate GitHub login
```

#### User Endpoints (require authentication)
```
GET  /api/user/me             - Current user profile
GET  /api/user/auth-status    - Authentication status  
POST /api/user/token          - Generate JWT token
POST /api/user/logout         - Logout
PUT  /api/user/profile        - Update profile
```

### 3. JWT Authentication Testing

1. Login via OAuth2
2. Call `/api/user/token` to get JWT token
3. Use token in Authorization header: `Bearer <token>`
4. Test API endpoints with JWT authentication

### 4. Manual API Testing

```bash
# Check service status
curl http://localhost:8080/auth/status

# Check authentication status  
curl -b cookies.txt http://localhost:8080/api/user/auth-status

# Get user profile (after OAuth2 login)
curl -b cookies.txt http://localhost:8080/api/user/me

# Generate JWT token
curl -X POST -b cookies.txt http://localhost:8080/api/user/token

# Use JWT token for API calls
curl -H "Authorization: Bearer <your-jwt-token>" http://localhost:8080/api/user/me
```

## Architecture

### Authentication Flow

1. **OAuth2 Web Login**: Users click login button → OAuth2 provider → callback → session created
2. **JWT Token Generation**: Authenticated users can generate JWT tokens for API access
3. **API Authentication**: Support both session-based and JWT token-based authentication

### Key Components

- `CustomOAuth2UserService`: Handles OAuth2 user registration and updates
- `JwtUtil`: JWT token generation and validation
- `JwtAuthenticationFilter`: JWT token authentication filter
- `UserService`: User management and UserDetails integration
- `SecurityConfiguration`: Spring Security OAuth2 and JWT configuration

### Security Configuration

- **Public endpoints**: `/`, `/login`, `/oauth2/**`, `/auth/**`, `/api/books/**`
- **Authenticated endpoints**: `/api/user/**`, `/api/cart/**`, `/api/wishlist/**`, `/api/orders/**`
- **Admin endpoints**: `/api/admin/**`

## Troubleshooting

### Common Issues

1. **OAuth2 redirect URI mismatch**: Ensure redirect URIs in provider settings match exactly
2. **JWT secret too short**: Use at least 256-bit (32 character) JWT secret
3. **CORS issues**: Check CORS configuration in SecurityConfiguration
4. **Session issues**: Ensure Redis is running for session storage

### Debug Logging

Add to `application.yml`:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.bookmind: DEBUG
    org.springframework.security.oauth2: DEBUG
```

### Database Schema

The User table schema has been updated with these new fields:
```sql
ALTER TABLE users 
ADD COLUMN provider VARCHAR(50),
ADD COLUMN provider_id VARCHAR(255),
ADD COLUMN first_name VARCHAR(100),
ADD COLUMN last_name VARCHAR(100),  
ADD COLUMN picture_url VARCHAR(500);

-- Make password nullable for OAuth2 users
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
```

## Production Considerations

1. **Environment Variables**: Use secure environment variable management
2. **HTTPS**: Enable HTTPS in production
3. **JWT Secret**: Use a cryptographically secure random JWT secret
4. **Session Storage**: Configure Redis with persistence for production
5. **CORS**: Restrict CORS to your frontend domains
6. **Rate Limiting**: Implement rate limiting for authentication endpoints

## Next Steps

- Add support for more OAuth2 providers (Facebook, Twitter, etc.)
- Implement email verification for non-OAuth2 users  
- Add password reset functionality
- Implement user role management
- Add audit logging for authentication events
- Set up refresh token support for JWT tokens
