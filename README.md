# 📚 BookMind

A modern book e-commerce REST API built with Spring Boot, featuring AI-powered book summaries and personalized recommendations.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green)
![License](https://img.shields.io/badge/License-MIT-blue)

## ✨ Features

### Core E-Commerce
- 📖 **Book Catalog** - Browse, search, and filter books by title, author, genre, price, and rating
- 🛒 **Shopping Cart** - Add, update, remove items with automatic total calculation
- 📋 **Order Management** - Checkout, order history, and order status tracking
- ❤️ **Wishlists** - Create multiple wishlists, add/remove books, bulk operations
- 👤 **User Authentication** - JWT-based auth with refresh tokens

### AI-Powered (Coming Soon)
- 🤖 **AI Book Summaries** - OpenAI-generated summaries for each book
- 🎯 **Smart Recommendations** - Personalized book suggestions based on user interests

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 3.5.5 |
| **Language** | Java 21 |
| **Database** | PostgreSQL |
| **Caching** | Redis |
| **Security** | Spring Security + JWT |
| **Build** | Maven |
| **Containerization** | Docker & Docker Compose |

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven (or use included wrapper)

### Quick Start with Docker

```bash
# Clone the repository
git clone https://github.com/yourusername/BookMind.git
cd BookMind

# Start all services (PostgreSQL, Redis, App)
docker-compose up -d

# The API will be available at http://localhost:8080
```

### Local Development

```bash
# Start database services only
docker-compose up -d postgres redis

# Run the application
./mvnw spring-boot:run
```

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login & get tokens | Public |
| POST | `/api/auth/refresh` | Refresh access token | Public |
| GET | `/api/auth/me` | Get current user | Authenticated |
| POST | `/api/auth/logout` | Logout user | Authenticated |

### Books
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/books` | Get all books | Public |
| GET | `/api/v1/books/{id}` | Get book by ID | Public |
| GET | `/api/v1/books/search` | Search books | Public |
| GET | `/api/v1/books/advanced-search` | Advanced search with filters | Public |
| POST | `/api/v1/books` | Add new book | Admin |
| PUT | `/api/v1/books/{id}` | Update book | Admin |
| DELETE | `/api/v1/books/{id}` | Delete book | Admin |

### Cart
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/cart` | Get user's cart | Authenticated |
| POST | `/api/v1/cart/items` | Add item to cart | Authenticated |
| PUT | `/api/v1/cart/items` | Update item quantity | Authenticated |
| DELETE | `/api/v1/cart/items` | Remove item from cart | Authenticated |
| DELETE | `/api/v1/cart` | Clear cart | Authenticated |
| POST | `/api/v1/cart/checkout` | Checkout cart | Authenticated |

### Orders
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/orders` | Get user's orders | Authenticated |
| GET | `/api/v1/orders/{id}` | Get order details | Authenticated |
| PUT | `/api/v1/orders/{id}/cancel` | Cancel order | Authenticated |
| GET | `/api/v1/orders/admin/all` | Get all orders | Admin |
| PUT | `/api/v1/orders/admin/status` | Update order status | Admin |

### Wishlists
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/wishlists` | Get user's wishlists | Authenticated |
| POST | `/api/v1/wishlists` | Create wishlist | Authenticated |
| PUT | `/api/v1/wishlists/{id}` | Update wishlist | Authenticated |
| DELETE | `/api/v1/wishlists/{id}` | Delete wishlist | Authenticated |
| POST | `/api/v1/wishlists/{id}/books/{bookId}` | Add book to wishlist | Authenticated |
| DELETE | `/api/v1/wishlists/{id}/books/{bookId}` | Remove book | Authenticated |

## 🔒 Security

- **JWT Authentication** - Stateless authentication with access & refresh tokens
- **Role-Based Access Control** - USER and ADMIN roles
- **Secure User Data** - User ID extracted from JWT token, never from request body/URL
- **Password Encryption** - BCrypt hashing

## 🏗️ Project Structure

```
src/main/java/com/bookmind/
├── config/          # Security & app configuration
├── controller/      # REST API endpoints
├── dto/             # Data Transfer Objects
├── exception/       # Custom exceptions & global handler
├── mapper/          # Entity-DTO mappers
├── model/           # JPA entities
├── repository/      # Data access layer
├── security/        # JWT & authentication
├── service/         # Business logic
└── utility/         # Helper classes
```

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## 📝 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/bookmind` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| `JWT_SECRET` | JWT signing key | - |
| `JWT_EXPIRATION` | Access token expiry (ms) | `3600000` |

## 🗺️ Roadmap

- [x] Core book catalog & CRUD
- [x] User authentication with JWT
- [x] Shopping cart functionality
- [x] Order management
- [x] Wishlist feature
- [ ] OpenAI integration for book summaries
- [ ] AI-powered recommendations
- [ ] User reviews & ratings
- [ ] Payment gateway integration
- [ ] Email notifications

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 👨‍💻 Author

** Hissam **
- GitHub: [@Hissamr](https://github.com/Hissamr)

---

⭐ Star this repo if you find it helpful!