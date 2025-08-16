# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

BookMind is a Spring Boot 3.5.4 application built with Java 24 for managing a book catalog system with e-commerce features. It includes book management, user accounts, reviews, categories, shopping carts, wishlists, and order processing.

## Development Commands

### Build and Run
```bash
# Clean and build
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build Docker image
docker build -t bookmind .

# Run with Maven (skip tests)
./mvnw clean package -DskipTests
```

### Testing
```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=BookMindApplicationTests

# Run tests with coverage
./mvnw test jacoco:report
```

### Database Operations
```bash
# The application uses PostgreSQL with Docker Compose support
# Database connection: jdbc:postgresql://db:5432/bookmind
# Hibernate will auto-update schema on startup (spring.jpa.hibernate.ddl-auto=update)
```

### Linting and Code Quality
```bash
# Format code (if spotless is configured)
./mvnw spotless:apply

# Check code style
./mvnw spotless:check
```

## Architecture Overview

### Core Domain Models
The application follows a layered architecture with clear separation between entities:

**Book Management Core:**
- `Book` - Central entity with title, author, genre, price, availability, ratings
- `Category` - Many-to-many relationship with books via join table `book_categories`
- `Review` - User reviews linked to books with ratings (1-5 scale)
- `User` - User accounts with roles, linked to carts, wishlists, orders, and reviews

**E-commerce Features:**
- `Cart` - Shopping cart extending `UserBookCollection` with total price tracking
- `WishList` - User wishlists for saving books, also extends `UserBookCollection`
- `Order` - Order processing with `OrderItem` details and status tracking
- `OrderItem` - Individual items within orders with quantity and price snapshots

**Design Patterns:**
- `UserBookCollection` - Abstract superclass for Cart and WishList using Template Method pattern
- `BookCollection` - Interface defining contract for book collection operations
- Bidirectional JPA relationships with proper cascade handling

### Package Structure
```
com.bookmind/
├── model/           # JPA entities and domain models
├── repository/      # Data access layer with custom queries
├── service/         # Business logic (currently only BookService implemented)
├── controller/      # REST API endpoints (currently only BookController implemented)
└── config/          # Configuration classes (SecurityConfiguration)
```

### Data Relationships
- **Book ↔ Category**: Many-to-many via `book_categories` join table
- **Book ↔ Review**: One-to-many (Book has many Reviews)
- **User ↔ Review**: One-to-many (User can write many Reviews)  
- **User → Cart**: One-to-one relationship
- **User ↔ WishList**: One-to-many (User can have multiple wishlists)
- **User ↔ Order**: One-to-many (User can have multiple orders)
- **Order ↔ OrderItem**: One-to-many (Order contains multiple items)

### Key Features
- **Advanced Book Search**: Complex search with multiple criteria (title, author, genre, description, price range, rating, availability)
- **Pagination Support**: All search operations support pagination and sorting
- **Rating System**: Automatic average rating calculation when reviews are added/removed
- **Security**: Spring Security configured (currently permissive - all requests allowed)
- **Validation**: Bean validation with `spring-boot-starter-validation`
- **API Documentation**: SpringDoc OpenAPI integration for Swagger UI

### Database Configuration
- **Database**: PostgreSQL (configured for container hostname `db:5432`)
- **JPA**: Hibernate with `update` DDL mode
- **Connection Pool**: Default HikariCP
- **SQL Logging**: Enabled for development (DEBUG level for SQL, TRACE for bind parameters)

### Technology Stack
- **Framework**: Spring Boot 3.5.4 with Java 24
- **Security**: Spring Security + OAuth2 Client + JWT (JJWT 0.12.6)
- **Data**: Spring Data JPA + PostgreSQL + Redis (for sessions)
- **Build**: Maven with Lombok annotation processing
- **Containerization**: Docker support with Eclipse Temurin JDK 24

## Development Notes

### Service Layer Status
Currently only `BookService` is fully implemented with comprehensive CRUD operations and search functionality. The application appears to be missing service classes for User, Category, Review, Cart, WishList, and Order management.

### Controller Layer Status  
Only `BookController` is implemented with full REST API endpoints. Missing controllers for other entities suggest this is a work-in-progress or focused on book management features.

### Security Configuration
The current security setup disables CSRF, form login, and HTTP basic auth while allowing all requests. This appears to be a development configuration that will need hardening for production.

### Testing
Minimal test coverage - only the default `BookMindApplicationTests` exists. Consider adding unit tests for services, integration tests for repositories, and API tests for controllers.

### Database Schema Evolution
With `ddl-auto=update`, schema changes are automatically applied. For production, consider using Flyway or Liquibase for controlled database migrations.
