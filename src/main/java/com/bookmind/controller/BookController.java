package com.bookmind.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookmind.model.Book;
import com.bookmind.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Book operations.
 * 
 * PUBLIC: Read operations (GET) are accessible to all users.
 * ADMIN ONLY: Write operations (POST, PUT, PATCH, DELETE) require ROLE_ADMIN.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class BookController {

    private final BookService bookService;

    /**
     * Get all books (public)
     */
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        log.debug("Fetching all books");
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    /**
     * Get a book by ID (public)
     */
    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        log.debug("Fetching book with ID: {}", id);
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * Add a new book (Admin only)
     */
    @PostMapping("/books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> addBook(@RequestBody @Valid Book book) {
        log.info("Admin adding new book: {}", book.getTitle());
        Book savedBook = bookService.addBook(book);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    /**
     * Update a book (Admin only)
     */
    @PutMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody @Valid Book book) {
        log.info("Admin updating book with ID: {}", id);
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Delete a book (Admin only)
     */
    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("Admin deleting book with ID: {}", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Add a category to a book (Admin only)
     */
    @PostMapping("/books/{bookId}/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addCategoryToBook(@PathVariable Long bookId, @PathVariable Long categoryId) {
        log.info("Admin adding category {} to book {}", categoryId, bookId);
        bookService.addCategoryToBook(bookId, categoryId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove a category from a book (Admin only)
     */
    @DeleteMapping("/books/{bookId}/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeCategoryFromBook(@PathVariable Long bookId, @PathVariable Long categoryId) {
        log.info("Admin removing category {} from book {}", categoryId, bookId);
        bookService.removeCategoryFromBook(bookId, categoryId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Add a review to a book (Admin only - reviews should be added via ReviewController)
     */
    @PostMapping("/books/{bookId}/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addReviewToBook(@PathVariable Long bookId, @PathVariable Long reviewId) {
        log.info("Admin adding review {} to book {}", reviewId, bookId);
        bookService.addReviewToBook(bookId, reviewId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove a review from a book (Admin only)
     */
    @DeleteMapping("/books/{bookId}/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeReviewFromBook(@PathVariable Long bookId, @PathVariable Long reviewId) {
        log.info("Admin removing review {} from book {}", reviewId, bookId);
        bookService.removeReviewFromBook(bookId, reviewId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get categories for a specific book (public)
     */
    @GetMapping("/books/{id}/categories")
    public ResponseEntity<?> getBookCategories(@PathVariable Long id) {
        log.debug("Fetching categories for book {}", id);
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book.getCategories());
    }
    
    /**
     * Get reviews for a specific book (public)
     */
    @GetMapping("/books/{id}/reviews")
    public ResponseEntity<?> getBookReviews(@PathVariable Long id) {
        log.debug("Fetching reviews for book {}", id);
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book.getReviews());
    }
    
    /**
     * Bulk add categories to a book (Admin only)
     */
    @PostMapping("/books/{bookId}/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> addCategoriesToBook(@PathVariable Long bookId, @RequestBody List<Long> categoryIds) {
        log.info("Admin bulk adding {} categories to book {}", categoryIds.size(), bookId);
        for (Long categoryId : categoryIds) {
            bookService.addCategoryToBook(bookId, categoryId);
        }
        return ResponseEntity.ok(Map.of("message", "Categories added successfully"));
    }
    
    /**
     * Remove all categories from a book (Admin only)
     */
    @DeleteMapping("/books/{bookId}/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> removeAllCategoriesFromBook(@PathVariable Long bookId) {
        log.info("Admin removing all categories from book {}", bookId);
        Book book = bookService.getBookById(bookId);
        var categories = new ArrayList<>(book.getCategories());
        for (var category : categories) {
            bookService.removeCategoryFromBook(bookId, category.getId());
        }
        return ResponseEntity.ok(Map.of("message", "All categories removed successfully"));
    }
    
    /**
     * Search books by title, author, or genre (public)
     */
    @GetMapping("/books/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre) {
        log.debug("Searching books - title: {}, author: {}, genre: {}", title, author, genre);
        List<Book> books = bookService.searchBooks(title, author, genre);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Advanced search with multiple filters (public)
     */
    @GetMapping("/books/advanced-search")
    public ResponseEntity<List<Book>> advancedSearchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean available) {
        log.debug("Advanced search - title: {}, author: {}, genre: {}, minPrice: {}, maxPrice: {}",
                title, author, genre, minPrice, maxPrice);
        List<Book> books = bookService.advancedSearchBooks(
                title, author, genre, description, minPrice, maxPrice, minRating, available);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get books by category (public)
     */
    @GetMapping("/categories/{categoryId}/books")
    public ResponseEntity<List<Book>> getBooksByCategory(@PathVariable Long categoryId) {
        log.debug("Fetching books for category {}", categoryId);
        List<Book> books = bookService.getBooksByCategory(categoryId);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get all available books (public)
     */
    @GetMapping("/books/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        log.debug("Fetching available books");
        List<Book> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get top-rated books (public)
     */
    @GetMapping("/books/top-rated")
    public ResponseEntity<List<Book>> getTopRatedBooks(
            @RequestParam(required = false, defaultValue = "4.0") Double minRating) {
        log.debug("Fetching top-rated books with min rating: {}", minRating);
        List<Book> books = bookService.getTopRatedBooks(minRating);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get books within price range (public)
     */
    @GetMapping("/books/price-range")
    public ResponseEntity<List<Book>> getBooksByPriceRange(@RequestParam Double maxPrice) {
        log.debug("Fetching books with max price: {}", maxPrice);
        List<Book> books = bookService.getBooksByPriceRange(maxPrice);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Search books with pagination (public)
     */
    @GetMapping("/books/search/paged")
    public ResponseEntity<Map<String, Object>> searchBooksWithPagination(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.debug("Paged search - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        Page<Book> books = bookService.searchBooksWithPagination(
                title, author, genre, description, minPrice, maxPrice, minRating, available,
                page, size, sortBy, direction);

        Map<String, Object> response = new HashMap<>();
        response.put("books", books.getContent());
        response.put("currentPage", books.getNumber());
        response.put("totalItems", books.getTotalElements());
        response.put("totalPages", books.getTotalPages());

        return ResponseEntity.ok(response);
    }
    
    // ==================== ADDITIONAL ENDPOINTS ====================

    /**
     * Get all books with pagination (public)
     */
    @GetMapping("/books/paged")
    public ResponseEntity<Map<String, Object>> getAllBooksWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.debug("Fetching all books paged - page: {}, size: {}", page, size);
        Page<Book> books = bookService.searchBooksWithPagination(
                null, null, null, null, null, null, null, null,
                page, size, sortBy, direction);

        Map<String, Object> response = new HashMap<>();
        response.put("books", books.getContent());
        response.put("currentPage", books.getNumber());
        response.put("totalItems", books.getTotalElements());
        response.put("totalPages", books.getTotalPages());
        response.put("hasNext", books.hasNext());
        response.put("hasPrevious", books.hasPrevious());

        return ResponseEntity.ok(response);
    }
    
    /**
     * Get books by author (public)
     */
    @GetMapping("/books/author")
    public ResponseEntity<List<Book>> getBooksByAuthor(@RequestParam String author) {
        log.debug("Fetching books by author: {}", author);
        List<Book> books = bookService.searchBooks(null, author, null);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get books by genre (public)
     */
    @GetMapping("/books/genre")
    public ResponseEntity<List<Book>> getBooksByGenre(@RequestParam String genre) {
        log.debug("Fetching books by genre: {}", genre);
        List<Book> books = bookService.searchBooks(null, null, genre);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get book statistics (public)
     */
    @GetMapping("/books/stats")
    public ResponseEntity<Map<String, Object>> getBookStatistics() {
        log.debug("Calculating book statistics");
        List<Book> allBooks = bookService.getAllBooks();
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalBooks", allBooks.size());
        stats.put("availableBooks", allBooks.stream().filter(Book::getAvailable).count());
        stats.put("averagePrice", allBooks.stream().mapToDouble(Book::getPrice).average().orElse(0.0));
        stats.put("averageRating", allBooks.stream().mapToDouble(Book::getAverageRating).average().orElse(0.0));

        Map<String, Long> genreCount = new HashMap<>();
        allBooks.forEach(book -> {
            String genre = book.getGenre();
            if (genre != null && !genre.trim().isEmpty()) {
                genreCount.put(genre, genreCount.getOrDefault(genre, 0L) + 1);
            }
        });
        stats.put("genreDistribution", genreCount);

        return ResponseEntity.ok(stats);
    }
    
    /**
     * Partial update a book (Admin only)
     */
    @PatchMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> partialUpdateBook(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        log.info("Admin partially updating book with ID: {}", id);
        Book existingBook = bookService.getBookById(id);

        updates.forEach((key, value) -> {
            switch (key.toLowerCase()) {
                case "title" -> existingBook.setTitle((String) value);
                case "author" -> existingBook.setAuthor((String) value);
                case "description" -> existingBook.setDescription((String) value);
                case "genre" -> existingBook.setGenre((String) value);
                case "price" -> existingBook.setPrice(((Number) value).doubleValue());
                case "available" -> existingBook.setAvailable((Boolean) value);
                case "pages" -> existingBook.setPages(((Number) value).intValue());
                case "language" -> existingBook.setLanguage((String) value);
                case "publisher" -> existingBook.setPublisher((String) value);
                case "publicationyear" -> existingBook.setPublicationYear(((Number) value).intValue());
                case "coverimageurl" -> existingBook.setCoverImageUrl((String) value);
                case "isbn" -> existingBook.setIsbn((String) value);
            }
        });

        Book updatedBook = bookService.updateBook(id, existingBook);
        return ResponseEntity.ok(updatedBook);
    }
    
    /**
     * Check if a book exists (public)
     */
    @GetMapping("/books/{id}/exists")
    public ResponseEntity<Map<String, Boolean>> bookExists(@PathVariable Long id) {
        log.debug("Checking if book {} exists", id);
        try {
            bookService.getBookById(id);
            return ResponseEntity.ok(Map.of("exists", true));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }
    
    /**
     * Get total books count (public)
     */
    @GetMapping("/books/count")
    public ResponseEntity<Map<String, Object>> getBooksCount() {
        log.debug("Counting total books");
        long count = bookService.getAllBooks().size();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
