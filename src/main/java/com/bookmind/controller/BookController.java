package com.bookmind.controller;

import com.bookmind.model.Book;
import com.bookmind.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookController {

    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
       return new ResponseEntity<>(bookService.getAllBooks(), HttpStatus.OK);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/books")
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            Book savedBook = bookService.addBook(book);
            return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
        }
        catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding book: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Error adding book: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book book) {
        try {
            Book updatedBook = bookService.updateBook(id, book);
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error updating book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error deleting book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/books/{bookId}/categories/{categoryId}")
    public ResponseEntity<?> addCategoryToBook(@PathVariable Long bookId, @PathVariable Long categoryId) {
        try {
            bookService.addCategoryToBook(bookId, categoryId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding category to book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/books/{bookId}/categories/{categoryId}")
    public ResponseEntity<?> removeCategoryFromBook(@PathVariable Long bookId, @PathVariable Long categoryId) {
        try {
            bookService.removeCategoryFromBook(bookId, categoryId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error removing category from book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/books/{bookId}/reviews/{reviewId}")
    public ResponseEntity<?> addReviewToBook(@PathVariable Long bookId, @PathVariable Long reviewId) {
        try {
            bookService.addReviewToBook(bookId, reviewId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding review to book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/books/{bookId}/reviews/{reviewId}")
    public ResponseEntity<?> removeReviewFromBook(@PathVariable Long bookId, @PathVariable Long reviewId) {
        try {
            bookService.removeReviewFromBook(bookId, reviewId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error removing review from book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    // Get categories for a specific book
    @GetMapping("/books/{id}/categories")
    public ResponseEntity<?> getBookCategories(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            return new ResponseEntity<>(book.getCategories(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Book not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    // Get reviews for a specific book
    @GetMapping("/books/{id}/reviews")
    public ResponseEntity<?> getBookReviews(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            return new ResponseEntity<>(book.getReviews(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Book not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    // Bulk operations - Add multiple categories to a book
    @PostMapping("/books/{bookId}/categories")
    public ResponseEntity<?> addCategoriesToBook(@PathVariable Long bookId, @RequestBody List<Long> categoryIds) {
        try {
            for (Long categoryId : categoryIds) {
                bookService.addCategoryToBook(bookId, categoryId);
            }
            return new ResponseEntity<>("Categories added successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding categories: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    // Remove all categories from a book
    @DeleteMapping("/books/{bookId}/categories")
    public ResponseEntity<?> removeAllCategoriesFromBook(@PathVariable Long bookId) {
        try {
            Book book = bookService.getBookById(bookId);
            // Create a copy of categories to avoid concurrent modification
            var categories = new ArrayList<>(book.getCategories());
            for (var category : categories) {
                bookService.removeCategoryFromBook(bookId, category.getId());
            }
            return new ResponseEntity<>("All categories removed successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error removing categories: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/books/search")
    public ResponseEntity<?> searchBooks(@RequestParam(required = false) String title,
                                         @RequestParam(required = false) String author,
                                         @RequestParam(required = false) String genre) {
        try {
            List<Book> books = bookService.searchBooks(title, author, genre);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error searching for books: " + e.getMessage(), 
                                       HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/advanced-search")
    public ResponseEntity<?> advancedSearchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean available) {
        try {
            List<Book> books = bookService.advancedSearchBooks(
                title, author, genre, description, minPrice, maxPrice, minRating, available);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error searching for books: " + e.getMessage(), 
                                       HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/categories/{categoryId}/books")
    public ResponseEntity<?> getBooksByCategory(@PathVariable Long categoryId) {
        try {
            List<Book> books = bookService.getBooksByCategory(categoryId);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error finding books: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/books/available")
    public ResponseEntity<?> getAvailableBooks() {
        try {
            List<Book> books = bookService.getAvailableBooks();
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error finding available books: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/top-rated")
    public ResponseEntity<?> getTopRatedBooks(@RequestParam(required = false, defaultValue = "4.0") Double minRating) {
        try {
            List<Book> books = bookService.getTopRatedBooks(minRating);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error finding top-rated books: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/price-range")
    public ResponseEntity<?> getBooksByPriceRange(@RequestParam Double maxPrice) {
        try {
            List<Book> books = bookService.getBooksByPriceRange(maxPrice);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error finding books by price: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/search/paged")
    public ResponseEntity<?> searchBooksWithPagination(
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
        try {
            Page<Book> books = bookService.searchBooksWithPagination(
                title, author, genre, description, minPrice, maxPrice, minRating, available,
                page, size, sortBy, direction);
                
            Map<String, Object> response = new HashMap<>();
            response.put("books", books.getContent());
            response.put("currentPage", books.getNumber());
            response.put("totalItems", books.getTotalElements());
            response.put("totalPages", books.getTotalPages());
                
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error searching for books: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ================== Additional Comprehensive Endpoints ==================
    
    // Get all books with pagination
    @GetMapping("/books/paged")
    public ResponseEntity<?> getAllBooksWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
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
                
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving books: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get books by author
    @GetMapping("/books/author")
    public ResponseEntity<?> getBooksByAuthor(@RequestParam String author) {
        try {
            List<Book> books = bookService.searchBooks(null, author, null);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error finding books by author: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get books by genre
    @GetMapping("/books/genre")
    public ResponseEntity<?> getBooksByGenre(@RequestParam String genre) {
        try {
            List<Book> books = bookService.searchBooks(null, null, genre);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error finding books by genre: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get book statistics/summary
    @GetMapping("/books/stats")
    public ResponseEntity<?> getBookStatistics() {
        try {
            List<Book> allBooks = bookService.getAllBooks();
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalBooks", allBooks.size());
            stats.put("availableBooks", allBooks.stream().filter(Book::getAvailable).count());
            stats.put("averagePrice", allBooks.stream().mapToDouble(Book::getPrice).average().orElse(0.0));
            stats.put("averageRating", allBooks.stream().mapToDouble(Book::getAverageRating).average().orElse(0.0));
            
            // Top genres
            Map<String, Long> genreCount = new HashMap<>();
            allBooks.forEach(book -> {
                String genre = book.getGenre();
                if (genre != null && !genre.trim().isEmpty()) {
                    genreCount.put(genre, genreCount.getOrDefault(genre, 0L) + 1);
                }
            });
            stats.put("genreDistribution", genreCount);
            
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error calculating statistics: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Partial update (PATCH) for books
    @PatchMapping("/books/{id}")
    public ResponseEntity<?> partialUpdateBook(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Book existingBook = bookService.getBookById(id);
            
            // Apply partial updates
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
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Book not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating book: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    // Check if a book exists
    @GetMapping("/books/{id}/exists")
    public ResponseEntity<?> bookExists(@PathVariable Long id) {
        try {
            bookService.getBookById(id);
            return new ResponseEntity<>(Map.of("exists", true), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("exists", false), HttpStatus.OK);
        }
    }
    
    // Get books count
    @GetMapping("/books/count")
    public ResponseEntity<?> getBooksCount() {
        try {
            long count = bookService.getAllBooks().size();
            return new ResponseEntity<>(Map.of("count", count), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error counting books: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
