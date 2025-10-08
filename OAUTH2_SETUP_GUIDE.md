# 🚀 Complete OAuth2 Setup Guide

## Why Do I Need to Create OAuth2 Applications?

Think of OAuth2 applications like registering your app with Google/GitHub so they know it's legitimate. It's like getting a business license before you can accept payments.

**Without OAuth2 App Registration:**
❌ Google doesn't trust your app
❌ Users can't login with Google
❌ Security risks

**With OAuth2 App Registration:**
✅ Google recognizes your app as legitimate
✅ Users can safely login
✅ You get access to user profile data

---

## 📱 **1. Creating Google OAuth2 Application**

### Step 1: Go to Google Cloud Console
1. Open [Google Cloud Console](https://console.cloud.google.com/)
2. Sign in with your Google account

### Step 2: Create or Select a Project
```bash
# Option A: Create new project
Click "Select a project" → "NEW PROJECT" 
- Project name: "BookMind"
- Click "CREATE"

# Option B: Use existing project
Click "Select a project" → Choose existing project
```

### Step 3: Enable Google+ API
```bash
1. In the sidebar: APIs & Services → Library
2. Search for "Google+ API" or "Google Sign-In API"
3. Click on it → Click "ENABLE"
```

### Step 4: Create OAuth2 Credentials
```bash
1. APIs & Services → Credentials
2. Click "CREATE CREDENTIALS" → "OAuth 2.0 Client ID"
3. If prompted, configure OAuth consent screen first:
   - User Type: External
   - App name: BookMind
   - User support email: your-email@gmail.com
   - Developer contact: your-email@gmail.com
   - Click "SAVE AND CONTINUE" through all steps
```

### Step 5: Configure OAuth2 Client
```bash
Application type: Web application
Name: BookMind Web Client

Authorized JavaScript origins:
- http://localhost:8080
- http://localhost:3000  (if you have React frontend)

Authorized redirect URIs:
- http://localhost:8080/login/oauth2/code/google

Click "CREATE"
```

### Step 6: Copy Credentials
```bash
Google will show you:
- Client ID: 1234567890-abcdef.apps.googleusercontent.com
- Client Secret: GOCSPX-1234567890abcdef

⚠️ IMPORTANT: Copy these and keep them secure!
```

### Step 7: Set Environment Variables
```bash
# In your terminal:
export GOOGLE_CLIENT_ID="1234567890-abcdef.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="GOCSPX-1234567890abcdef"

# Or add to .bashrc/.zshrc for permanent setup:
echo 'export GOOGLE_CLIENT_ID="your-client-id"' >> ~/.bashrc
echo 'export GOOGLE_CLIENT_SECRET="your-client-secret"' >> ~/.bashrc
source ~/.bashrc
```

---

## 🐙 **2. Creating GitHub OAuth2 Application**

### Step 1: Go to GitHub Settings
1. Open [GitHub](https://github.com/)
2. Sign in to your account
3. Click your profile picture → Settings

### Step 2: Navigate to Developer Settings
```bash
In left sidebar:
Scroll down → Developer settings → OAuth Apps
```

### Step 3: Create New OAuth App
```bash
Click "New OAuth App"

Fill in the form:
- Application name: BookMind
- Homepage URL: http://localhost:8080
- Application description: Book catalog management system
- Authorization callback URL: http://localhost:8080/login/oauth2/code/github

Click "Register application"
```

### Step 4: Get Credentials
```bash
GitHub will show you:
- Client ID: 1234567890abcdef
- Click "Generate a new client secret"
- Client Secret: ghp_1234567890abcdefghijk

⚠️ IMPORTANT: Copy the client secret immediately - you can't see it again!
```

### Step 5: Set Environment Variables
```bash
export GITHUB_CLIENT_ID="1234567890abcdef"
export GITHUB_CLIENT_SECRET="ghp_1234567890abcdefghijk"

# Add to .bashrc/.zshrc:
echo 'export GITHUB_CLIENT_ID="your-client-id"' >> ~/.bashrc
echo 'export GITHUB_CLIENT_SECRET="your-client-secret"' >> ~/.bashrc
source ~/.bashrc
```

---

## 🔑 **3. Creating JWT Secret**

### Why Do We Need a JWT Secret?
The JWT secret is like a master key that signs all your JWT tokens. Without it, anyone could create fake tokens and impersonate users!

### Generate a Secure JWT Secret
```bash
# Option 1: Using openssl (recommended)
openssl rand -hex 32
# Output: a1b2c3d4e5f6789012345678901234567890123456789012345678901234

# Option 2: Using online generator
# Go to: https://generate-random.org/encryption-key-generator
# Select 256-bit key

# Option 3: Manual (not recommended for production)
JWT_SECRET="my-super-secret-key-that-should-be-very-long-and-random-for-security"
```

### Set JWT Secret Environment Variable
```bash
export JWT_SECRET="a1b2c3d4e5f6789012345678901234567890123456789012345678901234"

# Add to .bashrc/.zshrc:
echo 'export JWT_SECRET="your-generated-secret"' >> ~/.bashrc
source ~/.bashrc
```

---

## 🧪 **4. Testing Your Setup**

### Check Environment Variables
```bash
# Verify all variables are set:
echo "Google Client ID: $GOOGLE_CLIENT_ID"
echo "Google Client Secret: $GOOGLE_CLIENT_SECRET"
echo "GitHub Client ID: $GITHUB_CLIENT_ID"
echo "GitHub Client Secret: $GITHUB_CLIENT_SECRET"
echo "JWT Secret: $JWT_SECRET"

# All should show values (not empty)
```

### Test OAuth2 Applications

#### Test Google OAuth2:
1. Start your BookMind application
2. Go to: `http://localhost:8080/oauth2/authorize/google`
3. Should redirect to Google login page
4. Login and authorize BookMind
5. Should redirect back to your app

#### Test GitHub OAuth2:
1. Go to: `http://localhost:8080/oauth2/authorize/github`
2. Should redirect to GitHub login page
3. Login and authorize BookMind
4. Should redirect back to your app

---

## 🛠️ **5. Troubleshooting Common Issues**

### Issue 1: "Error 400: redirect_uri_mismatch"
```bash
❌ Problem: Redirect URI in Google/GitHub doesn't match your app

✅ Solution:
- Check Google Console: Authorized redirect URIs must include:
  http://localhost:8080/login/oauth2/code/google
- Check GitHub: Authorization callback URL must be:
  http://localhost:8080/login/oauth2/code/github
```

### Issue 2: "Invalid client_id"
```bash
❌ Problem: Wrong client ID or client secret

✅ Solution:
- Double-check environment variables: echo $GOOGLE_CLIENT_ID
- Make sure no extra spaces or quotes
- Copy-paste directly from Google/GitHub console
```

### Issue 3: "OAuth consent screen needs verification"
```bash
❌ Problem: Google requires app verification for production

✅ Solution for Development:
- In Google Console → OAuth consent screen
- Add test users (your email addresses)
- Use "Internal" app type if you have G Suite
```

### Issue 4: JWT Token Issues
```bash
❌ Problem: JWT secret too short or not random

✅ Solution:
- Use at least 256-bit (32 characters) secret
- Generate with: openssl rand -hex 32
- Never share or commit the secret to Git
```

---

## 🔒 **6. Security Best Practices**

### Environment Variables Security
```bash
# ✅ Good: Use environment variables
export GOOGLE_CLIENT_ID="your-id"

# ❌ Bad: Hard-code in application.yml
google:
  client-id: "1234567890-abcdef.apps.googleusercontent.com"
```

### Production Considerations
```bash
# Development URLs:
- http://localhost:8080
- http://localhost:3000

# Production URLs (update in Google/GitHub):
- https://your-domain.com
- https://api.your-domain.com
```

### Secret Management
```bash
# Development: Environment variables
export JWT_SECRET="your-secret"

# Production: Use secret management services
# - AWS Secrets Manager
# - HashiCorp Vault
# - Azure Key Vault
# - Google Secret Manager
```

---

## 📋 **7. Complete Setup Checklist**

### Google OAuth2 ✅
- [ ] Created Google Cloud Project
- [ ] Enabled Google+ API
- [ ] Created OAuth2 credentials
- [ ] Added correct redirect URI
- [ ] Copied Client ID and Secret
- [ ] Set environment variables
- [ ] Tested login flow

### GitHub OAuth2 ✅
- [ ] Created GitHub OAuth App
- [ ] Added correct callback URL
- [ ] Generated client secret
- [ ] Set environment variables
- [ ] Tested login flow

### JWT Configuration ✅
- [ ] Generated secure 256-bit secret
- [ ] Set JWT_SECRET environment variable
- [ ] Configured expiration time
- [ ] Never committed secret to Git

### Application Testing ✅
- [ ] Environment variables are set
- [ ] Application compiles successfully
- [ ] Can access test page at localhost:8080
- [ ] Google login works
- [ ] GitHub login works
- [ ] JWT tokens are generated
- [ ] API endpoints work with tokens

---

## 🎯 **What Happens Next?**

Once you have everything set up:

1. **User clicks "Login with Google"**
   ```
   Browser → Google → User enters password → Google asks permission → 
   User says "Yes" → Google redirects to your app with code →
   Your app exchanges code for user info → User is logged in! 🎉
   ```

2. **Your app creates the user**
   ```
   Google gives you:
   - Name: "John Doe"
   - Email: "john@gmail.com" 
   - Picture: "https://photo.jpg"
   
   Your app saves this to database and logs user in
   ```

3. **User gets JWT token for API calls**
   ```
   Frontend can now make API calls:
   Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
   ```

You're ready to build a modern, secure authentication system! 🚀
